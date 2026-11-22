package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class MissionTrainingTest {
    private val day = "2026-11-11"
    private val time = "2026-11-11T18:00:00Z"
    private fun prepared(): SaveState = SaveState(activeDate = day, totalXp = 150).apply {
        MissionBuilder.add(this, "d1"); MissionBuilder.add(this, "b1")
        MissionBuilder.publish(this, time)
    }
    private fun start(save: SaveState): MissionSession = MissionTraining.start(save, save.missionVersions!!.last().id, time)!!
    private fun complete(save: SaveState, session: MissionSession, index: Int): DrillResult? {
        val step = MissionTraining.version(save, session)!!.steps[index]
        val draft = MissionTraining.draft(save, session, index)!!
        assertTrue(MissionTraining.update(save, session.id, draft.copy(successful = step.measurement.minimumSuccesses,
            attempted = step.measurement.minimumSuccesses + 2, targetConfirmed = true)))
        return MissionTraining.finishStep(save, session.id, step.id, day, time)
    }
    @Test fun processRecreationResumesDraftAndSavesEachStepOnce() {
        var save = prepared(); val session = start(save)
        val draft = MissionTraining.draft(save, session, 0)!!
        assertTrue(MissionTraining.update(save, session.id, draft.copy(successful = 2, attempted = 3, remainingSeconds = 20)))
        assertTrue(MissionTraining.pause(save, session.id))
        save = Gson().fromJson(Gson().toJson(save), SaveState::class.java)
        assertEquals(2, MissionTraining.draft(save, save.missionSessions!!.single(), 0)!!.successful)
        assertTrue(MissionTraining.resume(save, session.id))
        assertNotNull(complete(save, save.missionSessions!!.single(), 0))
        assertNull(MissionTraining.finishStep(save, session.id, MissionTraining.version(save, session)!!.steps[0].id, day, time))
        assertEquals(1, save.drillResults!!.size)
        assertNotNull(complete(save, save.missionSessions!!.single(), 1))
        val receipt = save.missionReceipts!!.single()
        assertEquals(session.id, receipt.sessionId)
        assertEquals(2, receipt.resultIds.size)
        assertEquals("SAVED_LOCAL", save.missionSessions!!.single().state)
        assertEquals(150, save.totalXp); assertTrue(save.awardedKeys.isEmpty())
        assertEquals(64, receipt.resultHash.length)
        assertTrue(MissionTraining.receiptValid(save, receipt))
        save.drillResults!![0] = save.drillResults!![0].copy(attempted = 900)
        assertFalse(MissionTraining.receiptValid(save, receipt))
    }
    @Test fun invalidAttemptsCannotCompleteOrReplaceDraftAndSessionIdentity() {
        val save = prepared(); val session = start(save)
        val draft = MissionTraining.draft(save, session, 0)!!
        assertFalse(MissionTraining.update(save, session.id, draft.copy(attempted = -1)))
        assertFalse(MissionTraining.update(save, session.id, draft.copy(slotId = "wrong")))
        assertFalse(MissionTraining.update(save, session.id, draft.copy(remainingSeconds = 9999)))
        val step = MissionTraining.version(save, session)!!.steps[0]
        assertNull(MissionTraining.finishStep(save, session.id, step.id, day, time))
        assertNull(MissionTraining.finishStep(save, session.id, step.id, "bad-date", time))
        assertEquals(0, MissionTraining.draft(save, session, 0)!!.attempted)
    }
    @Test fun newMissionCannotOverwritePracticeOrDailyWork() {
        val save = prepared()
        TrainingLibrary.startPractice(save, "s1", day)
        val practice = save.practiceSession
        assertNull(MissionTraining.start(save, save.missionVersions!!.single().id, time))
        assertEquals(practice, save.practiceSession)
        TrainingLibrary.discardPractice(save, practice!!.id)
        save.slots = mutableListOf(Slot("legacy-slot", "s1"))
        save.drillDrafts = mutableListOf(DrillDraft(day, "legacy-slot", "s1", attempted = 1, remainingSeconds = 350))
        assertNull(MissionTraining.start(save, save.missionVersions!!.single().id, time))
        assertEquals(1, save.drillDrafts!!.single().attempted)
        save.drillDrafts = mutableListOf()
        val session = start(save)
        assertNull(MissionTraining.start(save, save.missionVersions!!.single().id, time))
        assertFalse(TrainingLibrary.startPractice(save, "s1", day))
        assertEquals(session.id, MissionTraining.active(save)!!.id)
    }
    @Test fun discardRetainsRecordedEvidenceAndNewSessionHasDistinctIdentity() {
        val save = prepared(); val first = start(save)
        assertNotNull(complete(save, first, 0))
        assertTrue(MissionTraining.discard(save, first.id))
        assertNull(MissionTraining.active(save)); assertTrue(save.missionReceipts.orEmpty().isEmpty())
        assertEquals(1, save.drillResults!!.size)
        val second = start(save)
        assertNotEquals(first.id, second.id)
        assertNotEquals(MissionTraining.draft(save, second, 0)!!.slotId, save.drillResults!!.single().slotId)
    }
    @Test fun midnightDoesNotExpireMissionAndPausedUpdatesAreRefused() {
        var save = prepared(); val session = start(save)
        assertTrue(MissionTraining.pause(save, session.id))
        val draft = MissionTraining.draft(save, save.missionSessions!!.single(), 0)!!
        assertFalse(MissionTraining.update(save, session.id, draft.copy(successful = 1, attempted = 1)))
        save.activeDate = "2026-11-12"
        save = Gson().fromJson(Gson().toJson(save), SaveState::class.java)
        assertTrue(MissionTraining.resume(save, session.id))
        assertEquals(day, MissionTraining.draft(save, save.missionSessions!!.single(), 0)!!.date)
        assertNotNull(complete(save, save.missionSessions!!.single(), 0))
    }
    @Test fun newPracticeCompletionCarriesFrozenEvidenceWithoutBackfillingHistory() {
        val save = SaveState(activeDate = day)
        val historic = DrillResult(day, "historic", "s1", 15, 16)
        save.drillResults = mutableListOf(historic)
        assertTrue(TrainingLibrary.startPractice(save, "d1", day))
        val practice = save.practiceSession!!
        TrainingLibrary.updatePracticeDraft(save, practice.draft.copy(successful = 4, attempted = 5, targetConfirmed = true))
        val result = TrainingLibrary.finishPractice(save, practice.id, day)!!
        assertNotNull(result.missionVersionId)
        assertEquals(4, result.measurementSnapshot!!.minimumSuccesses)
        assertNotNull(result.recordedAt)
        assertEquals("EXTRA_PRACTICE", save.missionVersions!!.single().source)
        assertEquals(historic, save.drillResults!!.first())
        assertNull(save.drillResults!!.first().recordedAt)
    }

    @Test fun finishRejectsAStaleDateAndUsesTheCurrentCompletionDay() {
        val save = prepared(); val session = start(save)
        val draft = MissionTraining.draft(save, session, 0)!!
        MissionTraining.update(save, session.id, draft.copy(successful = 4, attempted = 5, targetConfirmed = true))
        val step = MissionTraining.version(save, session)!!.steps.first()
        val nextDay = "2026-11-12T18:00:00Z"
        assertNull(MissionTraining.finishStep(save, session.id, step.id, day, nextDay))
        val result = MissionTraining.finishStep(save, session.id, step.id, "2026-11-12", nextDay)!!
        assertEquals("2026-11-12", result.date)
        assertEquals(nextDay, result.recordedAt)
    }

}
