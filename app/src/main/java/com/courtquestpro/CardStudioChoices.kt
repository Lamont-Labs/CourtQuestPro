package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun <T> Choices(
    title: String,
    values: List<T>,
    selected: T,
    label: (T) -> String,
    tag: (T) -> String,
    swatch: ((T) -> Int?)? = null,
    preview: (@Composable (T) -> Unit)? = null,
    select: (T) -> Unit
) {
    val railTag = values.firstOrNull()?.let { "${tag(it).substringBeforeLast('-')}-rail" }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 5.dp, top = 3.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = CourtQuestDesign.textSecondary,
                modifier = Modifier.semantics { heading() })
            Spacer(Modifier.weight(1f))
            Text("Swipe options", style = MaterialTheme.typography.labelSmall, color = CourtQuestDesign.textSecondary)
        }
        LazyRow(
            Modifier.fillMaxWidth().heightIn(min = 92.dp).selectableGroup()
                .then(railTag?.let { Modifier.testTag(it) } ?: Modifier),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 3.dp, vertical = 2.dp)
        ) {
            items(values, key = { tag(it) }) { choice ->
                val active = choice == selected
                val shape = RoundedCornerShape(11.dp)
                Column(
                    Modifier.widthIn(min = 78.dp, max = 106.dp).heightIn(min = 84.dp)
                        .clip(shape)
                        .background(if (active) CourtQuestDesign.warmSurface else CourtQuestDesign.surface, shape)
                        .border(if (active) 1.5.dp else 1.dp,
                            if (active) CourtQuestDesign.accent else CourtQuestDesign.border, shape)
                        .selectable(active, role = Role.RadioButton, onClick = { select(choice) })
                        .testTag(tag(choice))
                        .padding(horizontal = 5.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(Modifier.fillMaxWidth().height(62.dp).clearAndSetSemantics {},
                        contentAlignment = Alignment.Center) {
                        if (preview != null) preview.invoke(choice)
                        else swatch?.invoke(choice)?.let { color ->
                            Box(Modifier.size(30.dp).background(Color(color), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = .55f), CircleShape))
                        }
                        swatch?.invoke(choice)?.let { color ->
                            Box(Modifier.align(Alignment.BottomEnd).padding(2.dp).size(13.dp)
                                .background(Color(color), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = .7f), CircleShape))
                        }
                        if (active) Icon(Icons.Default.CheckCircle, "Selected",
                            Modifier.align(Alignment.TopEnd).size(17.dp), tint = CourtQuestDesign.accent)
                    }
                    Text(label(choice), style = MaterialTheme.typography.labelSmall,
                        color = if (active) CourtQuestDesign.textPrimary else CourtQuestDesign.textSecondary,
                        maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
internal fun CardStyleOptionPreview(jersey: PlayerJersey, playerName: String, style: PlayerCardStyle) {
    CourtPlayerCard(jersey, playerName, style, Modifier.height(60.dp), thumbnail = true)
}
