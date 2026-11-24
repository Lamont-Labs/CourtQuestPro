package com.courtquestpro

import org.junit.Assert.assertEquals
import org.junit.Test

class SoloArtworkPolicyTest {
    @Test fun onlyPartnerDependentPlatesRequireRevision() {
        assertEquals(setOf("p1", "d3", "r3", "p2", "p3"),
            drills.map { it.id }.filter(::requiresSoloArtworkRevision).toSet())
    }
    @Test fun customDrillsRetainTheirOwnFallback() {
        assertEquals(false, requiresSoloArtworkRevision("custom_example"))
    }
}
