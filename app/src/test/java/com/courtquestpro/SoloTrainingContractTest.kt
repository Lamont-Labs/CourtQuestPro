package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class SoloTrainingContractTest {
    private val expected = mapOf("s2" to 15, "p1" to 20, "d2" to 16,
        "d3" to 4, "r3" to 10, "p2" to 10, "p3" to 8)

    @Test fun revisedDrillsRetainIdentityAndCompletionThresholds() {
        expected.forEach { (id, minimum) ->
            val drill = drills.single { it.id == id }
            assertEquals(id, minimum, DrillTargets.minimum(drill))
            assertTrue(id, DrillTargets.eligible(drill, minimum, minimum))
            assertFalse(id, DrillTargets.eligible(drill, minimum - 1, minimum))
            assertFalse(id, DrillTargets.eligible(drill, minimum, minimum - 1))
        }
    }

    @Test fun revisedInstructionsDoNotRequirePartner() {
        expected.keys.forEach { id ->
            val drill = drills.single { it.id == id }
            val guide = requireNotNull(DrillCoachingLibrary.forDrill(drill))
            val text = listOf(drill.instructions, drill.target, guide.setup,
                guide.movement, guide.checkpoint, guide.correction,
                requireNotNull(DrillCoachingLibrary.repCheck(drill))).joinToString(" ")
            assertFalse(id, Regex("\\bpartner\\b", RegexOption.IGNORE_CASE).containsMatchIn(text))
        }
    }

    @Test fun revisedDrillsKeepTheirEquipmentAndCoaching() {
        expected.keys.forEach { id ->
            val drill = drills.single { it.id == id }
            assertNotNull(id, DrillEquipment.label(id))
            assertNotNull(id, DrillCoachingLibrary.repCheck(drill))
        }
    }
}
