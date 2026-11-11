package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DrillScreen(
    drill: Drill, slotNumber: Int, complete: Boolean, previousResults: List<DrillResult>,
    draft: DrillDraft, onDraft: (DrillDraft) -> Unit, onVideo: () -> Unit,
    onComplete: (Int, Int) -> Unit, onBack: () -> Unit,
    practice: Boolean = false, onDiscard: (() -> Unit)? = null
) {
    var completedCount by remember(drill.id, slotNumber) { mutableIntStateOf(draft.successful) }
    var attemptedCount by remember(drill.id, slotNumber) { mutableIntStateOf(draft.attempted) }
    var targetConfirmed by remember(drill.id, slotNumber) { mutableStateOf(draft.targetConfirmed) }
    var timerSeconds by remember(drill.id, slotNumber) { mutableIntStateOf(draft.remainingSeconds) }
    val measurement = measurementFor(drill.category)
    val requiredCount = DrillTargets.minimum(drill)
    val meetsCount = DrillTargets.eligible(drill, completedCount, attemptedCount)
    var confirmDiscard by remember(drill.id) { mutableStateOf(false) }
    if (confirmDiscard) AlertDialog(onDismissRequest = { confirmDiscard = false }, title = { Text("Discard this practice?") },
        text = { Text("Unrecorded attempts will be removed. Your saved results stay in Progress.") },
        confirmButton = { TextButton({ confirmDiscard = false; onDiscard?.invoke() }) { Text("Discard practice") } },
        dismissButton = { TextButton({ confirmDiscard = false }) { Text("Keep practicing") } })
    val best = previousResults.filter { it.attempted > 0 && it.completed in 0..it.attempted }.maxByOrNull { it.completed }

    fun saveDraft() {
        onDraft(draft.copy(successful = completedCount, attempted = attemptedCount,
            remainingSeconds = timerSeconds, targetConfirmed = targetConfirmed))
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 20.dp)) {
            item { AppHeader(drill.title, if (practice) "Extra practice · ${drill.category}" else "Drill ${slotNumber.coerceIn(1, 6)} of 6 · ${drill.category}") }
            item {
                Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CourtEyebrow("YOUR TARGET · ${drill.minutes} MIN")
                            Text(drill.target, color = CourtQuestDesign.accent, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        CourtMetric(measurement.successLabel, completedCount.toString(), Modifier.weight(1f), accent = true)
                        CourtMetric(measurement.attemptLabel, attemptedCount.toString(), Modifier.weight(1f))
                    }
                    Text("${completedCount.coerceAtMost(requiredCount ?: 0)} of ${requiredCount ?: 0} successful actions toward your target",
                        color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                    CourtQuestPrimaryButton(
                        onClick = { completedCount++; attemptedCount++; saveDraft() },
                        enabled = !complete,
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Record successful attempt" }
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text(measurement.successButton.removePrefix("+").trim().lowercase().replaceFirstChar { it.uppercase() })
                    }
                    OutlinedButton(
                        onClick = { attemptedCount++; saveDraft() }, enabled = !complete,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)
                    ) { Text("Record missed / other attempt") }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { if (completedCount > 0) { completedCount--; attemptedCount--; saveDraft() } },
                            enabled = !complete && completedCount > 0, modifier = Modifier.heightIn(min = 48.dp)) { Text("− Success") }
                        TextButton(onClick = { if (attemptedCount > completedCount) { attemptedCount--; saveDraft() } },
                            enabled = !complete && attemptedCount > completedCount, modifier = Modifier.heightIn(min = 48.dp)) { Text("− Miss") }
                    }
                    if (best != null) {
                        Text("Personal best · ${best.completed} successful from ${best.attempted} attempts",
                            color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    CourtRule()
                    DrillSessionTimer(drill.minutes, timerSeconds) { timerSeconds = it; saveDraft() }
                    CourtRule()
                    CourtHeading("The drill guide")
                    Text(drill.instructions, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = onVideo, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(Icons.Default.PlayCircleOutline, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Watch demonstration")
                    }
                    Text("Use a dry, clear court. Warm up first, keep movements controlled, and stop if the surface is slippery or something hurts.",
                        color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("Back to Court") }
                    if (practice && onDiscard != null) TextButton({ confirmDiscard = true }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Discard practice") }
                }
            }
        }
        Surface(color = CourtQuestDesign.surface, shadowElevation = 8.dp) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .toggleable(targetConfirmed, enabled = !complete, role = Role.Checkbox,
                        onValueChange = { targetConfirmed = it; saveDraft() }),
                    verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = targetConfirmed, onCheckedChange = null, enabled = !complete)
                    Spacer(Modifier.width(10.dp))
                    Text("Full target and safe technique completed.", color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
                CourtQuestPrimaryButton(onClick = { onComplete(completedCount, attemptedCount) },
                    enabled = !complete && meetsCount && targetConfirmed, modifier = Modifier.fillMaxWidth()) {
                    Text(if (complete) "Drill recorded" else "Save results", Modifier.weight(1f))
                    Icon(Icons.Default.Check, null)
                }
            }
        }
    }
}

@Composable
private fun DrillSessionTimer(minutes: Int, initialRemaining: Int, onRemaining: (Int) -> Unit) {
    val initialSeconds = minutes.coerceAtLeast(1) * 60
    var remaining by remember(minutes) { mutableIntStateOf(initialRemaining.coerceIn(0, initialSeconds)) }
    var running by remember(minutes) { mutableStateOf(false) }
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
        Text("%02d:%02d".format(remaining / 60, remaining % 60), color = CourtQuestDesign.textPrimary,
            style = MaterialTheme.typography.displayMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        Text("The clock pauses when you leave this drill.", color = CourtQuestDesign.textSecondary,
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun DrillResultScreen(
    drill: Drill, result: DrillResult, earnedXp: Int, completedSlots: Int,
    onContinue: () -> Unit, onProgress: () -> Unit, onHome: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { AppHeader(if (result.practice) "Practice recorded." else "Session recorded.", "${drill.category} · ${result.date}") }
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = CourtQuestDesign.accentVivid, modifier = Modifier.size(56.dp))
                Text(drill.title, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineLarge)
                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(24.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        CourtEyebrow("YOUR RECORDED RESULT")
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            CourtMetric("Successful", result.completed.toString(), Modifier.weight(1f), accent = true)
                            CourtMetric("Attempts", result.attempted.toString(), Modifier.weight(1f))
                        }
                        CourtRule()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (result.practice) "Extra practice" else "$completedSlots of 6 drills recorded", color = CourtQuestDesign.textSecondary,
                                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text("+$earnedXp XP", color = CourtQuestDesign.accent, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                Text(if (result.practice) "Extra practice recorded in Progress. Your daily workout and its XP rules stay separate."
                    else if (completedSlots == 6) "Today's six-drill workout is complete. Find the full record in Progress."
                    else "Your result is saved. Continue your workout or review your training record.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyLarge)
                CourtQuestPrimaryButton(onClick = if (!result.practice && completedSlots == 6) onProgress else onContinue,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(if (result.practice) "Back to Drills" else if (completedSlots == 6) "View Progress" else "Continue training", Modifier.weight(1f))
                    Icon(Icons.Default.ArrowForward, null)
                }
                if (result.practice || completedSlots < 6) {
                    OutlinedButton(onClick = onProgress, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("View Progress") }
                }
                TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Back to Court") }
            }
        }
    }
}
