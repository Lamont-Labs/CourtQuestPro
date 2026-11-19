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

/** Real textured athlete plates; textile dyes are applied only inside pose-specific cloth masks. */
@Composable
internal fun CourtCardAthlete(
    jersey: PlayerJersey,
    identity: PlayerCardIdentity,
    modifier: Modifier = Modifier,
    inspection: AvatarInspection = AvatarInspection.NONE
) {
    val context = LocalContext.current
    val font = remember(context) { ResourcesCompat.getFont(context, R.font.barlow_condensed_extra_bold) }
    var height by remember { mutableIntStateOf(0) }
    val sample = when { height <= 260 -> 4; height <= 850 -> 2; else -> 1 }
    val pose = identity.pose
    var plate by remember(pose, sample) { mutableStateOf(PlayerCardMaterials.cached(pose, sample)) }
    var failure by remember(pose, sample) { mutableStateOf(false) }
    LaunchedEffect(pose, sample) {
        try { plate = PlayerCardMaterials.load(context.assets, pose, sample) }
        catch (_: java.io.IOException) { failure = true }
    }
    Box(modifier.onSizeChanged { height = it.height }, contentAlignment = Alignment.Center) {
        val loaded = plate
        if (loaded != null) {
            Box(Modifier.fillMaxSize().testTag("card-athlete-${pose.name.lowercase()}-ready").drawWithCache {
                val viewport=when(inspection) {
                    AvatarInspection.HEAD -> when(pose) {
                        PlayerCardPose.STANDING -> RectF(380f,-20f,635f,285f)
                        PlayerCardPose.DRIBBLING -> RectF(405f,-20f,660f,285f)
                        PlayerCardPose.ACTION -> RectF(330f,-30f,585f,275f)
                    }
                    AvatarInspection.SHOES -> when(pose) {
                        PlayerCardPose.STANDING -> RectF(330f,780f,700f,1015f)
                        PlayerCardPose.DRIBBLING -> RectF(200f,765f,720f,1010f)
                        PlayerCardPose.ACTION -> RectF(290f,530f,690f,1030f)
                    }
                    AvatarInspection.NONE -> null
                }
                val factor = if(viewport!=null) minOf(size.width/viewport.width(),size.height/viewport.height())
                    else minOf(size.width / when (pose) {
                    PlayerCardPose.STANDING -> 560f
                    PlayerCardPose.DRIBBLING -> 780f
                    PlayerCardPose.ACTION -> 580f
                }, size.height / 1024f)
                val left = size.width*.5f-(viewport?.centerX()?:512f)*factor
                val top = if(viewport!=null) size.height*.5f-viewport.centerY()*factor else (size.height-1024f*factor)*.5f
                val design = jersey.design
                val template = when (design.template) {
                    JerseyTemplate.ARENA -> 0; JerseyTemplate.CLASSIC -> 1
                    JerseyTemplate.FASTBREAK -> 2; JerseyTemplate.RETRO -> 3
                }
                val diffuse = textileDyes(design.fabric, design.secondary, design.side)
                val binding = textileDyes(design.collar, design.armhole)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                val avatar=identity.avatar
                val mesh=AvatarGeometry.mesh(pose,avatar.build)
                val dest = RectF(0f, 0f, 1024f, 1024f)
                val printQuad = when (pose) {
                    PlayerCardPose.STANDING -> floatArrayOf(447f,224f,577f,228f,575f,358f,443f,354f)
                    PlayerCardPose.DRIBBLING -> floatArrayOf(440f,233f,580f,262f,540f,390f,402f,359f)
                    PlayerCardPose.ACTION -> floatArrayOf(423f,301f,525f,273f,536f,346f,422f,373f)
                }
                for(i in printQuad.indices step 2) printQuad[i]=AvatarGeometry.x(pose,avatar.build,printQuad[i],printQuad[i+1])
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
                        image(0)
                        avatar.skin.color?.let {image(11,AvatarDetailPainter.dye(it))}
                        avatar.shoes.color?.let {image(12,AvatarDetailPainter.dye(it))}
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
                        jersey.number?.let { fitted(it, if (design.chestText.isBlank()) 156f else 171f, 122f, design.numberInk) }
                        canvas.restore()
                        image(10) // original mesh folds and specular lighting above live ink
                        val face = identity.face
                        val matrix = when (face) {
                            PlayerCardFace.SHADOW -> floatArrayOf(.16f,0f,0f,0f,0f, 0f,.14f,0f,0f,0f, 0f,0f,.14f,0f,0f, 0f,0f,0f,1f,0f)
                            PlayerCardFace.BLANK -> floatArrayOf(.70f,0f,0f,0f,58f, 0f,.70f,0f,0f,45f, 0f,0f,.70f,0f,34f, 0f,0f,0f,1f,0f)
                            PlayerCardFace.QUESTION -> floatArrayOf(.09f,0f,0f,0f,1f, 0f,.09f,0f,0f,2f, 0f,0f,.12f,0f,4f, 0f,0f,0f,1f,0f)
                        }
                        val complexion=avatar.skin.color
                        image(1,if(face==PlayerCardFace.BLANK && complexion!=null)
                            AvatarDetailPainter.dye(complexion) else ColorMatrixColorFilter(matrix))
                        AvatarDetailPainter.draw(canvas,pose,avatar,avatar.skin.color?:0xFF925735.toInt(),loaded.images[11],mesh)
                        if (face == PlayerCardFace.QUESTION) {
                            val center = when (pose) {
                                PlayerCardPose.STANDING -> 507f to 104f
                                PlayerCardPose.DRIBBLING -> 538f to 119f
                                PlayerCardPose.ACTION -> 457f to 98f
                            }
                            text.color = android.graphics.Color.rgb(255,194,117)
                            text.style = Paint.Style.FILL; text.textSize = 59f
                            canvas.drawText("?",center.first,center.second+20f,text)
                        }
                        canvas.restore()
                    }
                }
            })
            if(inspection!=AvatarInspection.NONE) Box(Modifier.testTag("avatar-inspection-ready"))
        } else if (failure) {
            Text("Player artwork unavailable", color = CourtQuestDesign.textPrimary,
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("card-artwork-error"))
        } else {
            CircularProgressIndicator(color = CourtQuestDesign.accent, modifier = Modifier.testTag("card-artwork-loading"))
        }
    }
}
