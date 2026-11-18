package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
internal fun MeScreen(save: SaveState, onEdit: () -> Unit, onProgress: () -> Unit, onWalkthrough: () -> Unit = {}) {
    var section by rememberSaveable { mutableStateOf("Profile") }
    var settingsMenu by remember { mutableStateOf(false) }
    val days = save.trainingDays.orEmpty().filter { it in 1..7 }.distinct().sorted()
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val catalog = TrainingLibrary.catalog(save)
    val today = LocalDate.now()
    val achievements = playerAchievements(save, catalog, today)
    val earnedAchievements = achievements.filter { it.earned }
    val validResults = resultsInProgressRange(save.drillResults.orEmpty(), today, ProgressRange.ALL_TIME)
    val trainingDays = validResults.map { it.date }.distinct().size
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    CourtEyebrow("PLAYER ROOM")
                    Text("Me", color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.displaySmall)
                }
                Box {
                    IconButton(onClick = { settingsMenu = true }, modifier = Modifier.size(52.dp).testTag("me-settings-menu")) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Player settings menu",
                            tint = CourtQuestDesign.textPrimary)
                    }
                    // A popup is outside Scaffold's semantics root.
                    DropdownMenu(settingsMenu, { settingsMenu = false },
                        modifier = Modifier.semantics { testTagsAsResourceId = true }) {
                        DropdownMenuItem(text = { Text("Edit player profile") },
                            onClick = { settingsMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Default.Edit, null) })
                        DropdownMenuItem(modifier = Modifier.testTag("me-start-walkthrough"), text = { Text("Start app walkthrough") },
                            onClick = { settingsMenu = false; onWalkthrough() },
                            leadingIcon = { Icon(Icons.Default.HelpOutline, null) })
                        DropdownMenuItem(text = { Text("Training history") },
                            onClick = { settingsMenu = false; onProgress() },
                            leadingIcon = { Icon(Icons.Default.History, null) })
                    }
                }
            }
        }
        item {
            CourtQuestSegmentRail(
                labels = listOf("Profile", "Settings"),
                selected = section,
                onSelect = { section = it },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (section == "Profile") CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(20.dp), highlighted = true) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        CourtEyebrow("PLAYER PROFILE")
                        Box(
                            Modifier.size(if (LocalDensity.current.fontScale > 1.5f) 132.dp else 148.dp)
                                .clip(CircleShape).testTag("me-jersey-hero")
                                .background(Brush.radialGradient(listOf(Color(0xFF351609), Color(0xFF151517), Color(0xFF0B0B0C))))
                                .border(1.dp, CourtQuestDesign.accent.copy(alpha = .45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            ProfileJerseyHero(save.jerseyNumber, Modifier.fillMaxSize().padding(12.dp))
                        }
                        Text(save.playerName.ifBlank { "Your player profile" },
                            color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.displaySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text(if (save.jerseyNumber == null) "JERSEY NUMBER NOT SET"
                            else "JERSEY #${displayedJerseyNumber(save.jerseyNumber)}",
                            color = CourtQuestDesign.accent, style = MaterialTheme.typography.titleMedium)
                        Text("${rankFor(save.totalXp)}  ·  ${save.totalXp} XP",
                            color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineSmall)
                        CourtRankMeter(save.totalXp)
                        Text("${save.difficulty} · ${save.weeklyPlan} focus",
                            color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                        CourtRule()
                        if (LocalDensity.current.fontScale > 1.3f) {
                            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ProfileStat("TOTAL XP", save.totalXp.toString(), Modifier.fillMaxWidth(), true)
                                ProfileStat("RECORDED DRILLS", validResults.size.toString(), Modifier.fillMaxWidth())
                                ProfileStat("TRAINING DAYS", trainingDays.toString(), Modifier.fillMaxWidth())
                                ProfileStat("ACHIEVEMENTS", earnedAchievements.size.toString(), Modifier.fillMaxWidth())
                            }
                        } else {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                ProfileStat("TOTAL XP", save.totalXp.toString(), Modifier.weight(1f), true)
                                Box(Modifier.width(1.dp).height(34.dp).background(CourtQuestDesign.border))
                                ProfileStat("RECORDED DRILLS", validResults.size.toString(), Modifier.weight(1f))
                                Box(Modifier.width(1.dp).height(34.dp).background(CourtQuestDesign.border))
                                ProfileStat("TRAINING DAYS", trainingDays.toString(), Modifier.weight(1f))
                                Box(Modifier.width(1.dp).height(34.dp).background(CourtQuestDesign.border))
                                ProfileStat("ACHIEVEMENTS", earnedAchievements.size.toString(), Modifier.weight(1f))
                            }
                        }
                        CourtQuestPrimaryButton(onEdit, Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Edit, null); Spacer(Modifier.width(8.dp)); Text("Edit profile")
                        }
                    }
                }
                if (section == "Settings") {
                    CourtMeSettingsHub(save, days.map { dayLabels[it - 1] }, onEdit, onWalkthrough)
                }
                if (section == "Profile") {
                    CourtHeading("Milestones", "${earnedAchievements.size} earned")
                    if (earnedAchievements.isEmpty()) {
                        CourtQuestPremiumSurface(Modifier.fillMaxWidth().testTag("me-achievements-empty"),
                            PaddingValues(14.dp)) {
                            Text("Save a completed drill to unlock your first evidence-based milestone.",
                                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        earnedAchievements.take(2).forEach { achievement ->
                            CourtQuestPremiumSurface(
                                Modifier.fillMaxWidth().testTag("me-achievement-${achievement.id}"),
                                PaddingValues(14.dp), highlighted = true
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Default.EmojiEvents, null, tint = CourtQuestDesign.accent)
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(achievement.title, color = CourtQuestDesign.textPrimary,
                                            style = MaterialTheme.typography.titleMedium)
                                        Text("Earned ${achievement.earnedDate ?: "date unavailable"}",
                                            color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                    Text("XP tracks participation. Results are self-recorded.",
                        color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onProgress, Modifier.fillMaxWidth().heightIn(min = 52.dp)
                        .testTag("me-open-progress")) { Text("Open Progress and history") }
                }
                if (section == "Settings") {
                    CourtRule()
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Default.PhoneAndroid, null, tint = CourtQuestDesign.accent)
                        Spacer(Modifier.width(10.dp))
                        Text("Saved on this phone", color = CourtQuestDesign.textPrimary,
                            style = MaterialTheme.typography.titleMedium)
                    }
                    Text("Your profile, training and results stay on this device. Cloud backup is not connected.",
                        color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: String, modifier: Modifier = Modifier, highlighted: Boolean = false) {
    Column(modifier.padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, color = if (highlighted) CourtQuestDesign.accent else CourtQuestDesign.textPrimary,
            style = MaterialTheme.typography.headlineSmall, maxLines = 1)
        Text(label, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.labelSmall)
    }
}


/** Profile jersey uses the approved high-resolution atlas rather than a simplified vector. */
@Composable
private fun ProfileJerseyHero(jerseyNumber: Int?, modifier: Modifier = Modifier) {
    val atlas = rememberNavigationAtlas()
    Box(modifier, contentAlignment = Alignment.Center) {
        if (atlas != null) {
            Canvas(Modifier.fillMaxSize()) {
                val cellLeft = atlas.width * PlayerTab.ME.ordinal / 5
                val cellRight = atlas.width * (PlayerTab.ME.ordinal + 1) / 5
                val cellSize = minOf(cellRight - cellLeft, 434)
                val cellTop = ((atlas.height - cellSize) / 2).coerceAtLeast(0)
                // Focus the upper jersey portion, excluding the basketball at the foot of the tile.
                val cropHeight = (cellSize * .73f).toInt()
                val cropWidth = (cellSize * .78f).toInt()
                drawImage(
                    image = atlas,
                    srcOffset = androidx.compose.ui.unit.IntOffset(
                        cellLeft + (cellRight - cellLeft - cropWidth) / 2, cellTop
                    ),
                    srcSize = androidx.compose.ui.unit.IntSize(cropWidth, cropHeight),
                    dstOffset = androidx.compose.ui.unit.IntOffset.Zero,
                    dstSize = androidx.compose.ui.unit.IntSize(
                        size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1)
                    )
                )
            }
        }
        Text(
            displayedJerseyNumber(jerseyNumber).toString(),
            modifier = Modifier.offset(y = (-7).dp),
            color = Color(0xFFFF9A51),
            fontFamily = CourtDisplay,
            fontWeight = FontWeight.Black,
            fontSize = 40.sp,
            lineHeight = 42.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
