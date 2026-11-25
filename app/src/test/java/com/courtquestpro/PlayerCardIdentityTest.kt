package com.courtquestpro

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class PlayerCardIdentityTest {
    @Test fun previousEditionIsPreservedWithoutInventingNewPlayerData() {
        assertEquals(PlayerCardStyle.CLASSIC, PlayerCardIdentity.restore("CLASSIC",null,null).style)
        assertEquals("Leather Legacy",PlayerCardStyle.CLASSIC.label)
        assertEquals("Shattered Chrome",PlayerCardStyle.HOLOGRAPHIC.label)
        assertEquals("Street Comic",PlayerCardStyle.BATTLE.label)
        assertEquals("Arena Championship",PlayerCardStyle.OBSIDIAN.label)
        assertEquals("Court Blueprint",PlayerCardStyle.PLAYBOOK.label)
        assertEquals(5,PlayerCardStyle.entries.size)
        assertEquals("leather-legacy",PlayerCardStyle.CLASSIC.artworkId)
        assertEquals(PlayerCardStyle.PLAYBOOK,PlayerCardIdentity.restore("PLAYBOOK",null,null).style)
        assertEquals(PlayerCardPose.STANDING,PlayerCardIdentity.restore("CLASSIC",null,null).pose)
        assertEquals(PlayerCardFace.SHADOW,PlayerCardIdentity.restore("CLASSIC",null,null).face)
    }
    @Test fun invalidPreferencesHaveDeterministicSafeDefaults() {
        assertEquals(PlayerCardIdentity(),PlayerCardIdentity.restore("missing","missing","missing"))
        assertEquals(PlayerCardIdentity(),PlayerCardIdentity.restore(null,null,null))
    }
    @Test fun allOneHundredChoicesRoundTripWithoutMutatingTheUniform() {
        val save=SaveState(playerName="Avery",jerseyNumber=0,jerseyDoubleZero=true,totalXp=850,
            jerseyDesign=JerseyDesign.arena().withColor(JerseyPart.FABRIC,0xFF2259D1.toInt()))
        val gson=Gson()
        val before=gson.toJson(save)
        for(style in PlayerCardStyle.entries) for(pose in PlayerCardPose.entries) for(face in PlayerCardFace.entries) {
            val identity=PlayerCardIdentity(style,pose,face)
            assertEquals(identity,PlayerCardIdentity.restore(style.name,pose.name,face.name))
            assertEquals(before,gson.toJson(save))
        }
    }
    @Test fun liveUniformAlwaysReadsCurrentSavedStateIncludingLeadingZeroAndBranding() {
        val save=SaveState(playerName="Avery",jerseyNumber=7,jerseyNumberText="07")
        val first=save.playerJersey
        val design=JerseyDesign.arena().copy(chestBrand=ChestBrand.CUSTOM,teamName="STREET WORK")
        assertTrue(save.updatePlayerJersey("Avery Rivers","00",design))
        val restored=Gson().fromJson(Gson().toJson(save),SaveState::class.java)
        assertEquals("00",restored.playerJersey.number)
        assertEquals("Avery Rivers",restored.playerJersey.name)
        assertEquals("STREET WORK",restored.playerJersey.design.chestText)
        assertNotEquals(first,restored.playerJersey)
        assertEquals(0,restored.totalXp)
    }
}
