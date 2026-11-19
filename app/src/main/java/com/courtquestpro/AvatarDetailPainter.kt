package com.courtquestpro

import android.graphics.Canvas
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode

/** Pose-anchored native detail over original 1024px plates; no remote images or real faces. */
internal object AvatarDetailPainter {
    fun dye(color:Int):ColorMatrixColorFilter {
        val r=Color.red(color).toFloat();val g=Color.green(color).toFloat();val b=Color.blue(color).toFloat()
        fun row(v:Float)=floatArrayOf(v*.001196f,v*.002348f,v*.000456f,0f,v*.27f)
        return ColorMatrixColorFilter(row(r)+row(g)+row(b)+floatArrayOf(0f,0f,0f,1f,0f))
    }
    fun draw(canvas:Canvas,pose:PlayerCardPose,avatar:AvatarAppearance,skinColor:Int,skin:Bitmap,mesh:FloatArray) {
        val p=Paint(Paint.ANTI_ALIAS_FLAG)
        val head=when(pose) {
            PlayerCardPose.STANDING -> RectF(469f,24f,544f,93f)
            PlayerCardPose.DRIBBLING -> RectF(489f,23f,584f,105f)
            PlayerCardPose.ACTION -> RectF(418f,15f,482f,77f)
        }
        val cx=head.centerX();val cy=head.centerY();val w=head.width();val h=head.height()
        canvas.save()
        canvas.rotate(when(pose){PlayerCardPose.STANDING->0f;PlayerCardPose.DRIBBLING->12f;PlayerCardPose.ACTION->-14f},cx,cy)
        if(avatar.hair!=AvatarHair.ORIGINAL) {
            // Cover the previous crown using a lit scalp, never paint a flat rectangle on the head.
            p.shader=LinearGradient(head.left,head.top,head.right,head.bottom,
                intArrayOf(skinColor,Color.rgb(Color.red(skinColor)/2,Color.green(skinColor)/2,Color.blue(skinColor)/2)),null,Shader.TileMode.CLAMP)
            canvas.drawOval(head,p);p.shader=null
            if(avatar.hair!=AvatarHair.BALD) {
                val cap=RectF(head.left-2,head.top-2,head.right+2,head.top+h*.68f)
                if(avatar.hair==AvatarHair.CURLS) {cap.top-=10;cap.left-=5;cap.right+=5}
                p.color=Color.rgb(18,16,15);canvas.drawOval(cap,p)
                canvas.save();canvas.clipPath(Path().apply{addOval(cap,Path.Direction.CW)})
                when(avatar.hair) {
                    AvatarHair.FADE -> repeat(260) { i ->
                        p.color=if(i%3==0) 0xFF4A4035.toInt() else 0xFF29241F.toInt()
                        canvas.drawCircle(cap.left+(i*19%101)/101f*cap.width(),cap.top+(i*43%97)/97f*cap.height(),.9f,p)
                    }
                    AvatarHair.BRAIDS -> repeat(7) { i ->
                        val x=cap.left+cap.width()*(i+1)/8
                        p.style=Paint.Style.STROKE;p.strokeWidth=w*.07f;p.color=0xFF544436.toInt()
                        canvas.drawPath(Path().apply{moveTo(x,cap.bottom);cubicTo(x-w*.2f,cy,x+w*.08f,head.top,x+w*.18f,head.top-h*.05f)},p)
                        p.strokeWidth=w*.037f;p.color=0xFF12100F.toInt()
                        canvas.drawPath(Path().apply{moveTo(x+1,cap.bottom);cubicTo(x-w*.2f,cy,x+w*.08f,head.top,x+w*.18f,head.top-h*.05f)},p)
                        p.style=Paint.Style.FILL
                    }
                    else -> repeat(110) { i ->
                        val x=cap.left+(i*37%113)/113f*cap.width()
                        val y=cap.top+(i*17%107)/107f*cap.height()
                        p.color=if(i%4==0) 0xFF504236.toInt() else 0xFF221C18.toInt()
                        canvas.drawCircle(x,y,if(avatar.hair==AvatarHair.LOCS) w*.065f else w*.048f,p)
                    }
                }
                canvas.restore()
                if(avatar.hair==AvatarHair.LOCS) repeat(9) { i ->
                    p.strokeWidth=w*.075f;p.strokeCap=Paint.Cap.ROUND;p.color=if(i%2==0)0xFF3D3026.toInt() else 0xFF201A16.toInt()
                    canvas.drawLine(head.left+w*(i+.5f)/9,head.top+5,head.left+w*(i+.5f)/9-4,head.top-8-(i%3)*4,p)
                }
            }
        }
        avatar.headwear.ink?.let { ink ->
            val band=RectF(head.left,head.top+h*.58f,head.right,head.top+h*.78f)
            p.shader=LinearGradient(band.left,band.top,band.right,band.bottom,intArrayOf(ink,ink,Color.DKGRAY),null,Shader.TileMode.CLAMP)
            canvas.drawRoundRect(band,5f,5f,p);p.shader=null
            p.color=0x66FFFFFF;p.strokeWidth=1.5f
            canvas.drawLine(band.left+4,band.top+3,band.right-4,band.top+3,p)
        }
        canvas.restore()
        if(avatar.gear!=AvatarGear.NONE) {
            canvas.saveLayer(RectF(0f,0f,1024f,1024f),null)
            val points=when(pose) {
                PlayerCardPose.STANDING -> floatArrayOf(405f,235f,359f,404f,340f,442f)
                PlayerCardPose.DRIBBLING -> floatArrayOf(407f,235f,352f,445f,340f,553f)
                PlayerCardPose.ACTION -> floatArrayOf(396f,195f,438f,262f,552f,206f)
            }
            fun px(i:Int)=AvatarGeometry.x(pose,avatar.build,points[i],points[i+1])
            if(avatar.gear==AvatarGear.SLEEVE || avatar.gear==AvatarGear.BOTH) {
                val x0=px(0);val y0=points[1];val x1=px(2);val y1=points[3]
                p.strokeCap=Paint.Cap.BUTT;p.style=Paint.Style.STROKE;p.strokeWidth=72f
                p.shader=LinearGradient(x0-10,y0,x0+15,y0,intArrayOf(0xFF101316.toInt(),0xFF3A3F43.toInt(),0xFF111315.toInt()),null,Shader.TileMode.CLAMP)
                val wristX=x1+(px(4)-x1)*.86f
                val wristY=y1+(points[5]-y1)*.86f
                val sleeve=Path().apply {moveTo(x0,y0);lineTo(x1,y1);lineTo(wristX,wristY)}
                p.strokeJoin=Paint.Join.ROUND
                canvas.drawPath(sleeve,p);p.shader=null
                p.strokeWidth=2f;p.color=0xFF676D71.toInt()
                canvas.drawPath(Path().apply{moveTo(x0-10,y0);lineTo(x1-10,y1);lineTo(wristX-8,wristY)},p)
            }
            if(avatar.gear==AvatarGear.WRISTS || avatar.gear==AvatarGear.BOTH) {
                val x=px(4);val y=points[5]
                p.style=Paint.Style.FILL;p.color=0xFFE2DFD4.toInt()
                canvas.save();canvas.rotate(if(pose==PlayerCardPose.ACTION) 50f else 20f,x,y)
                canvas.drawRoundRect(RectF(x-17,y-9,x+17,y+9),3f,3f,p)
                p.color=0xFF8F8C84.toInt();canvas.drawRect(x-17,y-2,x+17,y,p);canvas.restore()
            }
            p.style=Paint.Style.FILL;p.shader=null
            p.xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            if(avatar.build==AvatarBuild.ORIGINAL) canvas.drawBitmap(skin,null,RectF(0f,0f,1024f,1024f),p)
            else canvas.drawBitmapMesh(skin,32,48,mesh,0,null,0,p)
            p.xfermode=null;canvas.restore()
        }
    }
}
