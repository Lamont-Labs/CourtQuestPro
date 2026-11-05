package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DrillResultPersistenceTest {
    private val gson = Gson()

    @Test fun completedAttemptsSurviveJsonRoundTrip() {
        val original = SaveState(
            playerName = "Vincent",
            drillResults = mutableListOf(
                DrillResult("2026-11-05", "slot-a", "s1", 12, 20),
                DrillResult("2026-11-05", "slot-b", "b1", 25, 30)
            )
        )
        val restored = gson.fromJson(gson.toJson(original), SaveState::class.java)
        assertEquals("Vincent", restored.playerName)
        assertEquals(original.drillResults, restored.drillResults)
    }

    @Test fun legacySaveWithoutResultsRemainsReadable() {
        val restored = gson.fromJson("""{"playerName":"Vincent","totalXp":100}""", SaveState::class.java)
        assertNull(restored.drillResults)
        assertEquals(0, restored.drillResults.orEmpty().size)
    }

    @Test fun personalBestUsesHighestSuccessfulCount() {
        val results = listOf(
            DrillResult("2026-11-03", "a", "s1", 8, 10),
            DrillResult("2026-11-04", "b", "s1", 10, 18),
            DrillResult("2026-11-05", "c", "s1", 9, 10)
        )
        assertEquals(10, results.maxByOrNull { it.completed }?.completed)
    }
}
