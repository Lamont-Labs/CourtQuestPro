package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate

/** Evidence-backed daily and weekly quests, achievements, and existing Mission receipts. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuestsScreen(save: SaveState, today: String, onTraining: () -> Unit, onBuild: () -> Unit,
    onResumeMission: () -> Unit, onProgress: () -> Unit) {
    var section by rememberSaveable { mutableStateOf("Active") }
    val catalog = TrainingLibrary.catalog(save)
    val now = Instant.now()
    val questDate = runCatching { LocalDate.parse(today) }.getOrElse { LocalDate.now() }
    val dailyQuest = CourtQuestEngine.dailyProgress(save, questDate, now)
    val weeklyQuest = CourtQuestEngine.weeklyProgress(save, questDate, now)
    val dailyPlan = CourtQuestEngine.dailyPlanProgress(save, questDate)
    val achievements = playerAchievements(save, catalog, questDate)
    val slots = if (save.activeDate == today) save.slots else emptyList()
    val recordedIds = save.drillResults.orEmpty().filter { result ->
        result.date == today && !result.practice && result.attempted > 0 && result.completed in 0..result.attempted &&
            slots.any { it.id == result.slotId && it.drillId == result.drillId }
    }.map { it.slotId }.toSet()
    val done = recordedIds.size
    val active = MissionTraining.active(save)
    val version = active?.let { MissionTraining.version(save, it) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp)) {
        item {
            Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)) {
                CourtEyebrow("PLAYER CHALLENGES")
                Text("Quests", Modifier.testTag("quests-screen-title"),
                    color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.displaySmall)
            }
        }
        item {
            CourtArenaHero(R.drawable.court_arena_hoop,
                Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    .height(if (LocalDensity.current.fontScale > 1.5f) 112.dp else 136.dp)
                    .testTag("quests-challenge-hero"), imageAlignment = Alignment.TopEnd) {
                Row(Modifier.align(Alignment.BottomStart).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    CourtQuestShield(Modifier.size(44.dp), if (slots.isEmpty()) 0f else done.toFloat() / slots.size)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        CourtEyebrow("REAL SAVED TRAINING")
                        Text("Build your next milestone.", color = CourtQuestDesign.textPrimary,
                            style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
        item {
            FlowRow(
                Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                maxItemsInEachRow = if (LocalDensity.current.fontScale > 1.5f) 2 else 3
            ) {
                listOf("Active", "Completed", "Achievements").forEach { tab ->
                    FilterChip(
                        section == tab, { section = tab }, label = { Text(tab) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("quests-tab-${tab.lowercase()}")
                    )
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (section == "Active") {
                    QuestProgressCard(dailyQuest, "DAILY QUEST", "quests-daily-checkin")
                    QuestProgressCard(weeklyQuest, "WEEKLY QUEST", "quests-weekly-consistency")
                }
                if (section == "Active" && active != null) CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(20.dp), highlighted = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CourtEyebrow("YOUR ACTIVE MISSION")
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CourtQuestShield(Modifier.size(52.dp),
                                (MissionTraining.currentIndex(save, active) ?: version?.steps?.size ?: 0).toFloat() /
                                    (version?.steps?.size?.coerceAtLeast(1) ?: 1))
                            Text(version?.title ?: "Your saved Mission", modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.headlineMedium, color = CourtQuestDesign.textPrimary)
                        }
                        Text("${MissionTraining.currentIndex(save, active) ?: version?.steps?.size ?: 0} of ${version?.steps?.size ?: 0} drills recorded",
                            style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.accent)
                        Text("Your recorded drills and unfinished attempts are safe.", style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                        CourtQuestPrimaryButton(onResumeMission, Modifier.fillMaxWidth()) { Text("Resume Mission") }
                    }
                }
                if (section == "Active") CourtQuestPremiumSurface(
                    Modifier.fillMaxWidth().testTag("quests-daily-plan"),
                    PaddingValues(16.dp), highlighted = active == null
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CourtEyebrow("TODAY'S TRAINING PLAN")
                        Text(
                            if ("six_$today" in save.awardedKeys) "100 XP completion bonus recorded"
                            else "50 XP per daily drill · 100 XP six-drill completion bonus",
                            style = MaterialTheme.typography.labelLarge, color = CourtQuestDesign.accent)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(dailyPlan.current.toString().padStart(2, '0'), style = MaterialTheme.typography.displaySmall, color = CourtQuestDesign.accent)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    when {
                                        dailyPlan.target == 0 -> "No daily plan is available yet."
                                        dailyPlan.complete -> "Work recorded."
                                        else -> "Build your daily training plan."
                                    },
                                    style = MaterialTheme.typography.headlineMedium, color = CourtQuestDesign.textPrimary)
                                Text("${dailyPlan.current} of ${dailyPlan.target} drills recorded today",
                                    style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                            }
                        }
                        Row(Modifier.fillMaxWidth().height(5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(maxOf(dailyPlan.target, 1)) { index ->
                                Box(Modifier.weight(1f).fillMaxHeight()
                                    .background(if (index < dailyPlan.current) CourtQuestDesign.accentVivid else CourtQuestDesign.border,
                                        RoundedCornerShape(4.dp)))
                            }
                        }
                        Text("Recorded today: ${dailyPlan.earnedXp} / ${dailyPlan.possibleXp} XP",
                            color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                        if (slots.isNotEmpty() && dailyPlan.complete) {
                            CourtQuestPrimaryButton(onProgress, Modifier.fillMaxWidth()) { Text("See today's results") }
                        } else if (active == null && dailyPlan.target > 0) {
                            CourtQuestPrimaryButton(onTraining, Modifier.fillMaxWidth()) { Text(if (dailyPlan.current > 0) "Continue daily training" else "Start daily training") }
                        }
                        Text("Recorded results count here. Take rest when you need it; your saved work stays yours.",
                            style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                    }
                }
                if (section == "Active") slots.forEach { slot ->
                    val drill = catalog.firstOrNull { it.id == slot.drillId }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (slot.id in recordedIds) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            if (slot.id in recordedIds) "Recorded" else "Not recorded", tint = CourtQuestDesign.accent)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(drill?.category ?: "Training", style = MaterialTheme.typography.titleMedium, color = CourtQuestDesign.textPrimary)
                            Text(drill?.title ?: "Saved drill", style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                        }
                    }
                }
                if (section == "Active") {
                CourtRule()
                CourtHeading("Make your own Mission")
                Text("Choose a short session or build a longer lineup. Saved versions keep the same drills and targets.",
                    style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                OutlinedButton(onBuild, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Build a Mission") }
                }
                if (section == "Completed") {
                CourtHeading("Completed quests", "Rewards already recorded")
                val completedQuests = CourtQuestEngine.completedHistory(save)
                if (completedQuests.isEmpty()) {
                    Text("Completed daily and weekly quests will appear here after their saved-result rewards are granted.",
                        style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                } else completedQuests.forEach { completion ->
                    CourtQuestPremiumSurface(
                        Modifier.fillMaxWidth().testTag("quests-completed-${completion.id}"),
                        PaddingValues(14.dp), highlighted = true
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = CourtQuestDesign.accent)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(completion.title, color = CourtQuestDesign.textPrimary,
                                    style = MaterialTheme.typography.titleMedium)
                                Text("${completion.periodLabel} · +${completion.rewardXp} XP recorded",
                                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                CourtHeading("Missions you finished")
                val receipts = save.missionReceipts.orEmpty().distinctBy { it.id }.sortedBy { it.savedAt }
                val count = receipts.size
                Text(if (count == 0) "No saved Mission receipts yet. Completed receipts remain available in Progress."
                    else "$count saved ${if (count == 1) "Mission receipt" else "Mission receipts"} · Stored on this device",
                    style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                receipts.asReversed().forEach { receipt ->
                    val savedVersion = save.missionVersions.orEmpty().firstOrNull { it.id == receipt.missionVersionId }
                    val verified = runCatching { MissionTraining.receiptValid(save, receipt) }.getOrDefault(false)
                    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CourtQuestShield(Modifier.size(44.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(savedVersion?.title ?: "Saved Mission", color = CourtQuestDesign.textPrimary,
                                    style = MaterialTheme.typography.titleLarge)
                                Text("Version ${savedVersion?.version ?: "?"} · ${receipt.resultIds.size} saved drills",
                                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                                Text(if (verified) "Completion evidence verified"
                                    else "Receipt preserved · evidence could not be verified",
                                    color = if (verified) CourtQuestDesign.accent else CourtQuestDesign.textSecondary,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                OutlinedButton(onProgress, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("View saved results") }
                }
                if (section == "Achievements") {
                    CourtHeading("Player achievements", "Earned and locked · based on saved evidence")
                    achievements.forEach { achievement -> QuestAchievementCard(achievement) }
                }
            }
        }
    }
}

@Composable
private fun QuestProgressCard(status: QuestProgress, cadence: String, tag: String) {
    val fraction = (status.current.toFloat() / status.target.coerceAtLeast(1)).coerceIn(0f, 1f)
    CourtQuestPremiumSurface(
        Modifier.fillMaxWidth().testTag(tag),
        PaddingValues(16.dp),
        highlighted = status.complete && status.rewardGranted
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    if (status.complete && status.rewardGranted) Icons.Default.CheckCircle else Icons.Default.Flag,
                    contentDescription = null,
                    tint = CourtQuestDesign.accent
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    CourtEyebrow(cadence)
                    Text(status.title, color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.titleLarge)
                }
                Text("+${status.rewardXp} XP", color = CourtQuestDesign.accent,
                    style = MaterialTheme.typography.labelLarge)
            }
            Text(status.objective, color = CourtQuestDesign.textSecondary,
                style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().height(6.dp)
                    .semantics { contentDescription = "${status.current} of ${status.target} saved training goals" },
                color = CourtQuestDesign.accentVivid,
                trackColor = CourtQuestDesign.elevatedSurface
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${status.current} of ${status.target} complete",
                    Modifier.weight(1f), color = CourtQuestDesign.textSecondary,
                    style = MaterialTheme.typography.bodySmall)
                if (status.rewardGranted) {
                    Text("REWARD RECORDED", color = CourtQuestDesign.accent,
                        style = MaterialTheme.typography.labelSmall)
                } else {
                    Text("REWARD +${status.rewardXp} XP", color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
            if (status.complete && status.rewardGranted) {
                Text("Challenge complete · +${status.rewardXp} XP is already in your total.",
                    color = CourtQuestDesign.accent, style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("$tag-earned"))
            }
        }
    }
}

@Composable
private fun QuestAchievementCard(achievement: AchievementProgress) {
    val progress = (achievement.displayProgress.toFloat() / achievement.target.coerceAtLeast(1)).coerceIn(0f, 1f)
    CourtQuestPremiumSurface(
        Modifier.fillMaxWidth().testTag("quests-achievement-${achievement.id}"),
        PaddingValues(14.dp),
        highlighted = achievement.earned
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                if (achievement.earned) Icons.Default.EmojiEvents else Icons.Default.Lock,
                contentDescription = if (achievement.earned) "Achievement earned" else "Achievement locked",
                tint = if (achievement.earned) CourtQuestDesign.accent else CourtQuestDesign.textSecondary
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(achievement.title, Modifier.weight(1f), color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.titleMedium)
                    Text(achievement.category.label.uppercase(), color = CourtQuestDesign.accent,
                        style = MaterialTheme.typography.labelSmall)
                }
                Text(achievement.description, color = CourtQuestDesign.textSecondary,
                    style = MaterialTheme.typography.bodySmall)
                if (achievement.earned) {
                    Text("EARNED · ${achievement.earnedDate?.toString() ?: "date unavailable"}", color = CourtQuestDesign.accent,
                        style = MaterialTheme.typography.labelMedium)
                } else {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(5.dp),
                        color = CourtQuestDesign.accentVivid,
                        trackColor = CourtQuestDesign.elevatedSurface
                    )
                    Text("${achievement.displayProgress} of ${achievement.target}",
                        color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
