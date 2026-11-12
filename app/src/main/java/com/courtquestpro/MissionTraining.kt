package com.courtquestpro

import com.google.gson.Gson
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

internal object MissionTraining {
    fun active(save: SaveState): MissionSession? = save.missionSessions.orEmpty().lastOrNull { it.state in listOf("ACTIVE", "PAUSED") }
    fun version(save: SaveState, session: MissionSession): MissionVersion? = save.missionVersions.orEmpty().firstOrNull { it.id == session.missionVersionId }
    fun pendingDaily(save: SaveState): Int? = save.slots.indices.firstOrNull { i ->
        val slot = save.slots[i]
        val drill = TrainingLibrary.catalog(save).firstOrNull { it.id == slot.drillId }
        drill != null && DrillDraftRecovery.recover(save.drillDrafts.orEmpty(), save.activeDate, slot, drill.minutes * 60) != null
    }
    fun start(save: SaveState, versionId: String, at: String): MissionSession? {
        if (active(save) != null || save.practiceSession != null || pendingDaily(save) != null || runCatching { Instant.parse(at) }.isFailure) return null
        val version = save.missionVersions.orEmpty().firstOrNull { it.id == versionId && it.source in listOf("PERSONAL", "RECOMMENDED") } ?: return null
        if (version.steps.isEmpty()) return null
        val id = "mission-session_${UUID.randomUUID()}"
        val date = Instant.parse(at).atZone(ZoneId.systemDefault()).toLocalDate().toString()
        val session = MissionSession(id, version.id, "ACTIVE", at, version.steps.map {
            DrillDraft(date, "$id:${it.id}", it.drill.id, remainingSeconds = it.drill.minutes * 60)
        })
        save.missionSessions = (save.missionSessions.orEmpty() + session).toMutableList()
        return session
    }
    private fun stored(save: SaveState, id: String) = save.missionSessions.orEmpty().firstOrNull { it.id == id }
    private fun replace(save: SaveState, session: MissionSession) {
        save.missionSessions = save.missionSessions.orEmpty().map { if (it.id == session.id) session else it }.toMutableList()
    }
    fun currentIndex(save: SaveState, session: MissionSession): Int? = version(save, session)?.steps?.indices?.firstOrNull { i ->
        val step = version(save, session)!!.steps[i]
        save.drillResults.orEmpty().none { it.slotId == "${session.id}:${step.id}" }
    }
    fun draft(save: SaveState, session: MissionSession, index: Int): DrillDraft? = stored(save, session.id)?.drafts?.getOrNull(index)
    fun update(save: SaveState, sessionId: String, draft: DrillDraft): Boolean {
        val session = stored(save, sessionId)?.takeIf { it.state == "ACTIVE" } ?: return false
        val index = currentIndex(save, session) ?: return false
        val step = version(save, session)?.steps?.getOrNull(index) ?: return false
        val original = session.drafts.getOrNull(index) ?: return false
        if (!DrillDraftRecovery.valid(draft, original.date, Slot("$sessionId:${step.id}", step.drill.id), step.drill.minutes * 60)) return false
        replace(save, session.copy(drafts = session.drafts.mapIndexed { i, value -> if (i == index) draft else value }))
        return true
    }
    fun pause(save: SaveState, id: String): Boolean {
        val session = stored(save, id)?.takeIf { it.state == "ACTIVE" } ?: return false
        replace(save, session.copy(state = "PAUSED")); return true
    }
    fun resume(save: SaveState, id: String): Boolean {
        val session = stored(save, id)?.takeIf { it.state in listOf("ACTIVE", "PAUSED") && version(save, it) != null } ?: return false
        replace(save, session.copy(state = "ACTIVE")); return true
    }
    fun discard(save: SaveState, id: String): Boolean {
        val session = stored(save, id)?.takeIf { it.state in listOf("ACTIVE", "PAUSED") } ?: return false
        replace(save, session.copy(state = "ABANDONED", drafts = emptyList())); return true
    }
    fun finishStep(save: SaveState, sessionId: String, stepId: String, date: String, at: String): DrillResult? {
        val session = stored(save, sessionId)?.takeIf { it.state == "ACTIVE" } ?: return null
        val version = version(save, session) ?: return null
        val index = currentIndex(save, session) ?: return null
        val step = version.steps[index]; val draft = session.drafts.getOrNull(index) ?: return null
        if (step.id != stepId || runCatching { LocalDate.parse(date); Instant.parse(at) }.isFailure ||
            date != Instant.parse(at).atZone(ZoneId.systemDefault()).toLocalDate().toString() ||
            !draft.targetConfirmed || draft.successful < step.measurement.minimumSuccesses || draft.attempted < draft.successful ||
            !DrillDraftRecovery.valid(draft, draft.date, Slot("$sessionId:$stepId", step.drill.id), step.drill.minutes * 60)) return null
        val result = DrillResult(date, draft.slotId, step.drill.id, draft.successful, draft.attempted, true, step.drill,
            version.id, session.id, step.id, step.measurement, at)
        save.drillResults = (save.drillResults.orEmpty() + result).toMutableList()
        if (currentIndex(save, session) == null) {
            replace(save, session.copy(state = "SAVED_LOCAL", drafts = emptyList()))
            val results = results(save, session, version)
            val receipt = MissionReceipt("receipt_${session.id}", session.id, version.id, results.map { it.slotId }, hash(results), at)
            save.missionReceipts = (save.missionReceipts.orEmpty() + receipt).toMutableList()
        }
        return result
    }
    private fun results(save: SaveState, session: MissionSession, version: MissionVersion) = version.steps.mapNotNull { step ->
        save.drillResults.orEmpty().singleOrNull { it.slotId == "${session.id}:${step.id}" && it.missionVersionId == version.id && it.missionSessionId == session.id }
    }
    private fun hash(results: List<DrillResult>): String = MessageDigest.getInstance("SHA-256")
        .digest(("courtquest-mission-receipt-v1\n" + Gson().toJson(results)).toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
    fun receiptValid(save: SaveState, receipt: MissionReceipt): Boolean {
        val session = stored(save, receipt.sessionId)?.takeIf { it.state == "SAVED_LOCAL" && it.missionVersionId == receipt.missionVersionId } ?: return false
        val version = version(save, session) ?: return false
        val results = results(save, session, version)
        return results.size == version.steps.size && receipt.resultIds == results.map { it.slotId } && receipt.resultHash == hash(results)
    }
    /** Add provenance only for a newly recorded legacy result; old history is never backfilled. */
    fun legacyEvidence(save: SaveState, result: DrillResult, at: String): DrillResult {
        if (result.missionVersionId != null || runCatching { Instant.parse(at) }.isFailure) return result
        val drill = result.drillSnapshot ?: return result
        val step = MissionBuilder.snapshot(drill, result.slotId) ?: return result
        val source = if (result.practice) "EXTRA_PRACTICE" else "DAILY"
        val id = "legacy_${source.lowercase()}_${result.date}_${result.slotId}"
        val version = MissionVersion("$id:v1", id, 1, drill.title, source, listOf(step), at)
        if (save.missionVersions.orEmpty().none { it.id == version.id }) save.missionVersions = (save.missionVersions.orEmpty() + version).toMutableList()
        return result.copy(missionVersionId = version.id, missionSessionId = id, missionStepId = step.id, measurementSnapshot = step.measurement, recordedAt = at)
    }
}
