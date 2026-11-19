package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalPrivacyTest {
    @Test fun damagedSavedStateFailsAndLegacyStateStillLoads() {
        assertEquals("", readLocalPlayerState(Gson(), null).playerName)
        assertEquals("Avery", readLocalPlayerState(Gson(), """{"playerName":"Avery"}""").playerName)
        listOf("null", "", "[1,2]", "{invalid").forEach { raw ->
            assertTrue(runCatching { readLocalPlayerState(Gson(), raw) }.isFailure)
        }
    }

    @Test fun externalSearchUsesBundledTextOrAnAllowlistedCategory() {
        val official = drills.first()
        val private = "Avery's personal after-school routine"
        assertEquals(official.videoQuery, demonstrationSearchQuery(official.copy(videoQuery = private)))
        val custom = official.copy(id = "custom_local", title = private, videoQuery = private, instructions = private)
        assertEquals("basketball shooting drill", demonstrationSearchQuery(custom))
        assertEquals("basketball skills drill", demonstrationSearchQuery(custom.copy(category = private)))
    }
}
