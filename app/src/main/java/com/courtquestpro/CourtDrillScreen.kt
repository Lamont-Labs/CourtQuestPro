package com.courtquestpro

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DrillScreen(
    drill: Drill, slotNumber: Int, complete: Boolean, previousResults: List<DrillResult>,
    draft: DrillDraft, onDraft: (DrillDraft) -> Unit, onVideo: () -> Unit,
    onComplete: (Int, Int) -> Unit, onBack: () -> Unit,
    practice: Boolean = false, onDiscard: (() -> Unit)? = null,
    sessionLabel: String? = null, frozenMeasurement: MissionMeasurement? = null
) {
    var completedCount by remember(draft.slotId) { mutableIntStateOf(draft.successful) }
    var attemptedCount by remember(draft.slotId) { mutableIntStateOf(draft.attempted) }
    var targetConfirmed by remember(draft.slotId) { mutableStateOf(draft.targetConfirmed) }
    var timerSeconds by remember(draft.slotId) { mutableIntStateOf(draft.remainingSeconds) }
    val specific = drillSpecificMeasurement(drill)
    val measurement = frozenMeasurement?.vocabulary() ?: specific ?: measurementFor(drill.category)
    val requiredCount = frozenMeasurement?.minimumSuccesses ?: DrillTargets.minimum(drill)
    val meetsCount = requiredCount != null && completedCount >= requiredCount && attemptedCount >= completedCount
    var confirmDiscard by remember(drill.id) { mutableStateOf(false) }
    var showFullGuide by remember(drill.id) { mutableStateOf(false) }
    val largeText = LocalDensity.current.fontScale > 1.3f
    if (confirmDiscard) AlertDialog(onDismissRequest = { confirmDiscard = false }, title = { Text(if (sessionLabel != null) "Discard this Mission session?" else "Discard this practice?") },
        text = { Text("Unrecorded attempts will be removed. Your saved results stay in Progress.") },
        confirmButton = { TextButton({ confirmDiscard = false; onDiscard?.invoke() }) { Text(if (sessionLabel != null) "Discard Mission session" else "Discard practice") } },
        dismissButton = { TextButton({ confirmDiscard = false }) { Text("Keep practicing") } })
    val best = previousResults.filter {
        it.attempted > 0 && it.attempted == attemptedCount && it.completed in 0..it.attempted &&
            (specific == null || it.measurementSnapshot?.vocabulary() == measurement)
    }.maxByOrNull { it.completed }

    fun saveDraft() {
        onDraft(draft.copy(successful = completedCount, attempted = attemptedCount,
            remainingSeconds = timerSeconds, targetConfirmed = targetConfirmed))
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 20.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 10.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onBack, Modifier.size(48.dp).testTag("live-leave-session")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Leave session", tint = CourtQuestDesign.textPrimary)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        CourtEyebrow(sessionLabel ?: if (practice) "EXTRA PRACTICE" else "DAILY DRILL ${slotNumber.coerceIn(1, 6)} OF 6")
                        Text(drill.title, style = MaterialTheme.typography.headlineMedium, color = CourtQuestDesign.textPrimary)
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Keep the essential instruction visible while attempts are being recorded.
                    // The full illustrated guide remains available below on demand.
                    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(14.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CourtEyebrow("FOCUS FOR THIS SET")
                            Text(drill.target, color = CourtQuestDesign.textPrimary,
                                style = MaterialTheme.typography.titleMedium)
                            DrillCoachingLibrary.forDrill(drill)?.let { guide ->
                                Text(guide.checkpoint, color = CourtQuestDesign.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp), highlighted = true) {
                      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val rate = if (attemptedCount == 0) null else completedCount.toFloat() / attemptedCount
                    @Composable fun Counts() {
                      Box(Modifier.testTag("live-drill-stats")) {
                         CourtMetricStrip(listOf(measurement.successLabel to completedCount.toString(),
                             measurement.attemptLabel to attemptedCount.toString(),
                             "Success rate" to
                                 (rate?.let { "${(it * 100).toInt()}%" } ?: "—")))
                      }
                    }
                    if (!largeText) Counts()
                    CourtQuestPrimaryButton(
                        onClick = {
                            if (!meetsCount) targetConfirmed = false
                            completedCount++; attemptedCount++; saveDraft()
                        },
                        enabled = !complete,
                         modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("live-record-success")
                            .semantics { contentDescription = "Record successful attempt" }
                    ) {
                        Icon(Icons.Default.Add, null, Modifier.size(25.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(measurement.successButton.removePrefix("+").trim())
                    }
                    OutlinedButton(
                        onClick = { attemptedCount++; saveDraft() }, enabled = !complete,
                         modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("live-record-miss"), shape = RoundedCornerShape(16.dp)
                    ) { Text("Record missed / other attempt") }
                    if (largeText) Counts()
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { if (completedCount > 0) {
                            completedCount--; attemptedCount--
                            if (requiredCount == null || completedCount < requiredCount) targetConfirmed = false
                            saveDraft()
                        } }, enabled = !complete && completedCount > 0,
                            modifier = Modifier.heightIn(min = 48.dp).testTag("live-undo-success")) { Text("− Success") }
                        TextButton(onClick = { if (attemptedCount > completedCount) { attemptedCount--; saveDraft() } },
                            enabled = !complete && attemptedCount > completedCount,
                            modifier = Modifier.heightIn(min = 48.dp).testTag("live-undo-miss")) { Text("− Miss") }
                    }
                     CourtEyebrow("NEXT STEP")
                     Text(
                         if (meetsCount) "Target reached. Confirm safe completion, then save your result."
                         else if (requiredCount != null) "Record ${(requiredCount - completedCount).coerceAtLeast(0)} more ${measurement.successLabel.lowercase()} toward your target."
                         else "Follow the drill target above.",
                         modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                         color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.bodyMedium
                     )
                     if (requiredCount != null && requiredCount > 0) {
                         val progress = (completedCount.toFloat() / requiredCount).coerceIn(0f, 1f)
                         LinearProgressIndicator(
                             progress = { progress },
                             modifier = Modifier.fillMaxWidth().height(8.dp)
                                 .testTag("live-target-progress"),
                             color = CourtQuestDesign.accentVivid,
                             trackColor = CourtQuestDesign.surface
                         )
                         FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                             Text("${completedCount.coerceAtMost(requiredCount)} / ${requiredCount} successful actions",
                                 color = CourtQuestDesign.textSecondary,
                                 style = MaterialTheme.typography.bodySmall)
                             Text("${(progress * 100).toInt()}% of target",
                                 color = if (meetsCount) CourtQuestDesign.accentVivid
                                     else CourtQuestDesign.textSecondary,
                                 style = MaterialTheme.typography.labelMedium)
                         }
                     } else {
                         Text("Follow the drill target and confirm completion when ready.",
                             color = CourtQuestDesign.textSecondary,
                             style = MaterialTheme.typography.bodySmall)
                     }
                      }
                    }
                     // Completion remains explicit and count-gated, but no longer
                     // occupies the live recording viewport while it is disabled.
                     CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp), blurred = true) {
                         Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                             Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                 .testTag("live-confirm-target")
                                 .toggleable(targetConfirmed && meetsCount, enabled = !complete && meetsCount, role = Role.Checkbox,
                                     onValueChange = { targetConfirmed = it; saveDraft() }),
                                 verticalAlignment = Alignment.CenterVertically) {
                                 Checkbox(checked = targetConfirmed && meetsCount, onCheckedChange = null, enabled = !complete && meetsCount)
                                 Spacer(Modifier.width(10.dp))
                                 Text("Full target completed safely.", color = CourtQuestDesign.textPrimary,
                                     style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                             }
                             CourtQuestPrimaryButton(onClick = { onComplete(completedCount, attemptedCount) },
                                 enabled = !complete && meetsCount && targetConfirmed,
                                 modifier = Modifier.fillMaxWidth().testTag("live-save-results")) {
                                 Text(if (complete) "Drill recorded" else "Save results", Modifier.weight(1f))
                                 Icon(Icons.Default.Check, null)
                             }
                         }
                     }
                    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(12.dp)) {
                        DrillSessionTimer(drill.minutes, timerSeconds, draft.slotId) { timerSeconds = it; saveDraft() }
                    }
                    if (best != null) {
                        Text("Previous high at ${best.attempted} attempts · ${best.completed} successful",
                            color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(
                        onClick = { showFullGuide = !showFullGuide },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                            .testTag("live-toggle-drill-guide")
                    ) {
                        Icon(if (showFullGuide) Icons.Default.ExpandLess else Icons.Default.MenuBook, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (showFullGuide) "Hide full drill guide" else "Open drill guide",
                            modifier = Modifier.weight(1f))
                        Icon(if (showFullGuide) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                    }
                    if (showFullGuide) {
                    CourtHeading("The drill guide")
                    CourtEyebrow("YOUR TARGET · ${drill.minutes} MIN PLAN")
                    Text(drill.target, color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.titleLarge)
                    Text("The clock pauses when you leave this drill.", color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodySmall)
                    Text(drill.instructions, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyLarge)
                    DrillCoachingLibrary.forDrill(drill)?.let {
                        CourtEyebrow("TECHNIQUE CHECK")
                        Text(it.checkpoint, color = CourtQuestDesign.textPrimary)
                        CourtEyebrow("COMMON MISTAKE & FIX")
                        Text(it.correction, color = CourtQuestDesign.textSecondary)
                    }
                    DrillArtworkReference(drill, showRepCheck = frozenMeasurement == null ||
                        specific == null || frozenMeasurement.vocabulary() == specific)
                    DrillCourtReference(drill)
                    DrillFormReference(drill)
                    TextButton(onClick = onVideo, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(Icons.Default.PlayCircleOutline, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Find videos on YouTube")
                    }
                    Text("Use a dry, clear court. Warm up first, keep movements controlled, and stop if the surface is slippery or something hurts.",
                        color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    }
                    if (practice && onDiscard != null) {
                        TextButton({ confirmDiscard = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text(if (sessionLabel != null) "Discard Mission session" else "Discard practice")
                        }
                    }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DrillSessionTimer(minutes: Int, initialRemaining: Int, sessionId: String, onRemaining: (Int) -> Unit) {
    val initialSeconds = minutes.coerceAtLeast(1) * 60
    var remaining by remember(sessionId) { mutableIntStateOf(initialRemaining.coerceIn(0, initialSeconds)) }
    var running by remember(sessionId) { mutableStateOf(false) }
    val lifecycle = (LocalContext.current as? ComponentActivity)?.lifecycle
    DisposableEffect(lifecycle, sessionId) {
        val pauseOnBackground = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) running = false
        }
        lifecycle?.addObserver(pauseOnBackground)
        onDispose { lifecycle?.removeObserver(pauseOnBackground) }
    }
    LaunchedEffect(running, remaining) {
        if (running && remaining > 0) {
            delay(1000L)
            remaining = (remaining - 1).coerceAtLeast(0)
            onRemaining(remaining)
            if (remaining == 0) running = false
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CourtEyebrow(if (remaining == 0) "CLOCK FINISHED" else if (running) "CLOCK RUNNING" else "TRAINING CLOCK")
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.widthIn(max = 152.dp * LocalDensity.current.fontScale).fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize().testTag("live-drill-ring").semantics {
                    contentDescription = "Training clock: %02d minutes %02d seconds".format(remaining / 60, remaining % 60)
                }) {
                    val stroke = 6.dp.toPx()
                    drawCircle(CourtQuestDesign.accentVivid.copy(alpha = .1f), size.minDimension * .47f)
                    drawArc(Color(0xFF363638), -90f, 360f, false, style = Stroke(stroke))
                    val fraction = remaining.toFloat() / initialSeconds.coerceAtLeast(1)
                    drawArc(CourtQuestDesign.accentVivid, -90f, 360f * fraction, false,
                        style = Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                }
                Text("%02d:%02d".format(remaining / 60, remaining % 60), color = CourtQuestDesign.textPrimary,
                    style = MaterialTheme.typography.headlineLarge)
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = {
                if (remaining == 0) { remaining = initialSeconds; onRemaining(remaining) }
                running = !running
            }, modifier = Modifier.heightIn(min = 48.dp)) {
                Icon(if (running) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(6.dp))
                Text(if (running) "Pause" else if (remaining == 0) "Restart" else "Start")
            }
            TextButton(onClick = { running = false; remaining = initialSeconds; onRemaining(remaining) },
                modifier = Modifier.heightIn(min = 48.dp)) { Text("Reset clock") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DrillResultScreen(
    drill: Drill, result: DrillResult, earnedXp: Int, completedSlots: Int,
    nextDrill: Drill? = null,
    onContinue: () -> Unit, onProgress: () -> Unit, onHome: () -> Unit
) {
    val workoutComplete = !result.practice && completedSlots == 6
    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 20.dp)) {
            item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(if (result.practice) "Practice recorded." else "Session recorded.",
                        style = MaterialTheme.typography.headlineMedium, color = CourtQuestDesign.textPrimary)
                    CourtQuestPremiumSurface(Modifier.fillMaxWidth().testTag("drill-saved-receipt"), PaddingValues(16.dp), highlighted = true) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CourtSavedStamp(Modifier.size(36.dp))
                                CourtEyebrow("SAVED TO PROGRESS", Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite })
                            }
                            Text(drill.title, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineMedium)
                            Text("Self-recorded · Saved on this device", color = CourtQuestDesign.textSecondary,
                                style = MaterialTheme.typography.bodySmall)
                            val recordedTime = result.recordedAt?.let {
                                runCatching { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                                    .withZone(ZoneId.systemDefault()).format(Instant.parse(it)) }.getOrNull()
                            }
                            Text(listOfNotNull(result.date, recordedTime).joinToString(" · "),
                                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                            val accuracy = if (result.attempted > 0)
                                "${(result.completed * 100.0 / result.attempted).toInt()}%" else "—"
                            val vocabulary = result.measurementSnapshot?.vocabulary() ?: measurementFor(drill.category)
                            Text("${result.completed} / ${result.attempted} · $accuracy",
                                color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineMedium)
                            Text("${vocabulary.successLabel} / ${vocabulary.attemptLabel} · Success rate",
                                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                            val target = DrillTargets.minimum(drill)
                            if (target != null && target > 0) {
                                val targetProgress = (result.completed.toFloat() / target).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { targetProgress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp)
                                        .testTag("result-target-progress"),
                                    color = CourtQuestDesign.accentVivid,
                                    trackColor = CourtQuestDesign.surface
                                )
                                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Target: ${result.completed} / $target",
                                        color = CourtQuestDesign.textSecondary,
                                        style = MaterialTheme.typography.bodySmall)
                                    Text(if (result.completed >= target) "TARGET MET"
                                        else "${(targetProgress * 100).toInt()}% complete",
                                        color = CourtQuestDesign.accentVivid,
                                        style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            CourtRule()
                            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(if (result.practice) "Extra practice" else "$completedSlots of 6 drills recorded",
                                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                                Text(if (result.practice) "No daily XP awarded" else "+$earnedXp participation XP",
                                    color = CourtQuestDesign.accent, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                    Text(if (result.practice) "Extra practice recorded in Progress. Your daily workout and its XP rules stay separate."
                        else if (workoutComplete) "Today's six-drill workout is complete. Find the full record in Progress."
                        else "Your result is saved. Continue your workout or review your training record.",
                        color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                    if (nextDrill != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CourtEyebrow("UP NEXT")
                            Text(nextDrill.title, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
                            Text("${nextDrill.category} · ${nextDrill.minutes} min", color = CourtQuestDesign.textSecondary,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (!workoutComplete) TextButton(onClick = onHome, modifier = Modifier.heightIn(min = 48.dp)) { Text("Back to Court") }
                }
            }
        }
        CourtQuestPremiumSurface(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            PaddingValues(12.dp), blurred = true) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CourtQuestPrimaryButton(onClick = if (workoutComplete) onProgress else onContinue,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(if (result.practice) "Back to Drills" else if (workoutComplete) "View Progress" else "Continue training", Modifier.weight(1f))
                    Icon(Icons.Default.ArrowForward, null)
                }
                TextButton(onClick = if (workoutComplete) onHome else onProgress,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(if (workoutComplete) "Back to Court" else "View Progress")
                }
            }
        }
    }
}
