package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class DrillTargetsTest {
    @Test fun allEighteenDrillsHavePositiveMinimums() {
        val ids = listOf("s1","s2","s3","b1","b2","b3","f1","f2","f3","d1","d2","d3","r1","r2","r3","p1","p2","p3")
        assertTrue(ids.all { (DrillTargets.minimum(it) ?: 0) > 0 })
    }

    @Test fun requiresMinimumSuccessfulActions() {
        assertFalse(DrillTargets.eligible("s1", 14, 20))
        assertTrue(DrillTargets.eligible("s1", 15, 20))
        assertFalse(DrillTargets.eligible("p1", 19, 25))
        assertTrue(DrillTargets.eligible("p1", 20, 25))
    }

    @Test fun rejectsImpossibleOrUnknownRecords() {
        assertFalse(DrillTargets.eligible("s1", 16, 10))
        assertFalse(DrillTargets.eligible("unknown", 100, 100))
        assertFalse(DrillTargets.eligible("s1", -1, 10))
    }
}
