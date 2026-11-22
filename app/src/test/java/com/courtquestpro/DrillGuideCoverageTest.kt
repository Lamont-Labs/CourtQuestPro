package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class DrillGuideCoverageTest {
    @Test fun everyCatalogDrillHasTechniqueAndDistinctRepCriteria() {
        val rules = drills.map { drill ->
            val guide = requireNotNull(DrillCoachingLibrary.forDrill(drill)) { drill.id }
            assertTrue(listOf(guide.setup, guide.movement, guide.checkpoint, guide.correction).all { it.isNotBlank() })
            requireNotNull(DrillCoachingLibrary.repCheck(drill)) { drill.id }
        }
        assertEquals(drills.size, rules.toSet().size)
    }

    @Test fun searchMatchesAcrossLevelTitleAndTechnique() {
        val layup = drills.single { it.id == "f1" }
        assertTrue(DrillDiscovery.matches(layup, "  BEGINNER, layup "))
        assertTrue(DrillDiscovery.matches(layup, "layup backboard"))
        assertFalse(DrillDiscovery.matches(layup, "advanced layup"))
        assertTrue(DrillDiscovery.matches(layup, " \t "))
    }

    @Test fun controlledAttemptsUseTheNewMetricWithoutChangingTheirThreshold() {
        val shot = drills.single { it.id == "s2" }
        val measurement = requireNotNull(MissionBuilder.snapshot(shot)).measurement
        assertEquals("Controlled shots", measurement.successLabel)
        assertEquals(15, measurement.minimumSuccesses)
        assertEquals("Shots made", requireNotNull(MissionBuilder.snapshot(drills.first())).measurement.successLabel)
    }

    @Test fun customDrillDoesNotInheritBuiltInTechniqueFromCategory() {
        val custom = drills.first().copy(id = "custom_test")
        assertNull(DrillCoachingLibrary.forDrill(custom))
        assertNull(DrillCoachingLibrary.repCheck(custom))
    }
}
