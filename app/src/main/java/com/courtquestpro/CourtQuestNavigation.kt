package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun CourtQuestNavigation(selected: PlayerTab, onSelect: (PlayerTab) -> Unit) {
    val config = LocalConfiguration.current
    // At large text sizes every label remains visible, with full native selection semantics.
    val rows = if (config.fontScale >= 1.5f) PlayerTab.entries.chunked(3) else listOf(PlayerTab.entries.toList())
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
        CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(0.dp), blurred = true) {
            Column {
                rows.forEach { row ->
                    NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0, 0, 0, 0)) {
                        row.forEach { tab ->
                            NavigationBarItem(selected == tab, { onSelect(tab) },
                                icon = { Icon(when (tab) {
                                    PlayerTab.COURT -> Icons.Default.SportsBasketball
                                    PlayerTab.TRAIN -> Icons.Default.FitnessCenter
                                    PlayerTab.QUESTS -> Icons.Default.Flag
                                    PlayerTab.PROGRESS -> Icons.Default.Insights
                                    PlayerTab.ME -> Icons.Default.Person
                                }, null) },
                                label = { Text(tab.label,
                                    fontFamily = if (config.fontScale >= 1.2f || config.screenWidthDp < 360) CourtDisplay else CourtBody,
                                    fontWeight = FontWeight.Bold, fontSize = 12.sp, softWrap = false, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CourtQuestDesign.iconForeground,
                                    selectedTextColor = CourtQuestDesign.accent,
                                    indicatorColor = CourtQuestDesign.accentVivid,
                                    unselectedIconColor = CourtQuestDesign.textSecondary,
                                    unselectedTextColor = CourtQuestDesign.textSecondary))
                        }
                    }
                }
            }
        }
    }
}
