package com.courtquestpro

/** Recolors a masked surface while keeping its per-pixel light and dark detail. */
internal object AvatarToneTransform {
    private const val RED_LUMA = 0.2126f
    private const val GREEN_LUMA = 0.7152f
    private const val BLUE_LUMA = 0.0722f
    private const val ORIGINAL_DETAIL = 0.08f

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
