package com.courtquestpro

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Floating left-edge navigation. The slim visible pull tab has a 48dp touch target.
 * Does not consume layout height or replace the five existing PlayerTab routes.
 */
@Composable
internal fun CourtQuestNavigation(selected: PlayerTab, onSelect: (PlayerTab) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    BackHandler(expanded) { expanded = false }
    val orange = CourtQuestDesign.accentVivid
    val shape = RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)
    Box(Modifier.fillMaxSize().testTag("player-navigation-overlay")) {
        if (expanded) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.38f))
                    .clickable(onClickLabel = "Close navigation") { expanded = false }
                    .testTag("nav-dismiss")
            )
        }
        Row(Modifier.align(Alignment.CenterStart).systemBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
            AnimatedVisibility(
                visible = expanded,
                enter = expandHorizontally(animationSpec = tween(200), expandFrom = Alignment.Start),
                exit = shrinkHorizontally(animationSpec = tween(180), shrinkTowards = Alignment.Start)
            ) {
                Column(
                    Modifier.width(66.dp)
                        .shadow(14.dp, shape, ambientColor = orange.copy(alpha = 0.35f), spotColor = orange.copy(alpha = 0.25f))
                        .clip(shape)
                        .background(CourtQuestDesign.elevatedSurface)
                        .border(1.dp, orange.copy(alpha = 0.65f), shape)
                        .pointerInput(Unit) {
                            var dragged = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { dragged = 0f },
                                onDragEnd = { if (dragged < -35f) expanded = false }
                            ) { change, amount -> change.consume(); dragged += amount }
                        }
                        .padding(vertical = 10.dp, horizontal = 7.dp)
                        .selectableGroup()
                        .testTag("player-navigation-dock"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    PlayerTab.entries.forEach { tab ->
                        val active = tab == selected
                        val icon = when (tab) {
                            PlayerTab.COURT -> Icons.Default.SportsBasketball
                            PlayerTab.TRAIN -> Icons.Default.FitnessCenter
                            PlayerTab.QUESTS -> Icons.Default.Flag
                            PlayerTab.PROGRESS -> Icons.Default.Insights
                            PlayerTab.ME -> Icons.Default.Person
                        }
                        Box(
                            Modifier.size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (active) orange else Color.Transparent)
                                .selectable(selected = active, role = Role.Tab) {
                                    expanded = false
                                    onSelect(tab)
                                }
                                .semantics { contentDescription = tab.label }
                                .testTag("nav-${tab.route}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = if (active) CourtQuestDesign.iconForeground else CourtQuestDesign.textPrimary, modifier = Modifier.size(23.dp))
                        }
                    }
                }
            }
            Box(
                Modifier.width(48.dp).height(84.dp)
                    .clickable(onClickLabel = if (expanded) "Close navigation" else "Open navigation") { expanded = !expanded }
                    .semantics { contentDescription = if (expanded) "Close navigation" else "Open navigation" }
                    .testTag("nav-pull-tab"),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    Modifier.width(18.dp).height(68.dp)
                        .shadow(16.dp, RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp), ambientColor = orange, spotColor = orange)
                        .clip(RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp))
                        .background(orange)
                        .border(1.dp, CourtQuestDesign.accent, RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (expanded) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                        contentDescription = null, tint = CourtQuestDesign.iconForeground, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
