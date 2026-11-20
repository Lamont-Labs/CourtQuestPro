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
        return ColorMatrixColorFilter(AvatarToneTransform.matrix(color))
    }
    fun draw(canvas:Canvas,pose:PlayerCardPose,avatar:AvatarAppearance,skin:Bitmap,mesh:FloatArray) {
        val p=Paint(Paint.ANTI_ALIAS_FLAG)
        val layout=pose.layout
        val head=RectF(layout.head[0],layout.head[1],layout.head[2],layout.head[3])
        val cx=head.centerX();val cy=head.centerY();val w=head.width();val h=head.height()
        canvas.save()
        canvas.rotate(layout.rotation,cx,cy)
        avatar.headwear.ink?.let { ink ->
            val band=RectF(head.left,head.top+h*.58f,head.right,head.top+h*.78f)
            p.shader=LinearGradient(band.left,band.top,band.right,band.bottom,intArrayOf(ink,ink,Color.DKGRAY),null,Shader.TileMode.CLAMP)
            canvas.drawRoundRect(band,5f,5f,p);p.shader=null
            p.color=0x66FFFFFF;p.strokeWidth=1.5f
            canvas.drawLine(band.left+4,band.top+3,band.right-4,band.top+3,p)
            if(avatar.headwear==AvatarHeadwear.WRAP) {
                p.color=ink
                canvas.drawPath(Path().apply{moveTo(head.right-4,band.top);lineTo(head.right+19,band.bottom+20);lineTo(head.right+7,band.bottom+23);lineTo(head.right-7,band.bottom);close()},p)
                p.color=0xFF72777C.toInt();p.strokeWidth=1f
                canvas.drawLine(head.right+2,band.bottom,head.right+10,band.bottom+18,p)
            }
        }
        canvas.restore()
        if(avatar.gear!=AvatarGear.NONE) {
            canvas.saveLayer(RectF(0f,0f,1024f,1024f),null)
            val points=layout.arm
            fun px(i:Int)=AvatarGeometry.x(pose,avatar.build,points[i],points[i+1])
            if(avatar.gear in listOf(AvatarGear.SLEEVE,AvatarGear.BOTH,AvatarGear.FULL)) {
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
            if(avatar.gear in listOf(AvatarGear.WRISTS,AvatarGear.BOTH,AvatarGear.FULL)) {
                val x=px(4);val y=points[5]
                p.style=Paint.Style.FILL;p.color=0xFFE2DFD4.toInt()
                canvas.save();canvas.rotate(if(pose==PlayerCardPose.ACTION) 50f else 20f,x,y)
                canvas.drawRoundRect(RectF(x-17,y-9,x+17,y+9),3f,3f,p)
                p.color=0xFF8F8C84.toInt();canvas.drawRect(x-17,y-2,x+17,y,p);canvas.restore()
            }
            if(avatar.gear==AvatarGear.KNEES || avatar.gear==AvatarGear.FULL) {
                for(i in listOf(0,2)) {
                    val x=AvatarGeometry.x(pose,avatar.build,layout.knees[i],layout.knees[i+1]);val y=layout.knees[i+1]
                    p.style=Paint.Style.FILL;p.shader=LinearGradient(x-27,y,x+27,y,
                        intArrayOf(0xFF15181C.toInt(),0xFF4B5056.toInt(),0xFF101215.toInt()),null,Shader.TileMode.CLAMP)
                    canvas.drawRoundRect(RectF(x-31,y-32,x+31,y+32),12f,12f,p);p.shader=null
                    p.color=0xFFB1B5BA.toInt();p.strokeWidth=1.5f;canvas.drawLine(x-22,y-22,x+22,y-22,p)
                }
            }
            p.style=Paint.Style.FILL;p.shader=null
            p.xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            if(avatar.build==AvatarBuild.ORIGINAL) canvas.drawBitmap(skin,null,RectF(0f,0f,1024f,1024f),p)
            else canvas.drawBitmapMesh(skin,32,48,mesh,0,null,0,p)
            p.xfermode=null;canvas.restore()
        }
    }
}
