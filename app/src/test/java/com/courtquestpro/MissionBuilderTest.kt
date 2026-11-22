package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class MissionBuilderTest {
    private val gson = Gson()
    private fun save() = SaveState(activeDate = "2026-11-11", difficulty = "Beginner")
    private fun add(save: SaveState, id: String) { assertTrue(MissionBuilder.add(save, id)) }

    @Test fun olderSavePreservesLegacyIdentitiesAndDoesNotInventMissions() {
        val raw = """{"playerName":"Avery","activeDate":"2026-11-11","slots":[{"id":"old-slot","drillId":"s1","complete":true}],"totalXp":150,"awardedKeys":["drill_2026-11-11_old-slot"],"drillResults":[{"date":"2026-11-11","slotId":"old-slot","drillId":"s1","completed":15,"attempted":20}],"drillDrafts":[{"date":"2026-11-11","slotId":"pending","drillId":"b1","successful":3,"attempted":4,"remainingSeconds":90,"targetConfirmed":false}],"matches":[],"practiceSession":{"id":"old-practice","drillId":"b1","date":"2026-11-11","draft":{"date":"2026-11-11","slotId":"old-practice","drillId":"b1","successful":2,"attempted":2,"remainingSeconds":80,"targetConfirmed":false}}}"""
        val restored = gson.fromJson(raw, SaveState::class.java)
        assertNull(restored.missionDraft); assertTrue(restored.missionVersions.orEmpty().isEmpty())
        assertNull(restored.drillResults!!.single().missionVersionId)
        MissionBuilder.ensureDraft(restored)
        val roundtrip = gson.fromJson(gson.toJson(restored), SaveState::class.java)
        assertEquals("old-slot", roundtrip.slots.single().id)
        assertEquals("old-practice", roundtrip.practiceSession!!.id)
        assertEquals(3, roundtrip.drillDrafts!!.single().successful)
        assertEquals(150, roundtrip.totalXp); assertEquals(restored.awardedKeys, roundtrip.awardedKeys)
        assertEquals(restored.drillResults, roundtrip.drillResults)
    }
    @Test fun publishedStepsSurviveCustomRevisionArchiveAndBuilderEdits() {
        val save = save()
        val original = TrainingLibrary.saveCustom(save, DrillInput("Corner shots", "Shooting", "Beginner", 3, 2, "Make two shots from a comfortable spot."))!!
        add(save, original.id)
        MissionBuilder.rename(save, "My workout")
        val first = MissionBuilder.publish(save, "2026-11-11T18:00:00Z")!!
        TrainingLibrary.saveCustom(save, DrillInput("New corner shots", "Shooting", "Beginner", 5, 8, "Make eight shots using balanced form."), original.id)
        MissionBuilder.remove(save, save.missionDraft!!.steps.single().id)
        add(save, "b1")
        val second = MissionBuilder.publish(save, "2026-11-11T19:00:00Z")!!
        assertEquals(first.missionId, second.missionId); assertEquals(2, second.version)
        assertEquals("Corner shots", first.steps.single().drill.title)
        assertEquals(2, first.steps.single().measurement.minimumSuccesses)
        assertEquals("b1", second.steps.single().drill.id)
        assertEquals(2, save.missionVersions!!.size)
        assertTrue(MissionBuilder.open(save, first.id))
        assertEquals(first.steps, save.missionDraft!!.steps)
        assertEquals(first, MissionBuilder.publish(save, "2026-11-12T00:00:00Z"))
        assertEquals(2, save.missionVersions!!.size)
    }
    @Test fun editsReorderRemoveAndReopenWithoutTrainingSideEffects() {
        val save = save(); add(save, "b1"); add(save, "s1")
        assertFalse(MissionBuilder.add(save, "s1"))
        assertTrue(MissionBuilder.move(save, save.missionDraft!!.steps[1].id, -1))
        assertEquals(listOf("s1", "b1"), save.missionDraft!!.steps.map { it.drill.id })
        assertEquals(12, save.missionDraft!!.steps.sumOf { it.drill.minutes })
        val restored = gson.fromJson(gson.toJson(save), SaveState::class.java)
        assertEquals(save.missionDraft, restored.missionDraft)
        assertEquals(0, restored.totalXp); assertTrue(restored.drillResults.orEmpty().isEmpty())
        MissionBuilder.rename(restored, " "); assertNull(MissionBuilder.publish(restored, "2026-11-11T18:00:00Z"))
    }
    @Test fun generatorEnforcesConstraintsAndReplaysStoredCandidates() {
        val inputs = MissionConstraints(skill = "Ball Handling", minutes = 15, equipment = listOf("Ball"), space = "Small space", maxLevel = "Intermediate")
        val generated = MissionBuilder.generate(TrainingLibrary.available(save()), inputs, 921L)
        assertNull(generated.error); assertEquals(listOf("b1"), generated.steps.map { it.drill.id })
        assertTrue(generated.steps.sumOf { it.drill.minutes } <= inputs.minutes)
        val replay = MissionBuilder.replay(generated.provenance!!)
        assertEquals(generated.steps, replay.steps)
        assertEquals(generated.provenance, gson.fromJson(gson.toJson(generated.provenance), MissionGenerationProvenance::class.java))
    }
    @Test fun impossibleGenerationPreservesMandatoryStepsAndBuilder() {
        val save = save(); add(save, "s1")
        MissionBuilder.toggleRequired(save, save.missionDraft!!.steps.single().id)
        val before = save.missionDraft
        val inputs = MissionConstraints(minutes = 5, equipment = emptyList(), space = "Small space", maxLevel = "Beginner")
        val result = MissionBuilder.generateIntoDraft(save, inputs, 9L)
        assertNotNull(result.error); assertEquals(before, save.missionDraft)
        assertTrue(result.error!!.contains("required", true))
        val empty = MissionBuilder.generate(drills, inputs.copy(skill = "Shooting"), 1L)
        assertNotNull(empty.error); assertTrue(empty.steps.isEmpty())
    }
    @Test fun generatedDraftKeepsInputProvenanceButManualEditsChangeSource() {
        val save = save()
        assertNull(MissionBuilder.generateIntoDraft(save, MissionConstraints(minutes = 15), 42L).error)
        val version = MissionBuilder.publish(save, "2026-11-11T18:00:00Z")!!
        assertEquals("RECOMMENDED", version.source)
        assertEquals(42L, version.generation!!.seed)
        MissionBuilder.move(save, save.missionDraft!!.steps.last().id, -1)
        val edited = MissionBuilder.publish(save, "2026-11-11T19:00:00Z")!!
        assertEquals("PERSONAL", edited.source)
        assertNull(edited.generation)
    }
}
