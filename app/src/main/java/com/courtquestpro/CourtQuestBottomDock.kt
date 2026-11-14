package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** One row at every font size. Native hit areas stay >=48dp; system bars are separate. */
@Composable
internal fun CourtQuestBottomDock(selected: PlayerTab, onSelect: (PlayerTab) -> Unit) {
    val enlarged = LocalDensity.current.fontScale > 1.3f
    val shape = RoundedCornerShape(18.dp)
    Box(
        Modifier.fillMaxWidth().background(CourtQuestDesign.background)
            .navigationBarsPadding().padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).clip(shape)
                .background(Brush.verticalGradient(listOf(Color(0xFF1C1F24), Color(0xFF0B0D10))))
                .border(0.8.dp, Color.White.copy(alpha = 0.17f), shape)
                .padding(horizontal = 2.dp).selectableGroup().testTag("player-bottom-dock"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerTab.entries.forEach { tab ->
                val active = selected == tab
                val icon = when (tab) {
                    PlayerTab.COURT -> Icons.Outlined.SportsBasketball
                    PlayerTab.TRAIN -> Icons.Outlined.FitnessCenter
                    PlayerTab.QUESTS -> Icons.Outlined.Bolt
                    PlayerTab.PROGRESS -> Icons.Outlined.Insights
                    PlayerTab.ME -> Icons.Outlined.Checkroom
                }
                Column(
                    Modifier.weight(if (enlarged && active) 1.8f else 1f)
                        .height(48.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (active) CourtQuestDesign.accentVivid else Color.Transparent)
                        .selectable(selected = active, role = Role.Tab, onClick = { onSelect(tab) })
                        .semantics { contentDescription = tab.label }
                        .testTag("dock-${tab.route}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val ink = if (active) CourtQuestDesign.iconForeground else CourtQuestDesign.textSecondary
                    if (!enlarged || !active) Icon(icon, null, Modifier.size(21.dp), tint = ink)
                    if (!enlarged || active) Text(
                        tab.label, color = ink, fontFamily = CourtDisplay, fontWeight = FontWeight.Bold,
                        fontSize = if (enlarged) 12.sp else 11.sp,
                        lineHeight = if (enlarged) 14.sp else 13.sp, maxLines = 1
                    )
                }
            }
        }
    }
}
