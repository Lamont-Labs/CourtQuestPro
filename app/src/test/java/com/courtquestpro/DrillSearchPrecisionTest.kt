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

    @Test fun ranksExactTitleAbovePrefixAndCategory() {
        val exact = DrillDiscovery.relevanceFields("Layup", "Shooting", "Beginner", "layup")
        val prefix = DrillDiscovery.relevanceFields("Layups", "Shooting", "Beginner", "layup")
        val category = DrillDiscovery.relevanceFields("Footwork", "Layup", "Beginner", "layup")
        assertTrue(exact > prefix)
        assertTrue(exact > category)
        assertTrue(category > DrillDiscovery.relevanceFields("Footwork", "Shooting", "Beginner", "layup"))
    }

    @Test fun blankAndPunctuationOnlyQueriesMatch() {
        assertTrue(DrillDiscovery.matchesFields(fields, " "))
        assertTrue(DrillDiscovery.matchesFields(fields, "---"))
    }
}
