package com.courtquestpro
import androidx.activity.compose.BackHandler

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/** All content is bundled locally; opening these pages never changes saved training evidence. */
internal enum class MeSettingsPage(val title: String, val subtitle: String) {
    HOME("Settings", "Your player and app"),
    PLAYER("Player Settings", "Identity and training preferences"),
    PREFERENCES("App Preferences", "Display and accessibility"),
    HELP("Help", "Answers and troubleshooting"),
    HOW_TO("How to Use", "Practical instructions"),
    ABOUT("About CourtQuest Pro", "App information"),
    NOTICES("Disclaimers & Disclosures", "Safety and data information")
}

@Composable
internal fun CourtMeSettingsHub(
    save: SaveState,
    routineDays: List<String>,
    onEdit: () -> Unit,
    onWalkthrough: () -> Unit
) {
    var pageName by rememberSaveable { mutableStateOf(MeSettingsPage.HOME.name) }
    val page = MeSettingsPage.entries.firstOrNull { it.name == pageName } ?: MeSettingsPage.HOME
    BackHandler(enabled=page!=MeSettingsPage.HOME) {pageName=MeSettingsPage.HOME.name}
    CourtQuestPremiumSurface(Modifier.fillMaxWidth().testTag("me-settings-hub"), PaddingValues(18.dp), highlighted = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (page != MeSettingsPage.HOME) {
                TextButton(onClick = { pageName = MeSettingsPage.HOME.name },
                    modifier = Modifier.testTag("me-settings-back")) {
                    Icon(Icons.Default.ArrowBack, null)
                    Spacer(Modifier.width(8.dp))
                    Text("All settings")
                }
            }
            CourtEyebrow(page.title.uppercase())
            Text(page.subtitle, color = CourtQuestDesign.textSecondary,
                style = MaterialTheme.typography.bodyMedium)
            CourtRule()
            when (page) {
                MeSettingsPage.HOME -> {
                    SettingsHubRow("Player Settings", "Name, jersey, skill level and routine", "player") { pageName = MeSettingsPage.PLAYER.name }
                    SettingsHubRow("App Preferences", "Display and accessibility guidance", "preferences") { pageName = MeSettingsPage.PREFERENCES.name }
                    SettingsHubRow("Help", "Answers and troubleshooting", "help") { pageName = MeSettingsPage.HELP.name }
                    SettingsHubRow("How to Use", "Learn the training flow", "how-to") { pageName = MeSettingsPage.HOW_TO.name }
                    SettingsHubRow("Walkthrough Wizard", "Replay the guided five-destination tour", "walkthrough") { onWalkthrough() }
                    SettingsHubRow("About CourtQuest Pro", "What the app does", "about") { pageName = MeSettingsPage.ABOUT.name }
                    SettingsHubRow("Disclaimers & Disclosures", "Training safety and data handling", "notices") { pageName = MeSettingsPage.NOTICES.name }
                }
                MeSettingsPage.PLAYER -> {
                    SettingsCopy("Training routine", if (routineDays.isEmpty()) "No days selected." else routineDays.joinToString(" · "))
                    SettingsCopy("Current training preferences", "${save.difficulty} level · ${save.weeklyPlan} focus")
                    OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                        Text("Edit player settings")
                    }
                }
                MeSettingsPage.PREFERENCES -> {
                    SettingsCopy("Display", "CourtQuest Pro uses a dark interface with orange highlights. Your device's font-size and display-size settings can improve readability.")
                    SettingsCopy("Accessibility", "Use Android accessibility settings for larger text, screen reading, and interaction assistance. Some screens may require scrolling at larger text sizes.")
                    SettingsCopy("Notifications", "This screen does not enable or change system notifications.")
                }
                MeSettingsPage.HELP -> {
                    SettingsCopy("Where do I start?", "Open Court to start or resume a training session.")
                    SettingsCopy("How do I find drills?", "Open Train, browse or filter the library, and select a drill to view its instructions.")
                    SettingsCopy("Where are my results?", "Save your completed drill results, then open Progress to review recorded training.")
                    SettingsCopy("Can I add drills?", "Use the custom-drill controls in Train to create your own exercises.")
                    SettingsCopy("I cannot find a result", "Check that you completed the save action. Leaving an unfinished session does not count as a completed result.")
                    SettingsCopy("Will replaying the tour reset progress?", "No. The walkthrough changes navigation guidance only.")
                }
                MeSettingsPage.HOW_TO -> {
                    SettingsCopy("1 · Set up", "Use Me → Profile → Edit profile to enter your name, jersey number and level.")
                    SettingsCopy("2 · Explore Court", "Find your current training entry point and start or resume a session.")
                    SettingsCopy("3 · Choose a drill", "Open Train, use categories and filters, and read the visual reference and coaching tips.")
                    SettingsCopy("4 · Record training", "Enter your actual attempts and results, then save the completed drill once.")
                    SettingsCopy("5 · Review progress", "Use Progress for saved sessions, XP and training history; Quests shows personal challenges.")
                    SettingsCopy("6 · Replay the tour", "Return to Me → Settings → Walkthrough Wizard at any time.")
                    OutlinedButton(onClick = onWalkthrough, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("me-settings-start-walkthrough")) {
                        Text("Start walkthrough")
                    }
                }
                MeSettingsPage.ABOUT -> {
                    SettingsCopy("CourtQuest Pro", "A basketball practice companion for individual players, with drills, guided sessions, progress and personal challenges.")
                    SettingsCopy("Works without a live training connection", "The core drill library and saved training recommendations are designed for on-device use.")
                    SettingsCopy("Version", "Check Android Settings → Apps → CourtQuest Pro for the installed app version.")
                }
                MeSettingsPage.NOTICES -> {
                    SettingsCopy("Training safety", "Warm up, practice in a safe space, use suitable equipment and stop if you feel pain, dizziness or unusual symptoms. Adapt drills to your abilities and seek qualified guidance when needed.")
                    SettingsCopy("Not medical advice", "Training suggestions are practice aids, not medical advice or a guarantee of athletic performance.")
                    SettingsCopy("Recorded results", "The app uses saved training results to display progress and may use eligible records to explain drill recommendations. Missing or insufficient evidence is not proof of ability.")
                    SettingsCopy("Local saves", "Player profile and training information are saved in app storage on your device. Uninstalling the app or clearing its storage may erase that information.")
                    SettingsCopy("Data sharing", "This page does not assert that all data stays on-device or that no third-party services receive information. Network, analytics and service behavior must be reviewed before making a complete privacy disclosure.")
                }
            }
        }
    }
}

@Composable
private fun SettingsHubRow(title: String, subtitle: String, tag: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("me-settings-$tag"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

@Composable
private fun SettingsCopy(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = CourtQuestDesign.accent, style = MaterialTheme.typography.titleMedium)
        Text(body, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.bodyMedium)
        CourtRule()
    }
}
