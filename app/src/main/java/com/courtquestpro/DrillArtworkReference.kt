package com.courtquestpro

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal data class DrillArtworkComparison(
    val included: Boolean,
    val leftLabel: String?,
    val rightLabel: String?,
    val note: String?,
    val reason: String?
)

internal data class DrillArtworkManifestEntry(
    val id: String,
    val title: String,
    val assetPath: String,
    val width: Int,
    val height: Int,
    val visualSummary: String,
    val sequence: List<String>,
    val comparison: DrillArtworkComparison
)

private object DrillArtworkManifestStore {
    @Volatile private var entries: Map<String, DrillArtworkManifestEntry>? = null

    fun entry(context: Context, drillId: String): DrillArtworkManifestEntry? =
        all(context)[drillId]

    private fun all(context: Context): Map<String, DrillArtworkManifestEntry> {
        entries?.let { return it }
        return synchronized(this) {
            entries ?: runCatching {
                context.assets.open("drill_art/manifest.json").bufferedReader().use { reader ->
                    val root = JSONObject(reader.readText())
                    val drills = root.getJSONArray("drills")
                    buildMap {
                        for (index in 0 until drills.length()) {
                            val item = drills.getJSONObject(index)
                            val sequenceArray = item.getJSONArray("sequence")
                            val sequence = List(sequenceArray.length()) { sequenceArray.getString(it) }
                            val comparisonJson = item.getJSONObject("comparison")
                            val comparison = DrillArtworkComparison(
                                included = comparisonJson.getBoolean("included"),
                                leftLabel = comparisonJson.optString("leftLabel").takeIf(String::isNotBlank),
                                rightLabel = comparisonJson.optString("rightLabel").takeIf(String::isNotBlank),
                                note = comparisonJson.optString("note").takeIf(String::isNotBlank),
                                reason = comparisonJson.optString("reason").takeIf(String::isNotBlank)
                            )
                            val entry = DrillArtworkManifestEntry(
                                id = item.getString("id"),
                                title = item.getString("title"),
                                assetPath = item.getString("assetPath"),
                                width = item.getInt("width"),
                                height = item.getInt("height"),
                                visualSummary = item.getString("visualSummary"),
                                sequence = sequence,
                                comparison = comparison
                            )
                            put(entry.id, entry)
                        }
                    }
                }
            }.onFailure { Log.w("DrillArtwork", "Unable to read the offline drill-art manifest.", it) }
                .getOrElse { emptyMap() }
                .also { entries = it }
        }
    }
}

internal fun drillArtworkEntry(context: Context, drillId: String): DrillArtworkManifestEntry? =
    DrillArtworkManifestStore.entry(context.applicationContext, drillId)

private data class LoadedDrillArtwork(
    val entry: DrillArtworkManifestEntry?,
    val image: ImageBitmap?
)

/** Displays the drill-specific offline WebP or an explicit custom/missing-art fallback. */
@Composable
internal fun DrillArtworkReference(drill: Drill, showRepCheck: Boolean = true) {
    val context = LocalContext.current.applicationContext
    var enlarged by remember(drill.id) { mutableStateOf(false) }
    var loaded by remember(context, drill.id) { mutableStateOf<LoadedDrillArtwork?>(null) }
    LaunchedEffect(context, drill.id) {
        loaded = withContext(Dispatchers.IO) {
            val entry = drillArtworkEntry(context, drill.id)
            val bitmap = entry?.let { metadata ->
                runCatching {
                    context.assets.open(metadata.assetPath).use { input ->
                        BitmapFactory.decodeStream(input)?.asImageBitmap()
                    }
                }.onFailure {
                    Log.w("DrillArtwork", "Unable to decode bundled artwork for ${drill.id}.", it)
                }.getOrNull()
            }
            LoadedDrillArtwork(entry, bitmap)
        }
    }

    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("OFFLINE TECHNIQUE GUIDE",
                color = CourtQuestDesign.accentVivid,
                style = MaterialTheme.typography.labelMedium)
            when {
                loaded == null -> Text(
                    "Loading the bundled drill guide…",
                    color = CourtQuestDesign.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("drill-art-loading-${drill.id}")
                )
                loaded?.entry != null && loaded?.image != null -> {
                    val entry = requireNotNull(loaded?.entry)
                    val image = requireNotNull(loaded?.image)
                    if (enlarged) DrillPlateViewer(drill.title, entry.visualSummary, image) { enlarged = false }
                    Image(
                        bitmap = image,
                        contentDescription = "${entry.sequence.size}-phase technique illustration for ${drill.title}. ${entry.visualSummary}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(entry.width.toFloat() / entry.height.toFloat())
                            .testTag("drill-art-${drill.id}"),
                        contentScale = ContentScale.Fit
                    )
                    TextButton(onClick = { enlarged = true },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("drill-enlarge-art")) {
                        Text("Enlarge illustration")
                    }
                    Text(entry.visualSummary,
                        color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.bodyMedium)
                    entry.sequence.forEachIndexed { index, cue ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text((index + 1).toString().padStart(2, '0'),
                                color = CourtQuestDesign.accentVivid,
                                style = MaterialTheme.typography.labelMedium)
                            Text(cue, modifier = Modifier.weight(1f),
                                color = CourtQuestDesign.textPrimary,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (entry.comparison.included) {
                        HorizontalDivider(color = CourtQuestDesign.textSecondary.copy(alpha = 0.18f))
                        Text("${entry.comparison.leftLabel}  /  ${entry.comparison.rightLabel}",
                            color = CourtQuestDesign.accentVivid,
                            style = MaterialTheme.typography.labelMedium)
                        entry.comparison.note?.let { guidance ->
                            Text(guidance, color = CourtQuestDesign.textPrimary,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Text("Use the illustration to orient yourself. Follow the written cues for footwork, timing and safe spacing.",
                        color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodySmall)
                }
                drill.id.startsWith("custom_") -> Text(
                    "CUSTOM DRILL · NO MOVEMENT IS INFERRED\nUse the instructions saved with this drill.",
                    color = CourtQuestDesign.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("drill-art-custom-fallback")
                )
                else -> Text(
                    "OFFLINE ILLUSTRATION UNAVAILABLE\nUse the court-position diagram and written coaching cues.",
                    color = CourtQuestDesign.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("drill-art-missing-${drill.id}")
                )
            }
            if (showRepCheck) DrillCoachingLibrary.repCheck(drill)?.let {
                CourtEyebrow("WHAT COUNTS AS A REP")
                Text(it, Modifier.testTag("drill-rep-check-${drill.id}"),
                    color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun DrillPlateViewer(title: String, summary: String, image: ImageBitmap, onClose: () -> Unit) {
    var zoomed by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CourtQuestPremiumSurface(Modifier.fillMaxWidth().padding(12.dp)
            .semantics { testTagsAsResourceId = true }, PaddingValues(12.dp), highlighted = true) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
                FlowRow {
                    TextButton(onClick = { zoomed = !zoomed }, modifier = Modifier.heightIn(min = 48.dp).testTag("drill-zoom-art")) {
                        Text(if (zoomed) "Fit illustration" else "Zoom in")
                    }
                    TextButton(onClick = onClose, modifier = Modifier.heightIn(min = 48.dp).testTag("drill-close-art")) { Text("Close") }
                }
                BoxWithConstraints(Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                    val width = maxWidth
                    val height = width * (image.height.toFloat() / image.width)
                    val scale = if (zoomed) 2f else 1f
                    Box(Modifier.horizontalScroll(rememberScrollState()).verticalScroll(rememberScrollState())) {
                        Image(image, summary, Modifier.size(width * scale, height * scale)
                            .testTag("drill-art-viewer"))
                    }
                }
                if (zoomed) Text("Scroll to inspect the footwork, hands and ball.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/** Lightweight, offline artwork preview for drill discovery; no full coaching guide in list rows. */
@Composable
internal fun DrillArtworkThumbnail(drill: Drill, modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    var image by remember(context, drill.id) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(context, drill.id) {
        image = withContext(Dispatchers.IO) {
            drillArtworkEntry(context, drill.id)?.let { metadata ->
                runCatching {
                    context.assets.open(metadata.assetPath).use { input ->
                        BitmapFactory.Options().apply { inSampleSize = 4 }.let { options ->
                            BitmapFactory.decodeStream(input, null, options)?.asImageBitmap()
                        }
                    }
                }.getOrNull()
            }
        }
    }
    if (image != null) {
        Image(
            bitmap = requireNotNull(image),
            contentDescription = "Technique illustration for ${drill.title}",
            modifier = modifier.testTag("library-art-${drill.id}"),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier, contentAlignment = androidx.compose.ui.Alignment.Center) {
            Icon(
                androidx.compose.material.icons.Icons.Default.SportsBasketball,
                contentDescription = null,
                tint = CourtQuestDesign.accentVivid,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}
