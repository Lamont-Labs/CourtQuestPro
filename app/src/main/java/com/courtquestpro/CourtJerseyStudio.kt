package com.courtquestpro

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private enum class StudioTool(val label: String) { STYLE("Style"), IDENTITY("Identity"), COLORS("Colors") }
private val kitOrder = listOf(JerseyTemplate.ARENA, JerseyTemplate.CLASSIC, JerseyTemplate.FASTBREAK, JerseyTemplate.RETRO)
private val designSaver = listSaver<JerseyDesign, Any>(
    save = { d -> listOf(d.template.name) + JerseyPart.entries.map(d::color) + listOf(d.branding.name, d.teamName.orEmpty()) },
    restore = { values -> JerseyPart.entries.fold(JerseyDesign(templateId = values[0] as String,
        chestBrand = (values.getOrNull(8) as? String)?.let(ChestBrand::valueOf) ?: ChestBrand.COURTQUEST,
        teamName = (values.getOrNull(9) as? String)?.ifEmpty { null })) { d, part -> d.withColor(part, values[part.ordinal + 1] as Int) } }
)
private val swatches = listOf("Onyx" to 0xFF141519, "White" to 0xFFFFFFFF, "Steel" to 0xFF9BA7B5,
    "Navy" to 0xFF172950, "Royal blue" to 0xFF2259D1, "Sky blue" to 0xFF64B5F6,
    "Crimson" to 0xFFB51733, "Orange" to 0xFFFF6F27, "Gold" to 0xFFFFD05B,
    "Forest" to 0xFF176B46, "Teal" to 0xFF14A898, "Purple" to 0xFF794CCB)

@Composable
internal fun CourtJerseyStudio(saved: PlayerJersey, onSave: (String, String, JerseyDesign) -> Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    // The Studio owns its scrollable inspector. Let the keyboard resize it without panning the entire window.
    DisposableEffect(context) {
        val window = generateSequence<Context>(context) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<Activity>().firstOrNull()?.window
        val previous = window?.attributes?.softInputMode
        if (window != null && previous != null) window.setSoftInputMode(
            (previous and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST.inv()) or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        onDispose { if (window != null && previous != null) window.setSoftInputMode(previous) }
    }
    var design by rememberSaveable(stateSaver = designSaver) { mutableStateOf(saved.design) }
    var name by rememberSaveable { mutableStateOf(saved.name) }
    var number by rememberSaveable { mutableStateOf(saved.number.orEmpty()) }
    var tool by rememberSaveable { mutableStateOf(StudioTool.STYLE) }
    var view by rememberSaveable { mutableStateOf(JerseyView.FRONT) }
    var zoom by rememberSaveable { mutableStateOf(false) }
    var part by rememberSaveable { mutableStateOf(JerseyPart.FABRIC) }
    var hexOpen by rememberSaveable { mutableStateOf(false) }
    var hex by rememberSaveable { mutableStateOf(jerseyHex(design.fabric)) }
    var savedNotice by rememberSaveable { mutableStateOf(false) }
    var discard by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val nameValid = name.isNotBlank() || saved.name.isBlank()
    val numberValid = number.isEmpty() || parseJerseyNumber(number) != null
    val teamValid = design.branding != ChestBrand.CUSTOM || design.chestText.isNotEmpty()
    val hexValid = !hexOpen || parseJerseyColor(hex) != null
    val normalizedDesign = design.copy(teamName = design.teamName?.trim()?.ifEmpty { null })
    val dirty = name.trim() != saved.name || number != saved.number.orEmpty() || normalizedDesign != saved.design
    val preview = PlayerJersey(design, parseJerseyNumber(number), name.trim())
    val fontScale = LocalDensity.current.fontScale
    val canSave = dirty && nameValid && numberValid && teamValid && hexValid
    fun saveChanges(): Boolean {
        if (!canSave) return false
        if (!onSave(name.trim(), number, normalizedDesign)) return false
        design = normalizedDesign
        savedNotice = true
        focus.clearFocus()
        return true
    }
    fun close() { if (dirty) discard = true else onBack() }
    BackHandler(enabled = dirty && !discard) { close() }
    if (discard) AlertDialog(
        onDismissRequest = { discard = false },
        title = { Text("Save jersey changes?") },
        text = { Text(if (canSave) "Save your jersey before leaving the studio." else "Your edits need a valid name, number, team text, and color before saving.") },
        confirmButton = {
            TextButton(onClick = { if (saveChanges()) { discard = false; onBack() } }, enabled = canSave,
                modifier = Modifier.testTag("jersey-save-exit")) { Text("Save & exit") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onBack, modifier = Modifier.testTag("jersey-discard")) { Text("Discard") }
                TextButton(onClick = { discard = false }, modifier = Modifier.testTag("jersey-keep-editing")) { Text("Keep editing") }
            }
        }
    )

    BoxWithConstraints(Modifier.fillMaxSize().background(CourtQuestDesign.background).imePadding().testTag("jersey-studio")) {
        val wide = maxWidth >= 600.dp && maxWidth > maxHeight
        val smallHeader = maxWidth < 400.dp && fontScale > 1.45f
        val typing = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        val previewFraction = when { typing -> .28f; fontScale > 1.45f -> .40f; else -> .61f }
        val remainingInspector = if (typing) 180.dp else 235.dp
        val stageHeight = (maxHeight * previewFraction).coerceIn(88.dp, 515.dp)
            .coerceAtMost((maxHeight - remainingInspector).coerceAtLeast(88.dp))
        Column(Modifier.fillMaxSize().padding(horizontal = if (maxWidth < 370.dp) 10.dp else 16.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(::close, Modifier.testTag("jersey-close")) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close Jersey Studio", tint = CourtQuestDesign.textPrimary) }
                Text("Jersey Studio", Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleLarge,
                    color = CourtQuestDesign.textPrimary, fontWeight = FontWeight.Bold)
                val enabled = canSave
                val saveLabel = if (savedNotice && !dirty) "Saved" else "Save"
                val action = { saveChanges(); Unit }
                val saveMod = Modifier.testTag("jersey-save").semantics {
                    contentDescription = if (saveLabel == "Saved") "Jersey saved" else "Save jersey"
                    liveRegion = LiveRegionMode.Polite
                }
                if (smallHeader) IconButton(action, saveMod.size(48.dp), enabled = enabled) {
                    Icon(if (savedNotice && !dirty) Icons.Default.Check else Icons.Default.Save, null)
                } else Button(action, saveMod.heightIn(min = 48.dp), enabled = enabled,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CourtQuestDesign.accentVivid, contentColor = Color.Black)) { Text(saveLabel) }
            }
            val stage: @Composable (Modifier) -> Unit = { mod ->
                StudioStage(preview, view, zoom, mod, onView = { view = it }, onZoom = { zoom = !zoom })
            }
            val inspector: @Composable (Modifier) -> Unit = { mod ->
                Column(mod) {
                    CourtQuestSegmentRail(StudioTool.entries.map { it.label }, tool.label, { label ->
                        focus.clearFocus(); tool = StudioTool.entries.first { it.label == label }
                        if (tool != StudioTool.COLORS) hexOpen = false
                    }, tagKeys = StudioTool.entries.map { "jersey-editor-${it.name.lowercase()}" }, reflowAtLargeText = false)
                    key(tool) {
                        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).testTag("jersey-inspector")
                            .padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            when (tool) {
                                StudioTool.STYLE -> StyleChooser(preview) {
                                    design = design.copy(templateId = it.name); savedNotice = false
                                }
                                StudioTool.IDENTITY -> {
                                    val nameField: @Composable (Modifier) -> Unit = { modName ->
                                        OutlinedTextField(name, { name = it.take(40); view = JerseyView.BACK; savedNotice = false },
                                            modName.testTag("jersey-name"), label = { Text("Player name") }, singleLine = true, isError = !nameValid,
                                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done))
                                    }
                                    val numberField: @Composable (Modifier) -> Unit = { modNumber ->
                                        OutlinedTextField(number, { number = it.take(8); savedNotice = false }, modNumber.testTag("jersey-number"),
                                            label = { Text("Number") }, singleLine = true, isError = !numberValid,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done))
                                    }
                                    if (fontScale > 1.4f) { nameField(Modifier.fillMaxWidth()); numberField(Modifier.fillMaxWidth()) }
                                    else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        nameField(Modifier.weight(1.7f)); numberField(Modifier.weight(1f))
                                    }
                                    if (!numberValid) Text("Enter one or two digits.", color = MaterialTheme.colorScheme.error)
                                    if (!nameValid) Text("Enter your player name.", color = MaterialTheme.colorScheme.error)
                                    StudioPicker("Chest lettering", design.branding.label, "jersey-brand", ChestBrand.entries,
                                        { it.label }, { "jersey-brand-${it.name.lowercase()}" }, { mode ->
                                            design = design.copy(chestBrand = mode); view = JerseyView.FRONT; savedNotice = false
                                        })
                                    if (design.branding == ChestBrand.CUSTOM) OutlinedTextField(design.teamName.orEmpty(), {
                                        design = design.copy(teamName = it.take(32)); view = JerseyView.FRONT; savedNotice = false
                                    }, Modifier.fillMaxWidth().testTag("jersey-team"), label = { Text("Custom chest text") }, singleLine = true,
                                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done))
                                }
                                StudioTool.COLORS -> {
                                    StudioPicker("Color zone", part.label, "jersey-part-picker", JerseyPart.entries,
                                        { it.label }, { "jersey-part-${it.name.lowercase()}" }, { zone ->
                                            part = zone; hex = jerseyHex(design.color(zone)); hexOpen = false
                                            if (zone == JerseyPart.NAME) view = JerseyView.BACK
                                        }, ink = { zone -> design.color(zone) })
                                    Palette(design.color(part)) { ink ->
                                        design = design.withColor(part, ink); hex = jerseyHex(ink); savedNotice = false
                                    }
                                    TextButton({ hexOpen = !hexOpen; hex = jerseyHex(design.color(part)) }, Modifier.testTag("jersey-edit-hex")) {
                                        Text(if (hexOpen) "Close hex editor" else "Edit ${jerseyHex(design.color(part))}")
                                    }
                                    if (hexOpen) OutlinedTextField(hex, { text ->
                                        hex = text.take(12); parseJerseyColor(hex)?.let { design = design.withColor(part, it); savedNotice = false }
                                    }, Modifier.fillMaxWidth().testTag("jersey-hex"), label = { Text("Hex color") }, singleLine = true,
                                        isError = !hexValid, supportingText = if (!hexValid) ({ Text("Use six hex digits.") }) else null,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done))
                                }
                            }
                        }
                    }
                }
            }
            if (wide) Row(Modifier.fillMaxSize().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                stage(Modifier.weight(1.35f).fillMaxHeight()); inspector(Modifier.weight(1f).fillMaxHeight())
            } else {
                stage(Modifier.fillMaxWidth().height(stageHeight)); Spacer(Modifier.height(6.dp)); inspector(Modifier.fillMaxWidth().weight(1f))
            }
        }
    }
}

@Composable
private fun StudioStage(jersey: PlayerJersey, view: JerseyView, zoom: Boolean, modifier: Modifier,
    onView: (JerseyView) -> Unit, onZoom: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    BoxWithConstraints(modifier.clip(shape).border(1.dp, CourtQuestDesign.border, shape)) {
        Image(painterResource(R.drawable.jersey_studio_arena), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .20f), Color.Transparent,
            Color.Black.copy(alpha = .35f)))))
        val compact = maxHeight < 175.dp
        Crossfade(view, modifier = Modifier.fillMaxSize().padding(top = 7.dp, bottom = if (compact) 3.dp else 50.dp)
            .testTag("jersey-preview"), animationSpec = tween(140), label = "Uniform view") { side ->
            CourtJerseyArtwork(jersey, Modifier.fillMaxSize(), side, describe = true, zoom = zoom)
        }
        IconButton(onZoom, Modifier.align(Alignment.TopEnd).size(48.dp).testTag("jersey-zoom")) {
            Icon(if (zoom) Icons.Default.ZoomOut else Icons.Default.ZoomIn, if (zoom) "Fit whole jersey" else "Inspect fabric", tint = Color.White)
        }
        val position = if (compact) Alignment.BottomStart else Alignment.BottomCenter
        Row(Modifier.align(position).padding(horizontal = 4.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(24.dp)).background(Color(0xEC141517)).selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
            JerseyView.entries.forEach { side ->
                val active = side == view
                Box(Modifier.heightIn(min = 48.dp).widthIn(min = 55.dp)
                    .selectable(active, role = Role.Tab, onClick = { onView(side) }).testTag("segment-${side.name.lowercase()}")
                    .padding(horizontal = 7.dp), contentAlignment = Alignment.Center) {
                    Text(side.label, color = if (active) CourtQuestDesign.accent else CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.labelLarge, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StyleChooser(preview: PlayerJersey, select: (JerseyTemplate) -> Unit) {
    val columns = if (LocalDensity.current.fontScale > 1.3f) 2 else 4
    FlowRow(Modifier.fillMaxWidth().selectableGroup(), maxItemsInEachRow = columns,
        horizontalArrangement = Arrangement.spacedBy(5.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        kitOrder.forEach { template ->
            val selected = template == preview.design.template
            val shape = RoundedCornerShape(10.dp)
            Column(Modifier.weight(1f).clip(shape).background(CourtQuestDesign.surface)
                .border(if (selected) 1.5.dp else 1.dp, if (selected) CourtQuestDesign.accent else CourtQuestDesign.border, shape)
                .selectable(selected, role = Role.RadioButton, onClick = { select(template) }).testTag("segment-${template.name.lowercase()}")
                .padding(horizontal = 2.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CourtJerseyArtwork(preview.copy(design = preview.design.copy(templateId = template.name)), Modifier.fillMaxWidth().height(57.dp), compactMark = true)
                Text(template.label, style = MaterialTheme.typography.labelMedium, color = CourtQuestDesign.textPrimary)
            }
        }
    }
    Text(preview.design.template.detail, style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
}

@Composable
private fun <T> StudioPicker(label: String, current: String, tag: String, values: List<T>, title: (T) -> String,
    itemTag: (T) -> String, select: (T) -> Unit, ink: ((T) -> Int)? = null) {
    var open by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton({ open = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag(tag), shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = CourtQuestDesign.textSecondary)
                Text(current, style = MaterialTheme.typography.titleSmall, color = CourtQuestDesign.textPrimary)
            }
            Icon(Icons.Default.ExpandMore, null)
        }
        DropdownMenu(open, { open = false }) {
            values.forEach { item -> DropdownMenuItem(text = { Text(title(item)) }, onClick = { select(item); open = false },
                leadingIcon = ink?.let { get -> { ColorDot(get(item), Modifier.size(21.dp)) } },
                trailingIcon = if (title(item) == current) ({ Icon(Icons.Default.Check, "Selected") }) else null,
                modifier = Modifier.testTag(itemTag(item))) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Palette(current: Int, select: (Int) -> Unit) {
    FlowRow(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        swatches.forEach { (name, rgb) ->
            val value = rgb.toInt(); val selected = value == current
            Box(Modifier.size(48.dp).clip(CircleShape).selectable(selected, role = Role.RadioButton, onClick = { select(value) })
                .semantics { contentDescription = "$name, ${jerseyHex(value)}" }.testTag("jersey-swatch-${name.lowercase().replace(' ', '-')}"),
                contentAlignment = Alignment.Center) {
                ColorDot(value, Modifier.size(32.dp))
                if (selected) Icon(Icons.Default.Check, null, Modifier.size(18.dp),
                    tint = if (jerseyContrast(value, 0xFF000000.toInt()) < 4.5) Color.White else Color.Black)
            }
        }
    }
}

@Composable private fun ColorDot(ink: Int, modifier: Modifier) {
    Box(modifier.clip(CircleShape).background(Color(ink)).border(1.dp, CourtQuestDesign.controlOutline, CircleShape))
}
