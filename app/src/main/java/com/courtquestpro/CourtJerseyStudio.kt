package com.courtquestpro

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private enum class StudioSection(val label: String) { IDENTITY("Identity"), STYLE("Style"), COLORS("Colors") }

private val designSaver = listSaver<JerseyDesign, Any>(
    save = { design -> listOf(design.template.name) + JerseyPart.entries.map(design::color) },
    restore = { values ->
        JerseyPart.entries.fold(JerseyDesign(templateId = values[0] as String)) { design, part ->
            design.withColor(part, values[part.ordinal + 1] as Int)
        }
    }
)

private val jerseyPalette = listOf(
    "Onyx" to 0xFF141519, "White" to 0xFFFFFFFF, "Steel" to 0xFF9BA7B5,
    "Navy" to 0xFF172950, "Royal blue" to 0xFF2259D1, "Sky blue" to 0xFF64B5F6,
    "Crimson" to 0xFFB51733, "Orange" to 0xFFFF6F27, "Gold" to 0xFFFFD05B,
    "Forest" to 0xFF176B46, "Teal" to 0xFF14A898, "Purple" to 0xFF794CCB
)

@Composable
internal fun CourtJerseyStudio(saved: PlayerJersey, onSave: (String, String, JerseyDesign) -> Boolean, onBack: () -> Unit) {
    var design by rememberSaveable(stateSaver = designSaver) { mutableStateOf(saved.design) }
    var view by rememberSaveable { mutableStateOf(JerseyView.FRONT) }
    var section by rememberSaveable { mutableStateOf(StudioSection.IDENTITY) }
    var partsOpen by remember { mutableStateOf(false) }
    var part by rememberSaveable { mutableStateOf(JerseyPart.FABRIC) }
    var name by rememberSaveable { mutableStateOf(saved.name) }
    var number by rememberSaveable { mutableStateOf(saved.number.orEmpty()) }
    var hex by rememberSaveable { mutableStateOf(jerseyHex(saved.design.fabric)) }
    var showHex by rememberSaveable { mutableStateOf(false) }
    var savedNotice by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val numberValid = number.isEmpty() || parseJerseyNumber(number) != null
    val nameValid = name.isNotBlank() || saved.name.isBlank()
    val hexValid = section != StudioSection.COLORS || !showHex || parseJerseyColor(hex) != null
    val dirty = number != saved.number.orEmpty() || name.trim() != saved.name || design != saved.design
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    val preview = PlayerJersey(design, parseJerseyNumber(number), name.trim())

    fun close() {
        if (dirty) confirmDiscard = true else onBack()
    }
    BackHandler(enabled = dirty && !confirmDiscard) { close() }
    if (confirmDiscard) AlertDialog(
        onDismissRequest = { confirmDiscard = false },
        title = { Text("Discard jersey changes?") },
        confirmButton = { TextButton(onBack) { Text("Discard changes") } },
        dismissButton = { TextButton({ confirmDiscard = false }) { Text("Keep editing") } }
    )

    Box(Modifier.fillMaxSize().testTag("jersey-studio"), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxSize().imePadding()) {
            Column(Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 16.dp).testTag("jersey-editor"),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(::close, Modifier.size(48.dp).testTag("jersey-close")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                    Text("Jersey Studio", Modifier.weight(1f).semantics { heading() },
                        color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineMedium)
                }
                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(12.dp), highlighted = true) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().height(202.dp).background(
                            Brush.radialGradient(listOf(CourtQuestDesign.warmSurface, Color.Transparent))
                        ), contentAlignment = Alignment.Center) {
                            // The visible preview has one accessible description during animation.
                            Crossfade(view, modifier = Modifier.size(198.dp).testTag("jersey-preview").clearAndSetSemantics {
                                contentDescription = listOfNotNull(
                                    "${view.label} of ${design.template.label} jersey",
                                    preview.number?.let { "number $it" },
                                    preview.name.takeIf { view == JerseyView.BACK && it.isNotBlank() }
                                ).joinToString(", ")
                            }, label = "jersey-side") { side ->
                                CourtJerseyArtwork(preview, Modifier.fillMaxSize(), side)
                            }
                        }
                        CourtQuestSegmentRail(JerseyView.entries.map { it.label }, view.label, { label ->
                            view = JerseyView.entries.first { it.label == label }
                        })
                    }
                }
                CourtQuestSegmentRail(StudioSection.entries.map { it.label }, section.label, { label ->
                    focus.clearFocus()
                    section = StudioSection.entries.first { it.label == label }
                    scope.launch { scroll.scrollTo(0) }
                }, tagKeys = StudioSection.entries.map { "jersey-editor-${it.name.lowercase()}" })
                when (section) {
                    StudioSection.IDENTITY -> {
                        OutlinedTextField(name, {
                            name = it.take(40)
                            view = JerseyView.BACK
                            savedNotice = false
                        }, label = { Text("Player name") }, singleLine = true, isError = !nameValid,
                            supportingText = if (!nameValid) ({ Text("Enter your player name.") }) else null,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth().testTag("jersey-name"))
                        OutlinedTextField(number, {
                            number = it.take(8)
                            savedNotice = false
                        }, label = { Text("Jersey number") }, supportingText = {
                            Text(if (numberValid) "0–99 or 00" else "Enter 0–99 or 00.")
                        }, singleLine = true, isError = !numberValid,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth().testTag("jersey-number"))
                    }
                    StudioSection.STYLE -> {
                        CourtQuestSegmentRail(JerseyTemplate.entries.map { it.label }, design.template.label, { label ->
                            design = design.copy(templateId = JerseyTemplate.entries.first { it.label == label }.name)
                            savedNotice = false
                        })
                        Text(design.template.detail, color = CourtQuestDesign.textSecondary,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    StudioSection.COLORS -> {
                        Box(Modifier.fillMaxWidth()) {
                            OutlinedButton({ partsOpen = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                .testTag("jersey-part-picker")) {
                                JerseyColorDot(design.color(part), Modifier.size(21.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(part.label, Modifier.weight(1f))
                                Icon(Icons.Default.ExpandMore, null)
                            }
                            DropdownMenu(partsOpen, { partsOpen = false }) {
                                JerseyPart.entries.forEach { choice ->
                                    DropdownMenuItem(
                                        text = { Text(choice.label) },
                                        leadingIcon = { JerseyColorDot(design.color(choice), Modifier.size(20.dp)) },
                                        trailingIcon = if (choice == part) ({ Icon(Icons.Default.Check, "Selected") }) else null,
                                        onClick = {
                                            part = choice
                                            hex = jerseyHex(design.color(choice))
                                            if (choice == JerseyPart.NAME) view = JerseyView.BACK
                                            partsOpen = false
                                        },
                                        modifier = Modifier.testTag("jersey-part-${choice.name.lowercase()}")
                                    )
                                }
                            }
                        }
                        JerseyPalette(design.color(part)) { ink ->
                            design = design.withColor(part, ink)
                            hex = jerseyHex(ink)
                            savedNotice = false
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(jerseyHex(design.color(part)), Modifier.weight(1f),
                                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.labelLarge)
                            TextButton({ showHex = !showHex; hex = jerseyHex(design.color(part)) },
                                Modifier.heightIn(min = 48.dp)) {
                                Text(if (showHex) "Hide hex color" else "Enter hex color")
                            }
                        }
                        if (showHex) OutlinedTextField(
                            value = hex,
                            onValueChange = {
                                hex = it.take(12)
                                parseJerseyColor(hex)?.let { ink ->
                                    design = design.withColor(part, ink)
                                    savedNotice = false
                                }
                            },
                            label = { Text("${part.label} hex color") },
                            supportingText = { Text(if (hexValid) "Six digits, for example #FF6F27" else "Enter six hexadecimal digits.") },
                            singleLine = true,
                            isError = !hexValid,
                            modifier = Modifier.fillMaxWidth().testTag("jersey-hex"),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Column(Modifier.fillMaxWidth().background(CourtQuestDesign.background).padding(horizontal = 16.dp, vertical = 8.dp)) {
                val lowContrast = design.lowContrastParts()
                if (lowContrast.isNotEmpty()) {
                    val labels = lowContrast.joinToString(" and ") { it.label.lowercase() }
                    Text("Low contrast for $labels. Try lighter or darker lettering.",
                        color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }.testTag("jersey-contrast"))
                }
                if (savedNotice) Text("Jersey saved", color = CourtQuestDesign.accent,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                CourtQuestPrimaryButton(
                    onClick = {
                        if (onSave(name, number, design)) {
                            savedNotice = true
                            focus.clearFocus()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("jersey-save"),
                    enabled = dirty && numberValid && nameValid && hexValid
                ) { Text("Save jersey") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JerseyPalette(color: Int, onSelect: (Int) -> Unit) {
    FlowRow(
        Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        jerseyPalette.forEach { (label, rgb) ->
            val ink = rgb.toInt()
            val selected = color == ink
            Box(
                Modifier.size(48.dp).clip(CircleShape)
                    .border(1.dp, if (selected) CourtQuestDesign.accent else Color.Transparent, CircleShape)
                    .selectable(selected, role = Role.RadioButton, onClick = { onSelect(ink) })
                    .semantics { contentDescription = "$label, ${jerseyHex(ink)}" }
                    .testTag("jersey-swatch-${label.lowercase().replace(' ', '-')}"),
                contentAlignment = Alignment.Center
            ) {
                JerseyColorDot(ink, Modifier.size(31.dp))
                if (selected) Icon(Icons.Default.Check, null, Modifier.size(16.dp),
                    tint = if (jerseyContrast(ink, 0xFF000000.toInt()) < 4.5)
                        Color.White else Color.Black)
            }
        }
    }
}

@Composable
private fun JerseyColorDot(ink: Int, modifier: Modifier) {
    Box(modifier.clip(CircleShape).background(Color(ink)).border(1.dp, CourtQuestDesign.controlOutline, CircleShape))
}
