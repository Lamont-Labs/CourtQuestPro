package com.courtquestpro

import kotlin.math.abs
import kotlin.math.sin

/** Mild localized proportions; head, ball and distal hands stay out of the body deformation. */
internal object AvatarGeometry {
    fun x(pose:PlayerCardPose,build:AvatarBuild,x:Float,y:Float):Float {
        if(build==AvatarBuild.ORIGINAL || y<=160f || y>=750f) return x
        val center=when(pose) {
            PlayerCardPose.STANDING -> 510f
            PlayerCardPose.DRIBBLING -> 490f-(y-300f)*.12f
            PlayerCardPose.ACTION -> 439f
        }
        val delta=x-center
        val influence=(1f-abs(delta)/220f).coerceIn(0f,1f)
        val vertical=sin((y-160f)/590f*Math.PI).toFloat()
        return x+delta*(build.width-1f)*influence*vertical
    }
    fun mesh(pose:PlayerCardPose,build:AvatarBuild,columns:Int=32,rows:Int=48):FloatArray =
        FloatArray((columns+1)*(rows+1)*2).also { vertices ->
            for(row in 0..rows) for(column in 0..columns) {
                val px=column*1024f/columns;val py=row*1024f/rows;val i=(row*(columns+1)+column)*2
                vertices[i]=x(pose,build,px,py);vertices[i+1]=py
            }
        }
}
