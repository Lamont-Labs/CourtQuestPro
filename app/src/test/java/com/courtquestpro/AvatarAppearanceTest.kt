package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class AvatarAppearanceTest {
    @Test fun missingOrUnknownChoicesKeepOldAvatarInsteadOfLosingTheCard() {
        assertEquals(AvatarAppearance(),AvatarAppearance.restore(emptyList()))
        assertEquals(AvatarAppearance(),AvatarAppearance.restore(List(6){"future-value"}))
        val current=AvatarAppearance(AvatarHair.BRAIDS,AvatarHeadwear.WHITE,AvatarBuild.POWER,
            AvatarSkin.DEEP,AvatarShoes.ORANGE,AvatarGear.BOTH)
        assertEquals(current,AvatarAppearance.restore(current.values))
    }
    @Test fun eachAppearanceDimensionRoundTripsIndependently() {
        val base=AvatarAppearance()
        val variants=AvatarHair.entries.map{base.copy(hair=it)}+
            AvatarHeadwear.entries.map{base.copy(headwear=it)}+
            AvatarBuild.entries.map{base.copy(build=it)}+
            AvatarSkin.entries.map{base.copy(skin=it)}+
            AvatarShoes.entries.map{base.copy(shoes=it)}+
            AvatarGear.entries.map{base.copy(gear=it)}
        variants.forEach{assertEquals(it,AvatarAppearance.restore(it.values))}
        assertEquals(6,AvatarAppearance.keys.distinct().size)
    }
    @Test fun proportionsKeepHeadFeetAndDistantHandsStable() {
        for(pose in PlayerCardPose.entries)for(build in AvatarBuild.entries) {
            for(y in listOf(0f,80f,159f,750f,900f,1024f))
                assertEquals(500f,AvatarGeometry.x(pose,build,500f,y),.0001f)
            for(x in listOf(0f,1024f))assertEquals(x,AvatarGeometry.x(pose,build,x,400f),.0001f)
            val mesh=AvatarGeometry.mesh(pose,build)
            assertEquals(33*49*2,mesh.size)
            mesh.forEach{assertTrue(it.isFinite());assertTrue(it in 0f..1024f)}
            for(i in 0 until mesh.size/2)assertEquals((i/33)*1024f/48,mesh[i*2+1],.001f)
        }
    }
    @Test fun physiqueIsLocalizedAndDoesNotChangeHeightOrPlayerMeasurements() {
        assertTrue(AvatarGeometry.x(PlayerCardPose.STANDING,AvatarBuild.POWER,600f,400f)>600f)
        assertTrue(AvatarGeometry.x(PlayerCardPose.STANDING,AvatarBuild.LEAN,600f,400f)<600f)
        assertEquals(600f,AvatarGeometry.x(PlayerCardPose.STANDING,AvatarBuild.ORIGINAL,600f,400f),.001f)
    }
}
