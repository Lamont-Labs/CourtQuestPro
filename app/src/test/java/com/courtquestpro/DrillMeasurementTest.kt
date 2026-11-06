package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class DrillMeasurementTest {
    @Test fun everySkillCategoryHasSpecificVocabulary() {
        val categories = listOf("Shooting", "Finishing", "Passing", "Ball Handling", "Defense", "Rebounding")
        val values = categories.map(::measurementFor)
        assertEquals(6, values.size)
        assertTrue(values.all { it.successLabel.isNotBlank() && it.attemptLabel.isNotBlank() })
        assertEquals("+ MAKE", measurementFor("Shooting").successButton)
        assertEquals("+ ACCURATE", measurementFor("Passing").successButton)
        assertEquals("+ SECURED", measurementFor("Rebounding").successButton)
        assertEquals("+ CLEAN", measurementFor("Defense").successButton)
    }

    @Test fun unknownCategoryHasSafeDefault() {
        val value = measurementFor("Unknown")
        assertEquals("Successful repetitions", value.successLabel)
        assertEquals("+ SUCCESS", value.successButton)
    }
}
