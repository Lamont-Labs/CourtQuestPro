package com.courtquestpro

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrillSearchPrecisionTest {
    private val fields = listOf("Left-hand layup", "Beginner", "Passing", "Compass footwork")

    @Test fun matchesAcrossFieldsAndPrefixes() {
        assertTrue(DrillDiscovery.matchesFields(fields, "BEGIN lay"))
        assertTrue(DrillDiscovery.matchesFields(fields, "left-hand"))
        assertTrue(DrillDiscovery.matchesFields(fields, "pass"))
    }

    @Test fun rejectsInteriorSubstringsAndMissingTerms() {
        assertFalse(DrillDiscovery.matchesFields(listOf("Compass footwork"), "pass"))
        assertFalse(DrillDiscovery.matchesFields(fields, "beginner rebound"))
    }

    @Test fun blankAndPunctuationOnlyQueriesMatch() {
        assertTrue(DrillDiscovery.matchesFields(fields, " "))
        assertTrue(DrillDiscovery.matchesFields(fields, "---"))
    }
}
