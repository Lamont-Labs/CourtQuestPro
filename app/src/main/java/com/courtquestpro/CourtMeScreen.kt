package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun MeScreen(save: SaveState, onEdit: () -> Unit, onProgress: () -> Unit) {
    var section by rememberSaveable { mutableStateOf("Profile") }
    val days = (save.trainingDays ?: listOf(1, 3, 5)).filter { it in 1..7 }.distinct().sorted()
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp)) {
        item { AppHeader("Me", "Player profile and training setup") }
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
                if (section == "Profile") CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp), highlighted = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CourtEyebrow("PLAYER PROFILE")
                        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CourtQuestJerseyArtwork(save.jerseyNumber, Modifier.size(92.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(save.playerName.ifBlank { "Your player profile" },
                                    color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineLarge)
                                Text("Jersey #${displayedJerseyNumber(save.jerseyNumber)}",
                                    color = CourtQuestDesign.accent, style = MaterialTheme.typography.titleMedium)
                                Text("${save.difficulty} · ${save.weeklyPlan} focus",
                                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        CourtRule()
                        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            ProfileStat("TOTAL XP", save.totalXp.toString(), Modifier.weight(1f), true)
                            Box(Modifier.width(1.dp).height(34.dp).background(CourtQuestDesign.border))
                            ProfileStat("RECORDED DRILLS", save.drillResults.orEmpty().size.toString(), Modifier.weight(1f))
                            Box(Modifier.width(1.dp).height(34.dp).background(CourtQuestDesign.border))
                            ProfileStat("GAMES", save.matches.size.toString(), Modifier.weight(1f))
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
