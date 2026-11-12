package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Existing daily results and Mission receipts, without issuing new awards or skill claims. */
@Composable
internal fun QuestsScreen(save: SaveState, today: String, onTraining: () -> Unit, onBuild: () -> Unit,
    onResumeMission: () -> Unit, onProgress: () -> Unit) {
    val catalog = TrainingLibrary.catalog(save)
    val slots = if (save.activeDate == today) save.slots else emptyList()
    val recordedIds = save.drillResults.orEmpty().filter { result ->
        result.date == today && !result.practice && result.attempted > 0 && result.completed in 0..result.attempted &&
            slots.any { it.id == result.slotId && it.drillId == result.drillId }
    }.map { it.slotId }.toSet()
    val done = recordedIds.size
    val active = MissionTraining.active(save)
    val version = active?.let { MissionTraining.version(save, it) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { AppHeader("A little work. A better game.", "Choose a session. Keep a record of your work.") }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                if (active != null) CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(20.dp), highlighted = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CourtEyebrow("YOUR ACTIVE MISSION")
                        Text(version?.title ?: "Your saved Mission", style = MaterialTheme.typography.headlineMedium, color = CourtQuestDesign.textPrimary)
                        Text("Your recorded drills and unfinished attempts are safe.", style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                        CourtQuestPrimaryButton(onResumeMission, Modifier.fillMaxWidth()) { Text("Resume Mission") }
                    }
                }
                CourtQuestPremiumSurface(Modifier.fillMaxWidth(), PaddingValues(24.dp), highlighted = active == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CourtEyebrow("TODAY'S DAILY QUEST")
                        Text(if (slots.isNotEmpty() && done == slots.size) "Work recorded." else "Build all six skills.",
                            style = MaterialTheme.typography.headlineLarge, color = CourtQuestDesign.textPrimary)
                        Text("$done of ${slots.size} drills recorded today",
                            style = MaterialTheme.typography.titleMedium, color = CourtQuestDesign.accent)
                        LinearProgressIndicator(progress = { if (slots.isEmpty()) 0f else done.toFloat() / slots.size },
                            modifier = Modifier.fillMaxWidth(), color = CourtQuestDesign.accentVivid, trackColor = CourtQuestDesign.border)
                        if (slots.isNotEmpty() && done == slots.size) {
                            CourtQuestPrimaryButton(onProgress, Modifier.fillMaxWidth()) { Text("See today's results") }
                        } else if (active == null && slots.isNotEmpty()) {
                            CourtQuestPrimaryButton(onTraining, Modifier.fillMaxWidth()) { Text(if (done > 0) "Continue daily training" else "Start daily training") }
                        }
                        Text("Recorded results count here. Take rest when you need it; your saved work stays yours.",
                            style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                    }
                }
                slots.forEach { slot ->
                    val drill = catalog.firstOrNull { it.id == slot.drillId }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (slot.id in recordedIds) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            if (slot.id in recordedIds) "Recorded" else "Not recorded", tint = CourtQuestDesign.accent)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(drill?.category ?: "Training", style = MaterialTheme.typography.titleMedium, color = CourtQuestDesign.textPrimary)
                            Text(drill?.title ?: "Saved drill", style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                        }
                    }
                }
                CourtRule()
                CourtHeading("Make your own Mission")
                Text("Choose a short session or build a longer lineup. Saved versions keep the same drills and targets.",
                    style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                OutlinedButton(onBuild, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Build a Mission") }
                CourtHeading("Missions you finished")
                val count = save.missionReceipts.orEmpty().map { it.id }.distinct().size
                Text(if (count == 0) "No completed Missions yet. Finish a saved Mission to keep its completion receipt in Progress."
                    else "$count completed ${if (count == 1) "Mission" else "Missions"} · Self-recorded · Saved on this device",
                    style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                OutlinedButton(onProgress, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("View saved results") }
            }
        }
    }
}
