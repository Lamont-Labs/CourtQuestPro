package com.courtquestpro

import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.res.ResourcesCompat
import java.util.Locale

private data class PlayerCardRenderFrame(
    val plate: PlayerCardPlate?,
    val jersey: PlayerJersey,
    val identity: PlayerCardIdentity,
    val inspection: AvatarInspection
)

/** Real textured athlete plates; textile dyes are applied only inside pose-specific cloth masks. */
@Composable
internal fun CourtCardAthlete(
    jersey: PlayerJersey,
    identity: PlayerCardIdentity,
    modifier: Modifier = Modifier,
    inspection: AvatarInspection = AvatarInspection.NONE,
    showTestTags:Boolean = true
) {
    val context = LocalContext.current
    val font = remember(context) { ResourcesCompat.getFont(context, R.font.barlow_condensed_extra_bold) }
    var height by remember { mutableIntStateOf(0) }
    val sample = when { inspection!=AvatarInspection.NONE -> 1; height <= 260 -> 4; height <= 850 -> 2; else -> 1 }
    val pose = identity.pose
    val wornTemplate=when(jersey.design.template){JerseyTemplate.ARENA->0;JerseyTemplate.CLASSIC->1;JerseyTemplate.FASTBREAK->2;JerseyTemplate.RETRO->3}
    val hair=identity.avatar.hair
    val footwear=identity.avatar.footwear
    var plate by remember(pose, sample,wornTemplate,hair,footwear) { mutableStateOf(PlayerCardMaterials.cached(pose, sample,wornTemplate,hair,footwear)) }
    var failure by remember(pose, sample,wornTemplate,hair,footwear) { mutableStateOf(false) }
    val latestFrame = rememberUpdatedState(PlayerCardRenderFrame(plate, jersey, identity, inspection))
    LaunchedEffect(pose, sample,wornTemplate,hair,footwear) {
        failure = false
        try {
            plate = PlayerCardMaterials.load(context.assets, pose, sample,wornTemplate,hair,footwear)
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (_: java.io.IOException) {
            failure = true
        } catch (_: IllegalArgumentException) {
            // Invalid or missing decoded artwork should show the existing
            // recoverable error state instead of crashing the whole screen.
            failure = true
        } catch (_: IllegalStateException) {
            failure = true
        }
    }
    Box(modifier.onSizeChanged { height = it.height }, contentAlignment = Alignment.Center) {
        if (plate != null) {
            val poseReady=if(showTestTags) Modifier.testTag("card-athlete-${pose.name.lowercase()}-ready") else Modifier
            val selectedReady=if(showTestTags) Modifier.testTag("card-athlete-${pose.name.lowercase()}-${hair.name.lowercase()}-ready") else Modifier
            Box(Modifier.fillMaxSize().then(poseReady)) {
                Box(Modifier.fillMaxSize().then(selectedReady)
                    .drawWithCache {
                // These values are read inside drawWithCache so Compose invalidates the
                // cached drawing program when a player selects another hairstyle, pose,
                // face, jersey or inspection view. Capturing them outside this block can
                // leave one pose rendering the previous bitmap indefinitely.
                val frame=latestFrame.value
                val loaded=frame.plate ?: return@drawWithCache onDrawBehind { }
                val liveIdentity=frame.identity
                val liveJersey=frame.jersey
                val livePose=liveIdentity.pose
                val liveInspection=frame.inspection
                val layout=livePose.layout
                val viewport=when(liveInspection) {
                    AvatarInspection.HEAD -> RectF(layout.faceX-125f,layout.head[1]-38f,layout.faceX+125f,layout.head[1]+275f)
                    AvatarInspection.SHOES -> RectF(layout.shoes[0],layout.shoes[1],layout.shoes[2],layout.shoes[3])
                    AvatarInspection.NONE -> null
                }
                val factor = if(viewport!=null) minOf(size.width/viewport.width(),size.height/viewport.height())
                    else minOf(size.width/layout.width,size.height/1024f)
                val left = size.width*.5f-(viewport?.centerX()?:512f)*factor
                val top = if(viewport!=null) size.height*.5f-viewport.centerY()*factor else (size.height-1024f*factor)*.5f
                val design = liveJersey.design
                val template=when(liveJersey.design.template){
                    JerseyTemplate.ARENA->0;JerseyTemplate.CLASSIC->1
                    JerseyTemplate.FASTBREAK->2;JerseyTemplate.RETRO->3
                }
                val diffuse = textileDyes(design.fabric, design.secondary, design.side)
                val binding = textileDyes(design.collar, design.armhole)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                val avatar=liveIdentity.avatar
                val mesh=AvatarGeometry.mesh(livePose,avatar.build)
                val dest = RectF(0f, 0f, 1024f, 1024f)
                val printQuad=layout.print.toFloatArray()
                for(i in printQuad.indices step 2) printQuad[i]=AvatarGeometry.x(livePose,avatar.build,printQuad[i],printQuad[i+1])
                val warp = Matrix().apply { setPolyToPoly(floatArrayOf(0f,0f,200f,0f,200f,200f,0f,200f),0,printQuad,0,4) }
                val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = font; textAlign = Paint.Align.CENTER }
                onDrawBehind {
                    drawIntoCanvas { target ->
                        val canvas = target.nativeCanvas
                        canvas.save(); canvas.translate(left, top); canvas.scale(factor, factor)
                        fun image(index: Int, filter: android.graphics.ColorFilter? = null) {
                            paint.colorFilter = filter
                            if(avatar.build==AvatarBuild.ORIGINAL) canvas.drawBitmap(loaded.images[index], null, dest, paint)
                            else canvas.drawBitmapMesh(loaded.images[index],32,48,mesh,0,null,0,paint)
                        }
                        fun part(patch: RegisteredPatch, dye: Int?, baseLight: Float, deform: Boolean) {
                            paint.colorFilter=dye?.let { ColorMatrixColorFilter(AvatarToneTransform.surface(it,baseLight)) }
                            if (!deform || avatar.build==AvatarBuild.ORIGINAL) {
                                canvas.drawBitmap(patch.bitmap,null,patch.bounds,paint)
                            } else {
                                val columns=12; val rows=24
                                val vertices=FloatArray((columns+1)*(rows+1)*2)
                                var index=0
                                for(row in 0..rows) for(column in 0..columns) {
                                    val y=patch.bounds.top+patch.bounds.height()*row/rows
                                    val x=patch.bounds.left+patch.bounds.width()*column/columns
                                    vertices[index++]=AvatarGeometry.x(livePose,avatar.build,x,y)
                                    vertices[index++]=y
                                }
                                canvas.drawBitmapMesh(patch.bitmap,columns,rows,vertices,0,null,0,paint)
                            }
                        }
                        image(0)
                        val complexion=avatar.skin.color?.let { AvatarToneTransform.surface(it,112f) }
                        if(complexion!=null) image(11,ColorMatrixColorFilter(complexion))
                        part(loaded.shoe,avatar.shoes.color,95f,true)
                        image(2 + template * 2, diffuse)
                        image(3 + template * 2, binding)
                        canvas.save(); canvas.concat(warp)
                        fun fitted(value: String, baseline: Float, cap: Float, color: Int) {
                            if (value.isBlank()) return
                            text.textSize = cap
                            val width = text.measureText(value)
                            if (width > 185f) text.textSize = cap * 185f / width
                            text.style = Paint.Style.STROKE; text.strokeWidth = 2.2f
                            text.color = if (jerseyContrast(color, android.graphics.Color.BLACK) >= 4.5)
                                android.graphics.Color.BLACK else android.graphics.Color.WHITE
                            canvas.drawText(value,100f,baseline,text)
                            text.style = Paint.Style.FILL; text.color = color
                            canvas.drawText(value,100f,baseline,text)
                        }
                        fitted(design.chestText.uppercase(Locale.ROOT), 34f, 23f, design.nameInk)
                        liveJersey.number?.let { fitted(it, if (design.chestText.isBlank()) 156f else 171f, 122f, design.numberInk) }
                        canvas.restore()
                        image(10) // original mesh folds and specular lighting above live ink
                        val face = liveIdentity.face
                        val matrix = when (face) {
                            PlayerCardFace.SHADOW -> floatArrayOf(.64f,0f,0f,0f,0f, 0f,.63f,0f,0f,0f, 0f,0f,.64f,0f,0f, 0f,0f,0f,1f,0f)
                            PlayerCardFace.BLANK -> floatArrayOf(.96f,0f,0f,0f,3f, 0f,.96f,0f,0f,3f, 0f,0f,.96f,0f,3f, 0f,0f,0f,1f,0f)
                            PlayerCardFace.QUESTION -> floatArrayOf(.09f,0f,0f,0f,1f, 0f,.09f,0f,0f,2f, 0f,0f,.12f,0f,4f, 0f,0f,0f,1f,0f)
                            PlayerCardFace.VISOR -> floatArrayOf(.10f,0f,0f,0f,2f,0f,.12f,0f,0f,3f,0f,0f,.14f,0f,5f,0f,0f,0f,1f,0f)
                        }
                        val lighting = android.graphics.ColorMatrix(matrix)
                        val faceColor = android.graphics.ColorMatrix().apply {
                            if(complexion!=null) set(complexion)
                            postConcat(lighting)
                        }
                        val faceFilter=ColorMatrixColorFilter(faceColor)
                        if(face==PlayerCardFace.BLANK) {
                            paint.colorFilter=faceFilter
                            canvas.drawBitmap(loaded.blankFace.bitmap,null,loaded.blankFace.bounds,paint)
                            paint.colorFilter=null
                        } else image(1,faceFilter)
                        part(loaded.hair,avatar.hairInk.color,54f,false)
                        AvatarDetailPainter.draw(canvas,livePose,avatar,loaded.images[11],mesh)
                        if(face==PlayerCardFace.VISOR) {
                            val visor=Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                shader=android.graphics.LinearGradient(layout.faceX-27f,layout.faceY-12f,layout.faceX+26f,layout.faceY+5f,
                                    intArrayOf(0xFF101A27.toInt(),0xFFB1D8E7.toInt(),0xFF324659.toInt()),null,android.graphics.Shader.TileMode.CLAMP)
                            }
                            canvas.drawRoundRect(RectF(layout.faceX-27f,layout.faceY-13f,layout.faceX+27f,layout.faceY+4f),7f,7f,visor)
                        }
                        if (face == PlayerCardFace.QUESTION) {
                            text.color = android.graphics.Color.rgb(255,194,117)
                            text.style = Paint.Style.FILL; text.textSize = 59f
                            canvas.drawText("?",layout.faceX,layout.faceY+20f,text)
                        }
                        canvas.restore()
                    }
                }
                })
                if(showTestTags && inspection!=AvatarInspection.NONE)
                    Box(Modifier.fillMaxSize().testTag("avatar-inspection-ready"))
            }
        } else if (failure) {
            Text("Player artwork unavailable", color = CourtQuestDesign.textPrimary,
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("card-artwork-error"))
        } else {
            CircularProgressIndicator(color = CourtQuestDesign.accent, modifier = Modifier.testTag("card-artwork-loading"))
        }
    }
}
