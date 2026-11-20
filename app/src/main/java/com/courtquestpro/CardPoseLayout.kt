package com.courtquestpro

/** Authoring coordinates shared by the native layers; values are pixels on 1024px art. */
internal data class CardPoseLayout(
    val width:Float,val faceX:Float,val faceY:Float,
    val head:List<Float>,val rotation:Float,val print:List<Float>,
    val arm:List<Float>,val knees:List<Float>,val shoes:List<Float>
)
/** Pixel bounds of an authored, pose-matched head plate on the 1024px athlete. */
internal data class AvatarHeadPlateBounds(val left:Int,val top:Int,val right:Int,val bottom:Int)
internal val PlayerCardPose.headPlateBounds:AvatarHeadPlateBounds get()=when(this) {
    PlayerCardPose.STANDING->AvatarHeadPlateBounds(430,0,586,169)
    PlayerCardPose.DRIBBLING->AvatarHeadPlateBounds(449,0,621,194)
    PlayerCardPose.ACTION->AvatarHeadPlateBounds(382,0,521,162)
    PlayerCardPose.READY->AvatarHeadPlateBounds(438,10,587,161)
    PlayerCardPose.FOLLOWTHROUGH->AvatarHeadPlateBounds(474,130,561,270)
}
internal val PlayerCardPose.layout:CardPoseLayout get()=when(this) {
    PlayerCardPose.STANDING->CardPoseLayout(560f,507f,104f,listOf(469f,24f,544f,93f),0f,
        listOf(447f,224f,577f,228f,575f,358f,443f,354f),listOf(405f,235f,359f,404f,340f,442f),
        listOf(441f,719f,588f,719f),listOf(300f,775f,720f,1024f))
    PlayerCardPose.DRIBBLING->CardPoseLayout(780f,538f,119f,listOf(489f,23f,584f,105f),12f,
        listOf(440f,233f,580f,262f,540f,390f,402f,359f),listOf(407f,235f,352f,445f,340f,553f),
        listOf(389f,690f,646f,715f),listOf(150f,760f,810f,1024f))
    PlayerCardPose.ACTION->CardPoseLayout(580f,457f,98f,listOf(418f,15f,482f,77f),-14f,
        listOf(423f,301f,525f,273f,536f,346f,422f,373f),listOf(396f,195f,438f,262f,552f,206f),
        listOf(420f,701f,564f,454f),listOf(270f,520f,780f,1030f))
    PlayerCardPose.READY->CardPoseLayout(560f,514f,101f,listOf(476f,39f,550f,102f),0f,
        listOf(449f,224f,576f,226f,581f,360f,443f,360f),listOf(407f,221f,365f,316f,410f,408f),
        listOf(432f,716f,599f,716f),listOf(310f,775f,700f,1000f))
    PlayerCardPose.FOLLOWTHROUGH->CardPoseLayout(500f,529f,201f,listOf(493f,143f,565f,210f),0f,
        listOf(475f,311f,572f,319f,576f,420f,463f,412f),listOf(453f,282f,440f,200f,441f,89f),
        listOf(445f,721f,578f,721f),listOf(345f,775f,670f,1000f))
}
