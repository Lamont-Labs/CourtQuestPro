package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp

/** Large text keeps every option readable without three permanent rows above the chart. */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
internal fun CourtProgressSectionPicker(selected: String, onSelect: (String) -> Unit) {
    val choices = listOf("Overview", "Skills", "History")
    if (LocalDensity.current.fontScale <= 1.3f) {
        CourtQuestSegmentRail(choices, selected, onSelect, Modifier.padding(horizontal = 20.dp))
        return
    }
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        OutlinedButton({ expanded = true },
            Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("progress-section-picker")) {
            Text("View: $selected", Modifier.weight(1f), color = CourtQuestDesign.textPrimary)
            Icon(Icons.Default.ExpandMore, "Change Progress view", tint = CourtQuestDesign.accent)
        }
        DropdownMenu(expanded, { expanded = false },
            modifier = Modifier.widthIn(min = 240.dp).semantics { testTagsAsResourceId = true }) {
            choices.forEach { label ->
                DropdownMenuItem(text = { Text(label) },
                    onClick = { expanded = false; onSelect(label) },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("segment-${label.lowercase()}"))
            }
        }
    }
}
