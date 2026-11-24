package com.courtquestpro

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Floating guide. The parent supplies the usable viewport above the dock. */
@Composable
internal fun CourtNavigationTour(index: Int, onNext: () -> Unit, onSkip: () -> Unit, onBack: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val last = index == SoloNavigationTour.tabs.lastIndex

    BoxWithConstraints(
        Modifier.widthIn(max = 392.dp).fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val compactControls = maxWidth < 292.dp || LocalDensity.current.fontScale > 1.5f
        Column(
            Modifier.fillMaxWidth().testTag("navigation-tour")
                .shadow(10.dp, shape).clip(shape)
                .background(Brush.verticalGradient(listOf(Color(0xED17181A), Color(0xE609090A))))
                .drawBehind {
                    drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = .07f), Color.Transparent)),
                        size = androidx.compose.ui.geometry.Size(size.width, 24.dp.toPx().coerceAtMost(size.height)))
                }
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(
                        Color.White.copy(alpha = .28f),
                        CourtQuestDesign.accentVivid.copy(alpha = .42f),
                        Color.White.copy(alpha = .12f)
                    )),
                    shape
                )
                .padding(start = 12.dp, end = 12.dp, top = 9.dp, bottom = 2.dp)
        ) {
            Row(Modifier.fillMaxWidth().semantics {
                contentDescription = "Walkthrough step ${index + 1} of ${SoloNavigationTour.tabs.size}"
            }, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SoloNavigationTour.tabs.indices.forEach { position ->
                    val color by animateColorAsState(
                        targetValue = when {
                            position == index -> CourtQuestDesign.accentVivid
                            position < index -> CourtQuestDesign.accent.copy(alpha = .54f)
                            else -> Color.White.copy(alpha = .16f)
                        },
                        animationSpec = tween(150),
                        label = "tour-progress"
                    )
                    Box(Modifier.weight(1f).height(3.dp).background(color, CircleShape))
                }
            }
            key(index) {
                val entered = remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { entered.value = true }
                val copyAlpha by animateFloatAsState(if (entered.value) 1f else .65f, tween(160), label = "tour-copy")
                // Only the copy scrolls when text or the viewport grows tight.
                Column(
                    Modifier.fillMaxWidth().weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()).graphicsLayer { alpha = copyAlpha }.padding(top = 7.dp, bottom = 2.dp)
                        .testTag("tour-current-step").semantics { liveRegion = LiveRegionMode.Polite },
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            SoloNavigationTour.headings[index],
                            Modifier.weight(1f).semantics { heading() },
                            color = CourtQuestDesign.textPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${index + 1} / ${SoloNavigationTour.tabs.size}",
                            Modifier.semantics {
                                contentDescription = "Step ${index + 1} of ${SoloNavigationTour.tabs.size}"
                            },
                            color = CourtQuestDesign.accent,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Text(
                        SoloNavigationTour.cues[index],
                        color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).testTag("tour-skip"),
                    colors = ButtonDefaults.textButtonColors(contentColor = CourtQuestDesign.textSecondary),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) { Text("Skip", softWrap = false) }
                Spacer(Modifier.weight(1f))
                if (index > 0) {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).testTag("tour-back"),
                        colors = ButtonDefaults.textButtonColors(contentColor = CourtQuestDesign.textPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        if (compactControls) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Back", softWrap = false)
                        }
                    }
                }
                Button(
                    onClick = onNext,
                    modifier = Modifier.sizeIn(minWidth = 78.dp, minHeight = 48.dp).testTag("tour-next"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CourtQuestDesign.accentVivid,
                        contentColor = CourtQuestDesign.iconForeground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(if (last) "Finish" else "Next", softWrap = false, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
