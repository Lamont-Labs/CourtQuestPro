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
            AvatarShoes.entries.map{base.copy(shoes=it)}+HairInk.entries.map{base.copy(hairInk=it)}+
            AvatarGear.entries.map{base.copy(gear=it)}+
            AvatarFootwear.entries.map{base.copy(footwear=it)}
        variants.forEach{assertEquals(it,AvatarAppearance.restore(it.values))}
        assertEquals(8,AvatarAppearance.keys.distinct().size)
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
            assertTrue("$pose wrist band angle",layout.wristBandRotation.isFinite())
            layout.print.forEach{assertTrue(it in 100f..700f)}
            assertTrue(layout.print[0]<layout.print[2])
            assertTrue(layout.shoes[0]<layout.shoes[2] && layout.shoes[1]<layout.shoes[3])
        }
        assertEquals(529f,AvatarGeometry.x(PlayerCardPose.FOLLOWTHROUGH,AvatarBuild.POWER,529f,201f),.001f)
    }
    @Test fun dribblingArmAccessoriesFollowTheRegisteredForearmInsteadOfTheShorts() {
        val arm=PlayerCardPose.DRIBBLING.layout.arm
        assertEquals(listOf(405f,235f,320f,390f,280f,545f),arm)
        assertTrue(PlayerCardPose.DRIBBLING.layout.wristBandRotation in -360f..360f)
    }
    @Test fun footwearInspectionContainsAuthoredFeetAndHighCollarsForEveryBuild() {
        val asset=sequenceOf(java.io.File("src/main/assets/player_cards/premium-manifest.json"),
            java.io.File("app/src/main/assets/player_cards/premium-manifest.json")).first{it.isFile}
        val poses=com.google.gson.JsonParser.parseString(asset.readText()).asJsonObject.getAsJsonObject("poses")
        for(pose in PlayerCardPose.entries)for(build in AvatarBuild.entries) {
            val crop=pose.layout.shoes
            for(foot in poses.getAsJsonObject(pose.name.lowercase()).getAsJsonArray("feet")) {
                val box=foot.asJsonArray.map{it.asFloat}
                for(y in listOf(box[1]-20f,box[3]))for(x in listOf(box[0],box[2])) {
                    val actual=AvatarGeometry.x(pose,build,x,y)
                    assertTrue("$pose / $build foot outside inspection crop",actual in crop[0]..crop[2] && y in crop[1]..crop[3])
                }
            }
        }
    }
    @Test fun headPlateCropsStayAlignedToTheApprovedAthleteCoordinateSpace() {
        val asset=sequenceOf(java.io.File("src/main/assets/player_cards/head-style-manifest.json"),
            java.io.File("app/src/main/assets/player_cards/head-style-manifest.json")).first{it.isFile}
        val entries=com.google.gson.JsonParser.parseString(asset.readText()).asJsonObject
            .getAsJsonObject("assets")
        val manifest=com.google.gson.JsonParser.parseString(asset.readText()).asJsonObject
        val cleanBases=manifest.getAsJsonObject("cleanBases")
        assertEquals(PlayerCardPose.entries.size,cleanBases.size())
        assertTrue(entries.has("standing/straight"))
        assertEquals(PlayerCardPose.entries.size*(manifest.getAsJsonArray("styles").size()),entries.size())
        for(pose in PlayerCardPose.entries) {
            val bounds=pose.headPlateBounds
            for(hair in manifest.getAsJsonArray("styles").map { AvatarHair.valueOf(it.asString.uppercase()) }) {
                val item=entries.getAsJsonObject("${pose.name.lowercase()}/${hair.name.lowercase()}")
                assertNotNull("$pose/$hair head plate",item)
                val size=item.getAsJsonArray("dimensions").map{it.asInt}
                assertEquals(bounds.right-bounds.left,size[0])
                assertEquals(bounds.bottom-bounds.top,size[1])
            }
        }
    }
    @Test fun everyBuiltInHaircutHasARegisteredAnonymousStudioThumbnail() {
        val manifestFile=sequenceOf(
            java.io.File("src/main/assets/player_cards/appearance-thumbnails.json"),
            java.io.File("app/src/main/assets/player_cards/appearance-thumbnails.json")
        ).first{it.isFile}
        val styles=com.google.gson.JsonParser.parseString(manifestFile.readText()).asJsonObject
            .getAsJsonObject("styles")
        assertEquals(AvatarHair.entries.size,styles.size())
        for(hair in AvatarHair.entries) {
            val item=styles.getAsJsonObject(hair.name.lowercase())
            assertNotNull("$hair thumbnail manifest entry",item)
            val path=java.io.File(manifestFile.parentFile,item.get("file").asString)
            assertTrue("$hair thumbnail asset is missing",path.isFile)
            assertEquals(128,item.getAsJsonArray("dimensions")[0].asInt)
            assertEquals(128,item.getAsJsonArray("dimensions")[1].asInt)
        }
    }
    @Test fun allPoseBundlesContainTheAlignedBlankFaceVariant() {
        val root=sequenceOf(
            java.io.File("src/main/assets/player_cards/anatomy"),
            java.io.File("app/src/main/assets/player_cards/anatomy")
        ).first{it.isDirectory}
        for(pose in PlayerCardPose.entries) {
            java.util.zip.ZipFile(java.io.File(root,"${pose.name.lowercase()}.zip")).use { zip ->
                val blank=zip.getEntry("face-blank.webp")
                assertNotNull("$pose blank face layer",blank)
                assertTrue("$pose blank face layer is empty",blank.size>0)
                val manifest=com.google.gson.JsonParser.parseString(zip.getInputStream(zip.getEntry("bounds.json"))
                    .bufferedReader().use{it.readText()}).asJsonObject
                assertTrue("$pose blank face bounds",manifest.has("face-blank"))
            }
        }
    }
    @Test fun skinAndShoeToneTransformsPreserveSourceLuminanceAndOrder() {
        val colors=AvatarSkin.entries.mapNotNull{it.color}+AvatarShoes.entries.mapNotNull{it.color}
        fun mapped(matrix:FloatArray,r:Float,g:Float,b:Float):Triple<Float,Float,Float> {
            fun output(row:Int)=matrix[row*5]*r+matrix[row*5+1]*g+matrix[row*5+2]*b+matrix[row*5+4]
            return Triple(output(0),output(1),output(2))
        }
        fun luma(r:Float,g:Float,b:Float)=.2126f*r+.7152f*g+.0722f*b
        for(color in colors) {
            val matrix=AvatarToneTransform.matrix(color)
            val shadow=mapped(matrix,42f,28f,23f)
            val highlight=mapped(matrix,185f,121f,82f)
            assertTrue(luma(shadow.first,shadow.second,shadow.third)<luma(highlight.first,highlight.second,highlight.third))
            assertEquals(luma(42f,28f,23f),luma(shadow.first,shadow.second,shadow.third),.02f)
            assertEquals(luma(185f,121f,82f),luma(highlight.first,highlight.second,highlight.third),.02f)
        }
    }
}
