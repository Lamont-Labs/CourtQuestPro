package com.courtquestpro

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
internal fun MeScreen(save: SaveState, onEdit: () -> Unit, onProgress: () -> Unit, onTeam: () -> Unit = {}, onWalkthrough: () -> Unit = {}) {
    var section by rememberSaveable { mutableStateOf("Profile") }
    var settingsMenu by remember { mutableStateOf(false) }
    val days = (save.trainingDays ?: listOf(1, 3, 5)).filter { it in 1..7 }.distinct().sorted()
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
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
                            Modifier.size(190.dp).clip(CircleShape).testTag("me-jersey-hero")
                                .background(Brush.radialGradient(listOf(Color(0xFF351609), Color(0xFF151517), Color(0xFF0B0B0C))))
                                .border(1.dp, CourtQuestDesign.accent.copy(alpha = .45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CourtQuestJerseyArtwork(save.jerseyNumber, Modifier.fillMaxSize().padding(8.dp))
                            Box(Modifier.align(Alignment.BottomEnd).padding(30.dp).size(44.dp)
                                .testTag("me-neutral-avatar")
                                .background(CourtQuestDesign.surface, CircleShape)
                                .border(1.dp, CourtQuestDesign.accent, CircleShape),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, "Player avatar not set",
                                    Modifier.size(25.dp), tint = CourtQuestDesign.textPrimary)
                            }
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
                                ProfileStat("RECORDED DRILLS", save.drillResults.orEmpty().size.toString(), Modifier.fillMaxWidth())
                                ProfileStat("GAMES", save.matches.size.toString(), Modifier.fillMaxWidth())
                            }
                        } else {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                ProfileStat("TOTAL XP", save.totalXp.toString(), Modifier.weight(1f), true)
                                Box(Modifier.width(1.dp).height(34.dp).background(CourtQuestDesign.border))
                                ProfileStat("RECORDED DRILLS", save.drillResults.orEmpty().size.toString(), Modifier.weight(1f))
                                Box(Modifier.width(1.dp).height(34.dp).background(CourtQuestDesign.border))
                                ProfileStat("GAMES", save.matches.size.toString(), Modifier.weight(1f))
                            }
                        }
                        CourtQuestPrimaryButton(onEdit, Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Edit, null); Spacer(Modifier.width(8.dp)); Text("Edit profile")
                        }
                    }
                }
                if (section == "Settings") {
                    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(18.dp), highlighted = true) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, null, tint = CourtQuestDesign.accent)
                                Spacer(Modifier.width(10.dp))
                                CourtEyebrow("YOUR ROUTINE")
                            }
                            Text(if (days.isEmpty()) "Routine not set." else "${days.size} training ${if (days.size == 1) "day" else "days"}.",
                                color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineMedium)
                            Text(if (days.isEmpty()) "No training days selected. Set your routine on Court."
                                else days.joinToString("  ·  ") { dayLabels[it - 1] },
                                color = CourtQuestDesign.accent, style = MaterialTheme.typography.titleMedium)
                            CourtRule()
                            Text("${save.weeklyPlan} focus · ${save.difficulty} level",
                                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                            Text("Your focus and weekly schedule can be adjusted on Court. Your level applies to new daily workouts.",
                                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                            OutlinedButton(onEdit, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Edit player settings") }
                            OutlinedButton(onWalkthrough, Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                .testTag("me-settings-start-walkthrough")) {
                                Icon(Icons.Default.HelpOutline, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Start app walkthrough")
                            }
                        }
                    }
                }
                if (section == "Profile") {
                    Text("XP tracks participation. Results are self-recorded.",
                        color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onProgress, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Open training history") }
                }
                if (section == "Settings") {
                    CourtRule()
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Default.PhoneAndroid, null, tint = CourtQuestDesign.accent)
                        Spacer(Modifier.width(10.dp))
                        Text("Saved on this phone", color = CourtQuestDesign.textPrimary,
                            style = MaterialTheme.typography.titleMedium)
                    }
                    Text("Your profile, Missions and results stay on this device. Cloud backup and team sharing are not connected.",
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
