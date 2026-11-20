package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
internal fun MeScreen(save: SaveState, onEdit: () -> Unit, onProgress: () -> Unit, onWalkthrough: () -> Unit, onJersey: () -> Unit, onPlayerCard: () -> Unit) {
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
    BoxWithConstraints(Modifier.fillMaxSize()) {
    val wide=maxWidth>=480.dp && maxWidth>maxHeight
    val enlarged=LocalDensity.current.fontScale>1.5f
    val chromeReserve=when {wide&&enlarged->150.dp;wide->90.dp;enlarged->368.dp;else->224.dp}
    val cardHeight=(maxHeight-chromeReserve)
        .coerceIn(96.dp,if(wide)260.dp else 424.dp)
    val cardWidth=(cardHeight*.69f).coerceAtMost(292.dp)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(if(wide)Modifier.widthIn(min=72.dp,max=110.dp)else Modifier.weight(1f)) {
                    CourtEyebrow("PLAYER ROOM")
                    Text("Me", color = CourtQuestDesign.textPrimary, style = if(wide)MaterialTheme.typography.headlineSmall
                        else if(LocalDensity.current.fontScale>1.3f)MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displaySmall)
                }
                if(wide)CourtQuestSegmentRail(listOf("Profile","Settings"),section,{section=it},
                    Modifier.weight(1f).padding(horizontal=12.dp),reflowAtLargeText=false)
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
                        DropdownMenuItem(text = { Text("Jersey Studio") },
                            onClick = { settingsMenu = false; onJersey() })
                        DropdownMenuItem(text = { Text("Player Card Studio") },
                            onClick = { settingsMenu = false; onPlayerCard() })
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
        if(!wide)item {
            CourtQuestSegmentRail(
                labels = listOf("Profile", "Settings"),
                selected = section,
                onSelect = { section = it },
                modifier = Modifier.padding(horizontal = 20.dp),
                reflowAtLargeText=false
            )
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (section == "Profile") {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val identity = PlayerCardSelection.identity(context)
                    Column(Modifier.fillMaxWidth().testTag("me-player-card"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if(wide)Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(18.dp),
                                verticalAlignment=Alignment.Top) {
                                CourtPlayerCard(save.playerJersey,save.playerName,identity.style,
                                    Modifier.width(cardWidth),identity.pose,identity.face,appearance=identity.avatar)
                                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                                    // Accessible counterpart, not a second avatar/profile presentation.
                                    Text(save.playerName.ifBlank{"Your player"},color=CourtQuestDesign.textPrimary,
                                        style=MaterialTheme.typography.titleMedium)
                                    if(!enlarged)Text(identity.style.label,color=CourtQuestDesign.textSecondary,style=MaterialTheme.typography.bodyMedium)
                                    CourtQuestPrimaryButton(onPlayerCard,Modifier.fillMaxWidth().testTag("me-player-card-studio")){
                                        Text("Customize player card")
                                    }
                                }
                            } else {
                                CourtPlayerCard(save.playerJersey, save.playerName, identity.style,
                                    Modifier.widthIn(max = cardWidth).fillMaxWidth(), identity.pose, identity.face,
                                    appearance = identity.avatar)
                                CourtQuestPrimaryButton(onPlayerCard, Modifier.fillMaxWidth().testTag("me-player-card-studio")) {
                                    Text("Customize player card")
                                }
                            }
                    }
                }
                if (section == "Profile") CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(20.dp), highlighted = true) {
                    Column(Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        CourtEyebrow("YOUR DEVELOPMENT")
                        // A readable text counterpart to the collectible artwork at enlarged font scales.
                        if (!wide && LocalDensity.current.fontScale > 1.3f) Text(save.playerName.ifBlank { "Your player" },
                            color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleLarge)
                        Text("${rankFor(save.totalXp)}  ·  ${save.totalXp} XP",
                            color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineSmall)
                        CourtRankMeter(save.totalXp)
                        Text("${save.difficulty} · ${save.weeklyPlan} focus",
                            color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                        CourtQuestPrimaryButton(onJersey, Modifier.fillMaxWidth().testTag("me-jersey-studio")) {
                            Text("Customize jersey")
                        }
                        TextButton(onEdit, Modifier.heightIn(min = 48.dp)) {
                            Icon(Icons.Default.Edit, null); Spacer(Modifier.width(8.dp)); Text("Edit profile")
                        }
                        CourtRule()
                        CourtMetricStrip(listOf(
                            "Recorded drills" to validResults.size.toString(),
                            "Training days" to trainingDays.toString(),
                            "Milestones" to earnedAchievements.size.toString()
                        ))
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
}
