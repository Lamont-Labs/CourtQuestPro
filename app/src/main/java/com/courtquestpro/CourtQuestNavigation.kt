package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A single 56dp dock, including at enlarged text sizes. System navigation insets remain separate. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CourtQuestNavigation(selected: PlayerTab, onSelect: (PlayerTab) -> Unit) {
    val labelStyle = MaterialTheme.typography.labelLarge.copy(
        fontFamily = CourtDisplay, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp)) {
        CourtQuestPremiumSurface(Modifier.fillMaxWidth().testTag("player-navigation-dock"),
            PaddingValues(0.dp), blurred = true) {
            Row(Modifier.fillMaxWidth().height(56.dp).padding(4.dp).selectableGroup(),
                verticalAlignment = Alignment.CenterVertically) {
                PlayerTab.entries.forEach { tab ->
                    val active = selected == tab
                    val slot = if (active) Modifier.weight(1f) else Modifier.width(48.dp)
                    TooltipBox(modifier = slot,
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(tab.label) } },
                        state = rememberTooltipState()) {
                        BoxWithConstraints(Modifier.fillMaxWidth().height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (active) CourtQuestDesign.accentVivid else Color.Transparent)
                            .selectable(selected = active, role = Role.Tab, onClick = { onSelect(tab) })
                            .semantics { contentDescription = tab.label }
                            .testTag("nav-${tab.route}")) {
                            val labelWidth = with(density) {
                                textMeasurer.measure(tab.label, style = labelStyle,
                                    maxLines = 1, softWrap = false).size.width.toDp()
                            }
                            // Keep the full selected name at large text; its icon is optional.
                            val showIcon = !active || labelWidth + 42.dp <= maxWidth
                            val tint = if (active) CourtQuestDesign.iconForeground else CourtQuestDesign.textSecondary
                            Row(Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically) {
                                if (showIcon) {
                                    Icon(when (tab) {
                                        PlayerTab.COURT -> Icons.Default.SportsBasketball
                                        PlayerTab.TRAIN -> Icons.Default.FitnessCenter
                                        PlayerTab.QUESTS -> Icons.Default.Flag
                                        PlayerTab.PROGRESS -> Icons.Default.Insights
                                        PlayerTab.ME -> Icons.Default.Person
                                    }, null, Modifier.size(if (active) 20.dp else 22.dp), tint = tint)
                                }
                                if (active) {
                                    if (showIcon) Spacer(Modifier.width(6.dp))
                                    Text(tab.label, style = labelStyle, color = tint, maxLines = 1,
                                        softWrap = false, overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.clearAndSetSemantics { })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
