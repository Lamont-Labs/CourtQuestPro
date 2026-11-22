package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * This build has local individual-player saves, not an authorized team backend.
 * Do not turn an empty membership into a fictional team or expose fake join controls.
 */
@Composable
internal fun TeamScreen(onTrain: () -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().testTag("team-screen"), contentPadding = PaddingValues(bottom = 20.dp)) {
        item { AppHeader("Team", "Your next chapter. Together.", compact = true) }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CourtArenaHero(R.drawable.court_arena_hoop, Modifier.fillMaxWidth().height(216.dp * LocalDensity.current.fontScale)) {
                    Column(Modifier.align(Alignment.BottomStart).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CourtSportIcon(Icons.Outlined.Groups, Modifier.size(52.dp))
                        Text("BUILT FOR\nTHE WHOLE SQUAD.", color = CourtQuestDesign.textPrimary,
                            style = MaterialTheme.typography.headlineLarge, modifier = Modifier.semantics { heading() })
                    }
                }
                CourtQuestPremiumSurface(Modifier.fillMaxWidth().testTag("team-not-configured"), PaddingValues(18.dp),
                    highlighted = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CourtEyebrow("NO TEAM CONFIGURED")
                        Text("Your own game comes first.", color = CourtQuestDesign.textPrimary,
                            style = MaterialTheme.typography.headlineMedium)
                        Text("This build saves your individual training on this device. Team membership and shared schedules are not connected yet.",
                            color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                        CourtQuestPrimaryButton(onTrain, Modifier.fillMaxWidth()) { Text("Train on your own") }
                    }
                }
                listOf(
                    Icons.Outlined.Groups to ("Roster" to "No team members connected"),
                    Icons.Outlined.Event to ("Practices" to "No team practice schedule"),
                    Icons.Outlined.SportsBasketball to ("Games" to "No team games scheduled"),
                    Icons.Outlined.FitnessCenter to ("Team drills" to "No team assignments"),
                    Icons.Outlined.Flag to ("Team goals" to "No shared goals configured")
                ).forEach { (icon, copy) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(icon, null, Modifier.size(25.dp), tint = CourtQuestDesign.accent)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(copy.first, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(copy.second, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    CourtRule()
                }
                TextButton(onBack, Modifier.heightIn(min = 48.dp)) { Text("Back") }
            }
        }
    }
}
