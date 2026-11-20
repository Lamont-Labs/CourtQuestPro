package com.courtquestpro

/** Recolors a masked surface while keeping its per-pixel light and dark detail. */
internal object AvatarToneTransform {
    private const val RED_LUMA = 0.2126f
    private const val GREEN_LUMA = 0.7152f
    private const val BLUE_LUMA = 0.0722f
    private const val ORIGINAL_DETAIL = 0.08f

    /** A swatch changes actual exposure as well as hue. Local source relief stays monotonic. */
    fun surface(color: Int, baseLight: Float): FloatArray {
        val out = FloatArray(20)
        for (row in 0..2) {
            val channel = ((color ushr (16 - row*8)) and 255).toFloat()
            val gain = channel * .75f / baseLight
            val offset = row * 5
            out[offset] = gain * RED_LUMA
            out[offset+1] = gain * GREEN_LUMA
            out[offset+2] = gain * BLUE_LUMA
            out[offset+row] += .08f
            out[offset+4] = channel * .18f
        }
        out[18] = 1f
        return out
    }

    fun matrix(color: Int): FloatArray {
        val red = ((color ushr 16) and 0xff) / 255f
        val green = ((color ushr 8) and 0xff) / 255f
        val blue = (color and 0xff) / 255f
        val targetLuma = RED_LUMA * red + GREEN_LUMA * green + BLUE_LUMA * blue
        require(targetLuma > 0f) { "Skin/shoe target must have non-zero luminance" }
        val preserve = 1f - ORIGINAL_DETAIL
        val r = floatArrayOf(
            preserve * red * RED_LUMA / targetLuma + ORIGINAL_DETAIL,
            preserve * red * GREEN_LUMA / targetLuma,
            preserve * red * BLUE_LUMA / targetLuma, 0f, 0f
        )
        val g = floatArrayOf(
            preserve * green * RED_LUMA / targetLuma,
            preserve * green * GREEN_LUMA / targetLuma + ORIGINAL_DETAIL,
            preserve * green * BLUE_LUMA / targetLuma, 0f, 0f
        )
        val b = floatArrayOf(
            preserve * blue * RED_LUMA / targetLuma,
            preserve * blue * GREEN_LUMA / targetLuma,
            preserve * blue * BLUE_LUMA / targetLuma + ORIGINAL_DETAIL, 0f, 0f
        )
        return r + g + b + floatArrayOf(0f, 0f, 0f, 1f, 0f)
    }
}
