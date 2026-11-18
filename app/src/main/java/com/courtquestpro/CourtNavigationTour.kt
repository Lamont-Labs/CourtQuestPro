package com.courtquestpro

import androidx.compose.animation.animateColorAsState
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
    val shape = RoundedCornerShape(18.dp)
    val tab = SoloNavigationTour.tabs[index]
    val last = index == SoloNavigationTour.tabs.lastIndex

    BoxWithConstraints(
        Modifier.widthIn(max = 392.dp).fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val compactControls = maxWidth < 292.dp || LocalDensity.current.fontScale > 1.5f
        Column(
            Modifier.fillMaxWidth().testTag("navigation-tour").clip(shape)
                .background(Brush.verticalGradient(listOf(Color(0xEC211C1A), Color(0xE80E0E10))))
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(
                        CourtQuestDesign.accentVivid.copy(alpha = .68f),
                        CourtQuestDesign.accent.copy(alpha = .26f),
                        Color.White.copy(alpha = .18f)
                    )),
                    shape
                )
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 4.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    Box(Modifier.weight(1f).height(2.dp).background(color, CircleShape))
                }
            }
            key(index) {
                // Only the copy scrolls when text or the viewport grows tight.
                Column(
                    Modifier.fillMaxWidth().weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()).padding(top = 7.dp, bottom = 2.dp)
                        .testTag("tour-current-step").semantics { liveRegion = LiveRegionMode.Polite },
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            tab.label,
                            Modifier.weight(1f).semantics { heading() },
                            color = CourtQuestDesign.textPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
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
                        style = MaterialTheme.typography.bodyMedium
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
                    contentPadding = PaddingValues(horizontal = 15.dp)
                ) {
                    Text(if (last) "Finish" else "Next", softWrap = false, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
