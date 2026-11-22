package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class JerseyDesignTest {
    private val gson = Gson()

    @Test fun numberValidationPreservesDoubleZero() {
        (0..99).forEach { assertEquals(it.toString(), parseJerseyNumber(it.toString())) }
        assertEquals("00", parseJerseyNumber("00"))
        (0..9).forEach { assertEquals("0$it", parseJerseyNumber("0$it")) }
        listOf("", "000", "100", "-1", " 7", "١", "2a").forEach {
            assertNull(parseJerseyNumber(it))
        }
    }

    @Test fun legacySaveRoundTripsWithoutChangingExistingPlayerData() {
        val loaded = gson.fromJson(
            """{"playerName":"Rey","jerseyNumber":9,"totalXp":120,"matches":[{"player":"Rey","opponent":"Ali","playerScore":11,"opponentScore":7}],"favoriteDrillIds":["s1"]}""",
            SaveState::class.java
        )
        assertEquals("9", loaded.playerJersey.number)
        assertEquals(JerseyDesign.arena(), loaded.playerJersey.design)
        assertEquals("Rey", loaded.playerJersey.name)
        assertFalse(loaded.updatePlayerJersey("", "00", JerseyDesign()))
        assertFalse(loaded.updatePlayerJersey("Rey", "100", JerseyDesign()))
        assertEquals(9, loaded.jerseyNumber)

        val uniform = JerseyPart.entries.fold(JerseyDesign(templateId = JerseyTemplate.FASTBREAK.name)) { design, part ->
            design.withColor(part, 0x338050 + part.ordinal)
        }
        assertTrue(loaded.updatePlayerJersey("Rey Cruz", "00", uniform))
        val restored = gson.fromJson(gson.toJson(loaded), SaveState::class.java)
        assertEquals(PlayerJersey(uniform, "00", "Rey Cruz"), restored.playerJersey)
        assertEquals(0, restored.jerseyNumber)
        assertEquals(120, restored.totalXp)
        assertEquals(11, restored.matches.single().playerScore)
        assertEquals("Rey", restored.matches.single().player)
        assertEquals(listOf("s1"), restored.favoriteDrillIds)
        assertTrue(restored.updatePlayerJersey("Rey Cruz", "0", uniform))
        assertEquals("0", restored.playerJersey.number)
        assertTrue(restored.updatePlayerJersey("Rey Cruz", "", uniform))
        assertNull(restored.playerJersey.number)
    }

    @Test fun everyColorChangesIndependentlyAndUsesOpaqueHex() {
        val start = JerseyDesign()
        val navy = parseJerseyColor("#123aBC")!!
        assertEquals("#123ABC", jerseyHex(navy))
        listOf("abc", "#12", "#1234G0", "#123456AA", "0x1234", "").forEach {
            assertNull(parseJerseyColor(it))
        }
        JerseyPart.entries.forEach { edited ->
            val updated = start.withColor(edited, navy)
            JerseyPart.entries.forEach {
                assertEquals(if (edited == it) navy else start.color(it), updated.color(it))
            }
        }
    }

    @Test fun contrastWarningsIdentifyTheAffectedLettering() {
        val start = JerseyDesign()
        assertTrue(start.lowContrastParts().isEmpty())
        assertEquals(listOf(JerseyPart.NUMBER), start.withColor(JerseyPart.NUMBER, start.fabric).lowContrastParts())
        assertEquals(listOf(JerseyPart.NAME), start.withColor(JerseyPart.NAME, start.fabric).lowContrastParts())
        assertEquals(JerseyTemplate.CLASSIC, start.copy(templateId = "INCOMPATIBLE_STYLE").template)
    }

    @Test fun v56JerseyAndAllOtherFieldsSurviveEditingAndLeadingZeros() {
        val legacy = """{"playerName":"Lee","jerseyNumber":0,"jerseyDoubleZero":true,"totalXp":91,"jerseyDesign":{"templateId":"RETRO","fabric":-12566464,"secondary":-2,"side":-3,"collar":-4,"armhole":-5,"numberInk":-6,"nameInk":-7}}"""
        val save = gson.fromJson(legacy, SaveState::class.java)
        val old = save.playerJersey
        assertEquals("00", old.number)
        assertEquals("COURTQUEST", old.design.chestText)
        assertEquals(JerseyTemplate.RETRO, old.design.template)
        assertEquals(listOf(-12566464,-2,-3,-4,-5,-6,-7), JerseyPart.entries.map(old.design::color))
        val design = old.design.copy(chestBrand = ChestBrand.CUSTOM, teamName = "  East Ridge  ")
        assertFalse(save.updatePlayerJersey("Changed", "07", design.copy(teamName = " ")))
        assertFalse(save.updatePlayerJersey("Changed", "107", design))
        assertEquals(old, save.playerJersey)
        assertTrue(save.updatePlayerJersey("Lee", "07", design))
        val next = gson.fromJson(gson.toJson(save), SaveState::class.java)
        assertEquals(7, next.jerseyNumber); assertEquals("07", next.playerJersey.number)
        assertEquals("East Ridge", next.playerJersey.design.chestText)
        assertEquals(91, next.totalXp)
        assertTrue(next.updatePlayerJersey("Lee", "7", next.playerJersey.design.copy(chestBrand = ChestBrand.NONE)))
        assertEquals("7", next.playerJersey.number); assertEquals("", next.playerJersey.design.chestText)
        assertEquals("East Ridge", next.playerJersey.design.teamName)
        next.jerseyNumber = 8
        assertEquals("8", next.playerJersey.number)
    }

    @Test fun personalColorsCannotMutateATeamUniform() {
        val team = TeamUniformPreset("team-1", JerseyDesign(), JerseyDesign(fabric = 0xFFFFFFFF.toInt()))
        val player = SaveState()
        val practiceDesign = team.home.withColor(JerseyPart.FABRIC, 0xFF123456.toInt())
        assertTrue(player.updatePlayerJersey("Avery", "14", practiceDesign))
        assertEquals(practiceDesign, player.playerJersey.design)
        assertEquals(JerseyDesign(), team.home)
        assertEquals(0xFFFFFFFF.toInt(), team.away.fabric)
    }
}
