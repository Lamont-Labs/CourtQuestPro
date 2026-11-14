package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** A compact studio switcher: selected state is carried by a warm glass well and an orange rule. */
@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun CourtQuestSegmentRail(
    labels: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val railShape = RoundedCornerShape(13.dp)
    FlowRow(
        modifier.fillMaxWidth()
            .clip(railShape)
            .background(CourtQuestDesign.surface.copy(alpha = 0.88f))
            .border(1.dp, CourtQuestDesign.border.copy(alpha = 0.88f), railShape)
            .selectableGroup()
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        maxItemsInEachRow = if (LocalDensity.current.fontScale > 1.3f) 1 else labels.size
    ) {
        labels.forEach { label ->
            val active = label == selected
            val itemShape = RoundedCornerShape(10.dp)
            Box(
                Modifier.weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("segment-${label.lowercase()}")
                    .clip(itemShape)
                    .background(if (active) CourtQuestDesign.warmSurface else CourtQuestDesign.background.copy(alpha = 0.22f))
                    .selectable(selected = active, role = Role.Tab, onClick = { onSelect(label) }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 8.dp),
                    color = if (active) CourtQuestDesign.textPrimary else CourtQuestDesign.textSecondary,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
                if (active) {
                    Box(
                        Modifier.align(Alignment.BottomCenter)
                            .padding(bottom = 2.dp)
                            .fillMaxWidth(0.25f)
                            .heightIn(min = 2.dp)
                            .background(CourtQuestDesign.accentVivid, RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}
