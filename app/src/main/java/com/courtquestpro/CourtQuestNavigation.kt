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
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.alpha
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
    val shape = RoundedCornerShape(topEnd = 22.dp, bottomEnd = 22.dp)
    val ice = Color(0xFF69C9FF)
    val gold = Color(0xFFFFC36B)
    Box(Modifier.fillMaxSize().testTag("player-navigation-overlay")) {
        if (expanded) {
            Box(
                Modifier.fillMaxSize()
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
                    Modifier.width(88.dp)
                        .shadow(20.dp, shape, ambientColor = orange.copy(alpha = 0.36f), spotColor = orange.copy(alpha = 0.30f))
                        .clip(shape)
                        .background(Brush.horizontalGradient(listOf(Color(0xE90B1019), Color(0xC71B222E), Color(0xAF0C1119))))
                        .border(1.dp, Brush.verticalGradient(listOf(ice.copy(alpha = 0.75f), Color.White.copy(alpha = 0.17f), orange.copy(alpha = 0.70f))), shape)
                        .pointerInput(Unit) {
                            var dragged = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { dragged = 0f },
                                onDragEnd = { if (dragged < -35f) expanded = false }
                            ) { change, amount -> change.consume(); dragged += amount }
                        }
                        .padding(vertical = 16.dp, horizontal = 7.dp)
                        .selectableGroup()
                        .testTag("player-navigation-dock"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PlayerTab.entries.forEach { tab ->
                        val active = tab == selected
                        val icon = when (tab) {
                            PlayerTab.COURT -> Icons.Rounded.SportsBasketball
                            PlayerTab.TRAIN -> Icons.Rounded.FitnessCenter
                            PlayerTab.QUESTS -> Icons.Rounded.EmojiEvents
                            PlayerTab.PROGRESS -> Icons.Rounded.QueryStats
                            PlayerTab.ME -> Icons.Rounded.AccountCircle
                        }
                        val accent = when (tab) {
                            PlayerTab.COURT -> orange
                            PlayerTab.TRAIN -> ice
                            PlayerTab.QUESTS -> gold
                            PlayerTab.PROGRESS -> Color(0xFF83DDFF)
                            PlayerTab.ME -> Color(0xFFB6BFFF)
                        }
                        Column(
                            Modifier.width(74.dp).height(78.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.verticalGradient(listOf(accent.copy(alpha = if (active) 0.29f else 0.13f), Color(0xDA0A0E16))))
                                .border(1.dp, Brush.verticalGradient(listOf(accent.copy(alpha = if (active) 0.95f else 0.55f), accent.copy(alpha = 0.12f), accent.copy(alpha = 0.48f))), RoundedCornerShape(16.dp))
                                .selectable(selected = active, role = Role.Tab) {
                                    expanded = false
                                    onSelect(tab)
                                }
                                .semantics { contentDescription = tab.label }
                                .testTag("nav-${tab.route}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                Modifier.size(44.dp)
                                    .shadow(if (active) 12.dp else 5.dp, RoundedCornerShape(13.dp), ambientColor = accent.copy(alpha = 0.65f), spotColor = accent.copy(alpha = 0.55f))
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(Brush.linearGradient(listOf(accent.copy(alpha = if (active) 1f else 0.55f), Color(0xFF121A29), Color(0xFF080C12))))
                                    .border(1.dp, accent.copy(alpha = 0.85f), RoundedCornerShape(13.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = Color.Black.copy(alpha = 0.45f),
                                    modifier = Modifier.size(30.dp).alpha(0.35f)
                                )
                            }
                            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(27.dp).offset(y = (-35).dp))
                            Spacer(Modifier.height((-22).dp))
                            Text(
                                text = tab.label,
                                color = if (active) accent else Color(0xFFF3F7FF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            Box(
                Modifier.width(48.dp).height(84.dp)
                    .pointerInput(expanded) {
                        var dragDistance = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragDistance = 0f },
                            onDragEnd = {
                                if (dragDistance > 25f) expanded = true
                                if (dragDistance < -25f) expanded = false
                            }
                        ) { change, amount ->
                            change.consume()
                            dragDistance += amount
                        }
                    }
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
                    Icon(if (expanded) Icons.Rounded.ChevronLeft else Icons.Rounded.ChevronRight,
                        contentDescription = null, tint = CourtQuestDesign.iconForeground, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
