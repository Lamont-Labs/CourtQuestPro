package com.courtquestpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProgressDashboardScreen(
    save: SaveState,
    onHome: () -> Unit,
    onGames: () -> Unit,
    onAllTraining: () -> Unit,
    onMissionReceipt: (String) -> Unit
) {
    var section by rememberSaveable { mutableStateOf("Overview") }
    var rangeName by rememberSaveable { mutableStateOf(ProgressRange.THIRTY_DAYS.name) }
    val range = ProgressRange.entries.firstOrNull { it.name == rangeName } ?: ProgressRange.THIRTY_DAYS
    val today = LocalDate.now(ZoneId.systemDefault())
    val catalog = TrainingLibrary.catalog(save)
    val allResults = validSavedDrillResults(save.drillResults.orEmpty())
    val shownResults = resultsInProgressRange(allResults, today, range)
    val activeDays = shownResults.map { it.date }.distinct().size
    val validReceipts = validMissionReceipts(save).filter { receipt ->
        val date = missionReceiptDate(receipt)
        if (date == null) range == ProgressRange.ALL_TIME
        else dateInProgressRange(date, today, range)
    }
    val receiptHistory = save.missionReceipts.orEmpty().distinctBy { it.id }.filter { receipt ->
        val date = missionReceiptDate(receipt)
        if (date == null) range == ProgressRange.ALL_TIME
        else dateInProgressRange(date, today, range)
    }.sortedBy { it.savedAt }
    val achievements = playerAchievements(save, catalog, today)
    val bests = comparablePersonalBests(allResults)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)) {
                CourtEyebrow("PLAYER DEVELOPMENT")
                Text("Progress", Modifier.testTag("progress-screen-title"),
                    color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.displaySmall)
            }
        }
        item { CourtProgressSectionPicker(section) { section = it } }
        item {
            FlowRow(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                maxItemsInEachRow = if (LocalDensity.current.fontScale > 1.5f) 1 else 3
            ) {
                ProgressRange.entries.forEach { option ->
                    FilterChip(
                        selected = option == range,
                        onClick = { rangeName = option.name },
                        label = { Text(if (option == ProgressRange.ALL_TIME) "All time" else option.label) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("progress-range-${option.name.lowercase()}")
                    )
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when (section) {
                    "Overview" -> {
                        LifetimeProgressCard(save.totalXp)
                        ActivityTrendCard(save.drillResults.orEmpty(), today, range)
                        Text(
                            if (range == ProgressRange.ALL_TIME) "ALL-TIME TRAINING SUMMARY"
                            else "${range.label} TRAINING SUMMARY",
                            color = CourtQuestDesign.textSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                        FlowRow(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            maxItemsInEachRow = if (LocalDensity.current.fontScale > 1.35f) 1 else 2
                        ) {
                            ProgressMetric(
                                "RECORDED DRILLS", shownResults.size.toString(),
                                "Saved results", Modifier.weight(1f).testTag("progress-record-sessions")
                            )
                            ProgressMetric(
                                "TRAINING DAYS", activeDays.toString(),
                                "Distinct dates", Modifier.weight(1f).testTag("progress-training-days")
                            )
                            ProgressMetric(
                                "MISSIONS", validReceipts.size.toString(),
                                "Verified receipts", Modifier.weight(1f).testTag("progress-completed-missions")
                            )
                            ProgressMetric(
                                "EXTRA PRACTICE", shownResults.count { it.practice }.toString(),
                                "Saved practice drills", Modifier.weight(1f).testTag("progress-extra-practice")
                            )
                        }
                        if (shownResults.isEmpty()) {
                            CourtQuestPremiumSurface(
                                Modifier.fillMaxWidth().testTag("progress-empty"),
                                PaddingValues(16.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Insights, null, tint = CourtQuestDesign.accent)
                                    Text(
                                        "No saved training in this period. Your progress updates when a drill is completed and saved.",
                                        color = CourtQuestDesign.textSecondary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        CourtHeading("Personal bests", "Only repeated, comparable drill results")
                        if (bests.isEmpty()) {
                            CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(14.dp)) {
                                Text(
                                    "A best appears after the same saved drill is recorded again with the same measurement and attempt count.",
                                    color = CourtQuestDesign.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            bests.take(3).forEach { best ->
                                PersonalBestCard(best)
                            }
                        }
                        CourtHeading("Milestones", "Unlocked from saved training evidence")
                        val earned = achievements.filter { it.earned }.take(3)
                        if (earned.isEmpty()) {
                            CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(14.dp)) {
                                Text(
                                    "Milestones will appear here when your saved activity meets their stated criteria.",
                                    color = CourtQuestDesign.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            earned.forEach { achievement -> AchievementHighlight(achievement) }
                        }
                    }
                    "Skills" -> {
                        CourtHeading("Skill evidence", "Recorded makes and attempts · $rangeLabel(range)")
                        courtSkillCategories.forEach { category ->
                            val skillResults = shownResults.filter { drillCategory(it, catalog) == category }
                            SkillProgressCard(category, skillResults)
                        }
                        Text(
                            "These are saved results, not ratings or predictions. A category with no matching records has no score.",
                            color = CourtQuestDesign.textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    "History" -> {
                        CourtHeading("Saved training", "Drills and practice · ${rangeLabel(range)}")
                        val raw = if (range == ProgressRange.ALL_TIME) {
                            save.drillResults.orEmpty().asReversed()
                        } else shownResults.asReversed()
                        if (raw.isEmpty()) {
                            CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp)) {
                                Text("No training records in this period.", color = CourtQuestDesign.textSecondary)
                            }
                        } else {
                            raw.take(10).forEach { result ->
                                ProgressHistoryRow(result, catalog)
                            }
                            if (raw.size > 10) {
                                TextButton(
                                    onClick = onAllTraining,
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                        .testTag("progress-view-all-training")
                                ) { Text("View all ${raw.size} saved drills") }
                            }
                        }
                        CourtHeading("Completed Missions", "Valid saved receipts")
                        if (receiptHistory.isEmpty()) {
                            Text("No completed Mission receipts in this period.",
                                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            receiptHistory.asReversed().forEach { receipt ->
                                val version = save.missionVersions.orEmpty()
                                    .firstOrNull { it.id == receipt.missionVersionId }
                                val verified = runCatching { MissionTraining.receiptValid(save, receipt) }
                                    .getOrDefault(false)
                                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(14.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(version?.title ?: "Saved Mission",
                                            color = CourtQuestDesign.textPrimary,
                                            style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${missionReceiptDate(receipt)?.let(::formatProgressDate) ?: "Date unavailable"} · ${receipt.resultIds.size} saved drills · " +
                                                if (verified) "verified" else "saved evidence unavailable",
                                            color = CourtQuestDesign.textSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        TextButton(
                                            onClick = { onMissionReceipt(receipt.id) },
                                            modifier = Modifier.heightIn(min = 48.dp).testTag("progress-mission-receipt-${receipt.id}")
                                        ) { Text("View receipt") }
                                    }
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = onGames,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("progress-game-history")
                        ) {
                            Icon(Icons.Default.SportsBasketball, null)
                            Spacer(Modifier.width(8.dp))
                            Text("View game history")
                        }
                    }
                }
                TextButton(
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) { Text("Back to Court") }
            }
        }
    }
}

private fun dateInProgressRange(date: LocalDate, today: LocalDate, range: ProgressRange): Boolean {
    if (date.isAfter(today)) return false
    val days = range.days ?: return true
    return !date.isBefore(today.minusDays((days - 1).toLong()))
}

private fun rangeLabel(range: ProgressRange): String = when (range) {
    ProgressRange.SEVEN_DAYS -> "last 7 days"
    ProgressRange.THIRTY_DAYS -> "last 30 days"
    ProgressRange.ALL_TIME -> "all time"
}

@Composable
private fun LifetimeProgressCard(totalXp: Int) {
    CourtQuestPremiumSurface(
        Modifier.fillMaxWidth().testTag("progress-lifetime-xp"),
        PaddingValues(16.dp),
        highlighted = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    CourtEyebrow("PLAYER LEVEL")
                    Text(rankFor(totalXp), color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.headlineLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(totalXp.toString(), color = CourtQuestDesign.accent,
                        style = MaterialTheme.typography.headlineLarge)
                    CourtEyebrow("TOTAL XP", color = CourtQuestDesign.textSecondary)
                }
            }
            CourtRankMeter(totalXp)
            Text(
                "XP is earned from recorded activity. Level reflects participation, not a skill rating.",
                color = CourtQuestDesign.textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ActivityTrendCard(results: List<DrillResult>, today: LocalDate, range: ProgressRange) {
    val buckets = courtActivityBuckets(results, today, range)
    val count = resultsInProgressRange(results, today, range).size
    val activeDays = resultsInProgressRange(results, today, range).map { it.date }.distinct().size
    val period = if (range == ProgressRange.ALL_TIME) "LAST 12 MONTHS · MONTHLY" else "DAILY · ${range.label}"
    CourtQuestPremiumSurface(
        Modifier.fillMaxWidth().testTag("progress-activity"),
        PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CourtHeading("Training activity", period)
            CourtActivityBucketsChart(
                buckets,
                Modifier.fillMaxWidth().height(124.dp).testTag("progress-activity-chart")
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(buckets.firstOrNull()?.label.orEmpty(), color = CourtQuestDesign.textSecondary,
                    style = MaterialTheme.typography.labelSmall)
                Text(if (range == ProgressRange.ALL_TIME) "CURRENT MONTH" else "TODAY",
                    color = CourtQuestDesign.accent, style = MaterialTheme.typography.labelSmall)
            }
            Text(
                if (count == 0) "No recorded activity in ${rangeLabel(range)}."
                else "$count saved ${if (count == 1) "drill" else "drills"} · $activeDays ${if (activeDays == 1) "training day" else "training days"}",
                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CourtActivityBucketsChart(buckets: List<CourtActivityBucket>, modifier: Modifier = Modifier) {
    val values = buckets.map { it.count }
    val maximum = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    Canvas(modifier.semantics {
        contentDescription = if (values.sum() == 0) "No saved activity in this time range."
        else "Saved drills by time bucket: " + buckets.joinToString("; ") { "${it.label}: ${it.count}" }
    }) {
        val baseline = size.height - 10.dp.toPx()
        val left = 2.dp.toPx()
        val gap = if (buckets.size > 14) 2.dp.toPx() else 5.dp.toPx()
        val barWidth = ((size.width - left * 2 - gap * (buckets.size - 1)) / buckets.size).coerceAtLeast(1.dp.toPx())
        listOf(0f, .5f, 1f).forEach { level ->
            val y = baseline - (baseline - 8.dp.toPx()) * level
            drawLine(Color(0xFF292A2C), Offset(left, y), Offset(size.width - left, y), 1.dp.toPx())
        }
        values.forEachIndexed { index, value ->
            if (value > 0) {
                val height = (baseline - 8.dp.toPx()) * value / maximum
                val x = left + index * (barWidth + gap)
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(CourtQuestDesign.accent, CourtQuestDesign.accentVivid)),
                    topLeft = Offset(x, baseline - height),
                    size = androidx.compose.ui.geometry.Size(barWidth, height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
                )
            }
        }
    }
}

@Composable
private fun ProgressMetric(label: String, value: String, detail: String, modifier: Modifier = Modifier) {
    CourtQuestPremiumSurface(modifier, PaddingValues(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineMedium)
            CourtEyebrow(label, color = CourtQuestDesign.textSecondary)
            Text(detail, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PersonalBestCard(best: CourtPersonalBest) {
    val result = best.result
    CourtQuestPremiumSurface(Modifier.fillMaxWidth().testTag("progress-personal-best-${result.drillId}"),
        PaddingValues(14.dp), highlighted = best.improved) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.CheckCircle, null, tint = CourtQuestDesign.accent)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(result.drillSnapshot?.title ?: "Saved drill", color = CourtQuestDesign.textPrimary,
                    style = MaterialTheme.typography.titleMedium)
                Text(
                    "Best recorded · ${result.completed} / ${result.attempted} ${result.measurementSnapshot?.successLabel ?: "successes"}",
                    color = CourtQuestDesign.accent, style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    if (best.improved) "Raised across ${best.sampleCount} matching saved results."
                    else "Across ${best.sampleCount} comparable saved results.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SkillProgressCard(category: String, results: List<DrillResult>) {
    val attempts = results.sumOf { it.attempted }
    val makes = results.sumOf { it.completed }
    CourtQuestPremiumSurface(Modifier.fillMaxWidth().testTag("progress-skill-${category.lowercase().replace(' ', '-')}"),
        PaddingValues(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(category, modifier = Modifier.weight(1f), color = CourtQuestDesign.textPrimary,
                    style = MaterialTheme.typography.titleMedium)
                Text("${results.size} ${if (results.size == 1) "result" else "results"}",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.labelSmall)
            }
            if (attempts == 0) {
                Text("No saved evidence yet.", color = CourtQuestDesign.textSecondary,
                    style = MaterialTheme.typography.bodySmall)
            } else {
                LinearProgressIndicator(
                    progress = { (makes.toFloat() / attempts).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = CourtQuestDesign.accentVivid,
                    trackColor = CourtQuestDesign.elevatedSurface
                )
                Text("$makes successful / $attempts attempts · ${makes * 100 / attempts}% recorded rate",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun AchievementHighlight(achievement: AchievementProgress) {
    CourtQuestPremiumSurface(Modifier.fillMaxWidth().testTag("progress-milestone-${achievement.id}"),
        PaddingValues(14.dp), highlighted = true) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.CheckCircle, null, tint = CourtQuestDesign.accent)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(achievement.title, color = CourtQuestDesign.textPrimary,
                    style = MaterialTheme.typography.titleMedium)
                Text("${achievement.category.label} · earned ${achievement.earnedDate?.let(::formatProgressDate) ?: "date unavailable"}",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ProgressHistoryRow(result: DrillResult, catalog: List<Drill>) {
    val title = result.drillSnapshot?.title ?: catalog.firstOrNull { it.id == result.drillId }?.title ?: "Saved drill"
    val kind = when {
        result.missionSessionId?.startsWith("mission-session_") == true -> "Mission drill"
        result.practice -> "Extra practice"
        result.missionSessionId?.startsWith("legacy_daily_") == true -> "Daily training"
        else -> "Training"
    }
    CourtQuestPremiumSurface(Modifier.fillMaxWidth().testTag("progress-history-${drillResultIdentity(result)}"),
        PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.History, null, tint = CourtQuestDesign.accent)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                CourtEyebrow("$kind · ${formatProgressDateOrRaw(result.date)}")
                Text(title, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
                Text("${result.completed} successful / ${result.attempted} attempts",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun formatProgressDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
private fun formatProgressDateOrRaw(value: String): String =
    runCatching { formatProgressDate(LocalDate.parse(value)) }.getOrElse { "Date unavailable" }
