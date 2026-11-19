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
            AvatarGear.entries.map{base.copy(gear=it)}+
            AvatarFootwear.entries.map{base.copy(footwear=it)}
        variants.forEach{assertEquals(it,AvatarAppearance.restore(it.values))}
        assertEquals(7,AvatarAppearance.keys.distinct().size)
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
    @Test fun existingSixDimensionSavesRetainEveryChoiceAndDefaultOnlyNewFootwear() {
        val old=listOf("BRAIDS","ORANGE","POWER","LIGHT","VIOLET","BOTH")
        val restored=AvatarAppearance.restore(old)
        assertEquals(old,restored.values.take(6))
        assertEquals(AvatarFootwear.ORIGINAL,restored.footwear)
        assertEquals(restored,AvatarAppearance.restore(restored.values))
    }
    @Test fun everyPoseHasAnatomicalAnchorsAndValidUnobstructedPrintQuads() {
        for(pose in PlayerCardPose.entries) {
            val layout=pose.layout
            assertEquals(8,layout.print.size);assertEquals(6,layout.arm.size)
            assertTrue(layout.faceY in 50f..250f)
            layout.print.forEach{assertTrue(it in 100f..700f)}
            assertTrue(layout.print[0]<layout.print[2])
            assertTrue(layout.shoes[0]<layout.shoes[2] && layout.shoes[1]<layout.shoes[3])
        }
        assertEquals(529f,AvatarGeometry.x(PlayerCardPose.FOLLOWTHROUGH,AvatarBuild.POWER,529f,201f),.001f)
    }
}
