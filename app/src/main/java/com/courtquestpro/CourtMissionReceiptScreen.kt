package com.courtquestpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun MissionReceiptScreen(save: SaveState, receipt: MissionReceipt, onHome: () -> Unit, onProgress: () -> Unit) {
    val version = save.missionVersions.orEmpty().firstOrNull { it.id == receipt.missionVersionId }
    val valid = MissionTraining.receiptValid(save, receipt)
    val results = receipt.resultIds.mapNotNull { id -> save.drillResults.orEmpty().singleOrNull { it.slotId == id } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { AppHeader(if (valid) "Mission complete." else "Receipt needs attention.", version?.title ?: "Saved Mission") }
        item {
            CourtQuestPremiumSurface(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), PaddingValues(24.dp), highlighted = true) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (valid) CourtSavedStamp(Modifier.size(36.dp))
                    CourtEyebrow(if (valid) "SAVED ON THIS DEVICE" else "EVIDENCE COULD NOT BE CONFIRMED", Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    Text(version?.title ?: "Mission", style = MaterialTheme.typography.headlineMedium, color = CourtQuestDesign.textPrimary)
                    Text("Version ${version?.version ?: "?"} · Self-recorded", style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.textSecondary)
                    CourtMetricStrip(listOf("Drills recorded" to results.size.toString(), "Attempts" to results.sumOf { it.attempted }.toString()))
                    Text("Saved ${receipt.savedAt}", style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                    Text("Personal Mission · Extra practice", style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                }
            }
        }
        items(results, key = { it.slotId }) { result ->
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(result.drillSnapshot?.title ?: "Saved drill", style = MaterialTheme.typography.titleLarge, color = CourtQuestDesign.textPrimary)
                Text("${result.completed} successful / ${result.attempted} attempts", style = MaterialTheme.typography.bodyMedium, color = CourtQuestDesign.accent)
                Text(result.drillSnapshot?.target.orEmpty(), style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                Text("Self-recorded · ${result.date}", style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
                CourtRule()
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CourtQuestPrimaryButton(onProgress, Modifier.fillMaxWidth()) { Text("View Progress") }
                OutlinedButton(onHome, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Back to Court") }
                Text("Instructions, targets and results belong to this saved version. Planned minutes are an estimate, not measured training time.", style = MaterialTheme.typography.bodySmall, color = CourtQuestDesign.textSecondary)
            }
        }
    }
}

@Composable
internal fun MissionStepReceiptScreen(save: SaveState, result: DrillResult, onNext: () -> Unit, onSummary: (String) -> Unit,
    onHome: () -> Unit, onProgress: () -> Unit) {
    val version = save.missionVersions.orEmpty().firstOrNull { it.id == result.missionVersionId }
    val receipt = save.missionReceipts.orEmpty().firstOrNull { it.sessionId == result.missionSessionId }
    val session = save.missionSessions.orEmpty().firstOrNull { it.id == result.missionSessionId }
    val next = session?.let { MissionTraining.currentIndex(save, it) }?.let { version?.steps?.getOrNull(it) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { AppHeader("Drill recorded.", "${version?.title.orEmpty()} · Version ${version?.version ?: "?"}", compact = true) }
        item {
            CourtQuestPremiumSurface(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), PaddingValues(24.dp), highlighted = true) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CourtSavedStamp(Modifier.size(36.dp)); CourtEyebrow("SAVED TO PROGRESS", Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    Text(result.drillSnapshot?.title.orEmpty(), style = MaterialTheme.typography.headlineMedium, color = CourtQuestDesign.textPrimary)
                    CourtMetricStrip(listOf("Successful" to result.completed.toString(), "Attempts" to result.attempted.toString()))
                    Text("Self-recorded · Saved on this device", color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (next != null) item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CourtEyebrow("UP NEXT"); Text(next.drill.title, style = MaterialTheme.typography.titleLarge, color = CourtQuestDesign.textPrimary)
                CourtQuestPrimaryButton(onNext, Modifier.fillMaxWidth()) { Text("Next Mission drill") }
            }
        }
        if (receipt != null) item { CourtQuestPrimaryButton({ onSummary(receipt.id) }, Modifier.padding(horizontal = 20.dp).fillMaxWidth()) { Text("Mission summary") } }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onProgress, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("View Progress") }
                TextButton(onHome, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Back to Court") }
            }
        }
    }
}
