package com.courtquestpro

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Guided Spotlight: restrained floating glass card in Scaffold's reserved area.
 * Navigation spotlight remains in CourtQuestBottomDock. No modal scrim or data mutation.
 */
@Composable
internal fun CourtNavigationTour(step: Int, onNext: () -> Unit, onSkip: () -> Unit, onBack: () -> Unit) {
    val index = step.coerceIn(SoloNavigationTour.tabs.indices)
    val shape = RoundedCornerShape(22.dp)
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 7.dp)
            .testTag("navigation-tour")
            .shadow(14.dp, shape, ambientColor = Color.Black, spotColor = CourtQuestDesign.accentVivid.copy(alpha = .18f))
            .background(
                Brush.verticalGradient(listOf(Color(0xDA262426), Color(0xBF0C0C0F))),
                shape
            )
            .border(1.dp, Brush.linearGradient(listOf(
                CourtQuestDesign.accentVivid.copy(alpha = .65f),
                Color.White.copy(alpha = .16f),
                CourtQuestDesign.accentVivid.copy(alpha = .25f)
            )), shape)
            .padding(horizontal = 17.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).background(CourtQuestDesign.accentVivid, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text("COURTQUEST  /  GUIDE", color = CourtQuestDesign.accent,
                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                maxLines = 1, modifier = Modifier.weight(1f))
            Text("${index + 1} OF ${SoloNavigationTour.tabs.size}",
                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.labelSmall)
            IconButton(onClick = onSkip, modifier = Modifier.size(40.dp).testTag("tour-skip")) {
                Icon(Icons.Default.Close, contentDescription = "Skip walkthrough",
                    tint = CourtQuestDesign.textSecondary, modifier = Modifier.size(19.dp))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            SoloNavigationTour.tabs.indices.forEach { position ->
                Box(Modifier.weight(1f).height(3.dp)
                    .background(if (position == index) CourtQuestDesign.accentVivid
                        else if (position < index) CourtQuestDesign.accent.copy(alpha = .45f)
                        else Color.White.copy(alpha = .12f), CircleShape))
            }
        }
        AnimatedContent(targetState = index, transitionSpec = {
            fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(110))
        }, label = "guided-spotlight-step") { current ->
            val tab = SoloNavigationTour.tabs[current]
            val cue = SoloNavigationTour.cues[current]
            Column(Modifier.fillMaxWidth().testTag("tour-current-step")
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = "Step ${current + 1} of ${SoloNavigationTour.tabs.size}: ${tab.label}. $cue"
                }, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(tab.label, color = CourtQuestDesign.textPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black, maxLines = 1)
                Text(cue, color = CourtQuestDesign.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (index > 0) {
                OutlinedButton(onClick = onBack,
                    modifier = Modifier.heightIn(min = 44.dp).testTag("tour-back"),
                    contentPadding = PaddingValues(horizontal = 12.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous step",
                        modifier = Modifier.size(18.dp))
                }
            } else {
                TextButton(onClick = onSkip, modifier = Modifier.heightIn(min = 44.dp)) {
                    Text("Skip", maxLines = 1, softWrap = false)
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = onNext,
                modifier = Modifier.heightIn(min = 44.dp).testTag("tour-next"),
                colors = ButtonDefaults.buttonColors(containerColor = CourtQuestDesign.accentVivid),
                contentPadding = PaddingValues(horizontal = 19.dp)) {
                Text(if (index == SoloNavigationTour.tabs.lastIndex) "Finish" else "Next",
                    fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                Spacer(Modifier.width(7.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}
