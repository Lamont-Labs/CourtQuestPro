package com.courtquestpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Same five destinations, with Court at the center. Hit areas never overlap the medallion. */
@Composable
internal fun CourtQuestBottomDock(selected: PlayerTab, jersey: PlayerJersey, customizedJersey: Boolean, onSelect: (PlayerTab) -> Unit, spotlightTab: PlayerTab? = null) {
    val atlas = rememberNavigationAtlas()
    val showLabels = LocalDensity.current.fontScale <= 1.3f
    val order = listOf(PlayerTab.TRAIN, PlayerTab.PROGRESS, PlayerTab.COURT, PlayerTab.QUESTS, PlayerTab.ME)
    val shape = RoundedCornerShape(21.dp)
    Box(Modifier.fillMaxWidth().background(CourtQuestDesign.background).navigationBarsPadding()
        .padding(horizontal = 8.dp, vertical = 4.dp)) {
        Row(
            Modifier.fillMaxWidth().height(58.dp).clip(shape)
                .background(Brush.verticalGradient(listOf(Color(0xFF222223), Color(0xFF09090A))))
                .border(.8.dp, Color.White.copy(alpha = .19f), shape)
                .padding(horizontal = 3.dp).selectableGroup().testTag("player-bottom-dock"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            order.forEach { tab ->
                val active = selected == tab
                Box(
                    Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            if (spotlightTab == tab) 1.5.dp else 0.dp,
                            if (spotlightTab == tab) CourtQuestDesign.accentVivid else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        )
                        .selectable(selected = active, role = Role.Tab, onClick = { onSelect(tab) })
                        .semantics { contentDescription = tab.label }
                        .testTag("dock-${tab.route}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(Modifier.clearAndSetSemantics { },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                    Box(contentAlignment = Alignment.Center) {
                    if (tab == PlayerTab.ME && customizedJersey) {
                        CourtJerseyArtwork(jersey, Modifier.size(34.dp))
                    } else if (atlas != null) {
                        // Retain the original artwork, sized for a low-profile labeled dock.
                        NavigationSpriteArtwork(
                            atlas, tab.ordinal,
                            Modifier.size(if (tab == PlayerTab.COURT) 42.dp else 34.dp)
                        )
                    } else if (tab == PlayerTab.COURT) {
                        CourtBasketballMedallion(Modifier.size(40.dp), active)
                    } else {
                        val fallback = when (tab) {
                            PlayerTab.TRAIN -> Icons.Outlined.FitnessCenter
                            PlayerTab.PROGRESS -> Icons.Outlined.Insights
                            PlayerTab.QUESTS -> Icons.Outlined.Shield
                            PlayerTab.ME -> Icons.Outlined.Person
                            PlayerTab.COURT -> Icons.Outlined.SportsBasketball
                        }
                        Icon(fallback, contentDescription = null, modifier = Modifier.size(30.dp),
                            tint = if (active) CourtQuestDesign.accent else CourtQuestDesign.textSecondary)
                    }
                    if (tab == PlayerTab.ME && !customizedJersey && jersey.number != null) {
                        Text(
                            jersey.number,
                            modifier = Modifier.align(Alignment.Center).offset(y = (-3).dp),
                            color = CourtQuestDesign.accent,
                            fontFamily = CourtDisplay,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            lineHeight = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                    }
                    if (showLabels) Text(tab.label,
                        color = if (active) CourtQuestDesign.textPrimary else CourtQuestDesign.textSecondary,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp, lineHeight = 12.sp, maxLines = 1)
                    }
                    if (active) {
                        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp)
                            .width(22.dp).height(2.dp).background(CourtQuestDesign.accent, CircleShape))
                    }
                }
            }
        }
    }
}

/** Original native vector basketball, never a raster replacement for the approved CQ artwork. */
@Composable
internal fun CourtBasketballMedallion(modifier: Modifier = Modifier, selected: Boolean = false) {
    Canvas(modifier.shadow(if(selected) 10.dp else 4.dp, CircleShape,
        spotColor = CourtQuestDesign.accentVivid)) {
        val center = Offset(size.width / 2, size.height / 2)
        val r = size.minDimension * .45f
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFFA447), CourtQuestDesign.accentVivid, Color(0xFF9C2B03)),
            center = Offset(size.width * .32f,size.height * .22f), radius = size.width), r, center)
        drawCircle(Color(0xFFFFBC77),r,center,style=Stroke(1.dp.toPx()))
        val ink = Color(0xFF361204)
        val seam = 1.5.dp.toPx()
        drawLine(ink, Offset(center.x-r, center.y),Offset(center.x+r,center.y),seam)
        drawLine(ink,Offset(center.x,center.y-r),Offset(center.x,center.y+r),seam)
        drawArc(ink,-90f,180f,false,Offset(center.x-r*1.6f,center.y-r),Size(r*1.2f,r*2),style=Stroke(seam))
        drawArc(ink,90f,180f,false,Offset(center.x+r*.4f,center.y-r),Size(r*1.2f,r*2),style=Stroke(seam))
    }
}
