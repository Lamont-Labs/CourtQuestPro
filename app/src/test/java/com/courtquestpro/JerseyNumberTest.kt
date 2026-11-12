package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JerseyNumberTest {
    @Test fun profilesCreatedBeforeJerseyCustomizationKeepDefaultNumber() {
        val migrated = Gson().fromJson("""{"playerName":"Jordan"}""", SaveState::class.java)
        assertNull(migrated.jerseyNumber)
        assertEquals(23, displayedJerseyNumber(migrated.jerseyNumber))
    }

    @Test fun jerseyNumberIsBoundedToBasketballRosterRange() {
        assertEquals(0, displayedJerseyNumber(0))
        assertEquals(99, displayedJerseyNumber(99))
        assertEquals(0, displayedJerseyNumber(-4))
        assertEquals(99, displayedJerseyNumber(128))
    }
}
