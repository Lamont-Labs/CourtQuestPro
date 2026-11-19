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
    fun draw(canvas:Canvas,pose:PlayerCardPose,avatar:AvatarAppearance,skinColor:Int,skin:Bitmap,mesh:FloatArray,hair:List<Bitmap>) {
        val p=Paint(Paint.ANTI_ALIAS_FLAG)
        val layout=pose.layout
        val head=RectF(layout.head[0],layout.head[1],layout.head[2],layout.head[3])
        val cx=head.centerX();val cy=head.centerY();val w=head.width();val h=head.height()
        canvas.save()
        canvas.rotate(layout.rotation,cx,cy)
        if(avatar.hair!=AvatarHair.ORIGINAL) {
            // Cover the previous crown using a lit scalp, never paint a flat rectangle on the head.
            p.shader=LinearGradient(head.left,head.top,head.right,head.bottom,
                intArrayOf(skinColor,Color.rgb(Color.red(skinColor)/2,Color.green(skinColor)/2,Color.blue(skinColor)/2)),null,Shader.TileMode.CLAMP)
            canvas.drawOval(head,p);p.shader=null
            val material=when(avatar.hair) {
                AvatarHair.CURLS->0;AvatarHair.AFRO->1;AvatarHair.LOCS->2;AvatarHair.BRAIDS->3;AvatarHair.TOPKNOT->3;else->null
            }
            if(material!=null) {
                val volume=if(avatar.hair==AvatarHair.AFRO)1.5f else 1.15f
                val target=RectF(cx-w*volume*.5f,head.top-h*(if(avatar.hair==AvatarHair.AFRO).48f else .15f),
                    cx+w*volume*.5f,head.top+h*.65f)
                p.isFilterBitmap=true;canvas.drawBitmap(hair[material],null,target,p)
                if(avatar.hair==AvatarHair.TOPKNOT) {
                    val bun=RectF(cx-w*.23f,head.top-h*.38f,cx+w*.23f,head.top+h*.05f)
                    canvas.save();canvas.clipPath(Path().apply{addOval(bun,Path.Direction.CW)})
                    canvas.drawBitmap(hair[2],null,bun,p);canvas.restore()
                }
            } else if(avatar.hair!=AvatarHair.BALD) {
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
