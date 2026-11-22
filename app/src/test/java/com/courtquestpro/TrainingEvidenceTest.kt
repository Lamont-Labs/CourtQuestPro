package com.courtquestpro

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class TrainingEvidenceTest {
    private val day = LocalDate.parse("2026-11-18")
    private val drill = drills.first { it.id == "s1" }
    private val measurement = MissionMeasurement("test-shots-v1", 3, "Shots made",
        "Shots attempted", "+ MAKE", "Count made shots.")
    private fun sample(slot: String, date: String, made: Int, attempts: Int) =
        DrillResult(date, slot, "s1", made, attempts,
            drillSnapshot = drill, measurementSnapshot = measurement)

    @Test fun noHistoryOrLegacyUnfrozenResultsYieldNoClaim() {
        val save = SaveState()
        assertNull(TrainingEvidence.comparable(save, "s1", day))
        save.drillResults = mutableListOf(
            DrillResult("2026-11-15", "a", "s1", 5, 10),
            DrillResult("2026-11-16", "b", "s1", 6, 10),
            DrillResult("2026-11-17", "c", "s1", 7, 10))
        assertNull(TrainingEvidence.comparable(save, "s1", day))
    }

    @Test fun insufficientSessionsAndAttemptsYieldNoClaim() {
        val save = SaveState()
        save.drillResults = mutableListOf(sample("a", "2026-11-15", 5, 10),
            sample("b", "2026-11-16", 6, 10))
        assertNull(TrainingEvidence.comparable(save, "s1", day))
        save.drillResults!!.add(sample("c", "2026-11-17", 3, 5))
        assertNull(TrainingEvidence.comparable(save, "s1", day))
    }

    @Test fun enoughCompatibleEvidenceProducesDescriptiveRates() {
        val save = SaveState()
        save.drillResults = mutableListOf(sample("a", "2026-11-15", 5, 10),
            sample("b", "2026-11-16", 6, 10),
            sample("c", "2026-11-17", 7, 10))
        val evidence = TrainingEvidence.comparable(save, "s1", day)!!
        assertEquals(3, evidence.sessions)
        assertEquals(30, evidence.attempts)
        assertEquals(18, evidence.successes)
        assertEquals(0.6, evidence.aggregateRate, 0.00001)
        assertEquals(20.0, evidence.changePoints, 0.00001)
        assertNull(TrainingEvidence.comparable(save, "s1", LocalDate.parse("2026-11-16")))
    }

    @Test fun duplicateSavedEventsDoNotInflateEvidence() {
        val save = SaveState()
        val first = sample("a", "2026-11-15", 5, 10)
        save.drillResults = mutableListOf(first, first,
            sample("b", "2026-11-16", 6, 10))
        assertNull(TrainingEvidence.comparable(save, "s1", day))
    }
}
