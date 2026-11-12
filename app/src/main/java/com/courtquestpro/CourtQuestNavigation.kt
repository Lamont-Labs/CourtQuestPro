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
internal fun CourtQuestNavigation(screen: String, onCourt: () -> Unit, onDrills: () -> Unit,
                                  onProgress: () -> Unit, onSetup: () -> Unit) {
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
        CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(0.dp), blurred = true) {
            NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0)) {
                val colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CourtQuestDesign.iconForeground,
                    selectedTextColor = CourtQuestDesign.accent,
                    indicatorColor = CourtQuestDesign.accentVivid,
                    unselectedIconColor = CourtQuestDesign.textSecondary,
                    unselectedTextColor = CourtQuestDesign.textSecondary)
                NavigationBarItem(screen == "home", onCourt,
                    icon = { Icon(Icons.Default.SportsBasketball, "Court dashboard") },
                    label = { CourtNavigationLabel("Court") }, colors = colors)
                NavigationBarItem(screen == "library", onDrills,
                    icon = { Icon(Icons.Default.FitnessCenter, "Drill library") },
                    label = { CourtNavigationLabel("Drills") }, colors = colors)
                NavigationBarItem(screen in listOf("career", "gameHistory"), onProgress,
                    icon = { Icon(Icons.Default.Insights, "Player development") },
                    label = { CourtNavigationLabel("Progress") }, colors = colors)
                NavigationBarItem(screen == "settings", onSetup,
                    icon = { Icon(Icons.Default.Tune, "Player settings") },
                    label = { CourtNavigationLabel("Setup") }, colors = colors)
            }
        }
    }
}

@Composable
private fun CourtNavigationLabel(label: String) {
    val enlarged = LocalConfiguration.current.fontScale >= 1.5f
    Text(label, modifier = Modifier.wrapContentWidth(unbounded = true),
        fontFamily = if (enlarged) CourtDisplay else CourtBody,
        fontWeight = if (enlarged) FontWeight.ExtraBold else FontWeight.Bold,
        fontSize = 12.sp, softWrap = false, maxLines = 1)
}
