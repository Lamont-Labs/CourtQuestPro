package com.courtquestpro

import java.util.Locale
import kotlin.math.pow

enum class JerseyTemplate(val label: String, val detail: String) {
    CLASSIC("Classic", "Scoop neck · fine varsity mesh"),
    FASTBREAK("Fastbreak", "Split V-neck · ventilated speed panels"),
    RETRO("Retro", "Square neck · heritage side tape"),
    ARENA("Arena", "Deep V-neck · sculpted pro mesh")
}

enum class ChestBrand(val label: String) {
    COURTQUEST("COURTQUEST"), CUSTOM("Custom team name"), NONE("No text")
}

enum class JerseyPart(val label: String) {
    FABRIC("Primary fabric"), SECONDARY("Secondary"), SIDE("Side panels"),
    COLLAR("Collar trim"), ARMHOLE("Armhole trim"), NUMBER("Number"), NAME("Name")
}

data class JerseyDesign(
    val templateId: String? = JerseyTemplate.CLASSIC.name,
    val fabric: Int = 0xFF141519.toInt(),
    val secondary: Int = 0xFF322522.toInt(),
    val side: Int = 0xFFB43314.toInt(),
    val collar: Int = 0xFFFF6F27.toInt(),
    val armhole: Int = 0xFFFF6F27.toInt(),
    val numberInk: Int = 0xFFFF9D55.toInt(),
    val nameInk: Int = 0xFFFFF4E8.toInt(),
    val chestBrand: ChestBrand? = ChestBrand.COURTQUEST,
    val teamName: String? = null
) {
    companion object {
        fun arena() = JerseyDesign(
            templateId = JerseyTemplate.ARENA.name,
            fabric = 0xFF17181B.toInt(),
            secondary = 0xFFF0ECE4.toInt(),
            side = 0xFFFF650E.toInt(),
            collar = 0xFFE95B17.toInt(),
            armhole = 0xFFEF6A21.toInt(),
            numberInk = 0xFFFFF1DB.toInt(),
            nameInk = 0xFFF5EEE4.toInt()
        )
    }

    val template: JerseyTemplate
        get() = JerseyTemplate.entries.firstOrNull { it.name == templateId } ?: JerseyTemplate.CLASSIC

    val branding: ChestBrand get() = chestBrand ?: ChestBrand.COURTQUEST
    val chestText: String get() = when (branding) {
        ChestBrand.COURTQUEST -> "COURTQUEST"
        ChestBrand.CUSTOM -> teamName.orEmpty().trim()
        ChestBrand.NONE -> ""
    }

    fun color(part: JerseyPart): Int = when (part) {
        JerseyPart.FABRIC -> fabric
        JerseyPart.SECONDARY -> secondary
        JerseyPart.SIDE -> side
        JerseyPart.COLLAR -> collar
        JerseyPart.ARMHOLE -> armhole
        JerseyPart.NUMBER -> numberInk
        JerseyPart.NAME -> nameInk
    }

    fun withColor(part: JerseyPart, rgb: Int): JerseyDesign {
        val opaque = rgb or 0xFF000000.toInt()
        return when (part) {
            JerseyPart.FABRIC -> copy(fabric = opaque)
            JerseyPart.SECONDARY -> copy(secondary = opaque)
            JerseyPart.SIDE -> copy(side = opaque)
            JerseyPart.COLLAR -> copy(collar = opaque)
            JerseyPart.ARMHOLE -> copy(armhole = opaque)
            JerseyPart.NUMBER -> copy(numberInk = opaque)
            JerseyPart.NAME -> copy(nameInk = opaque)
        }
    }

    fun lowContrastParts(): List<JerseyPart> = listOf(JerseyPart.NUMBER, JerseyPart.NAME)
        .filter { jerseyContrast(color(it), fabric) < 4.5 }
}

/** A team record can own these presets without writing into an individual player's profile. */
data class TeamUniformPreset(val teamId: String, val home: JerseyDesign, val away: JerseyDesign)

internal data class PlayerJersey(val design: JerseyDesign, val number: String?, val name: String)

internal val SaveState.playerJersey: PlayerJersey
    get() = PlayerJersey(
        jerseyDesign ?: JerseyDesign.arena(),
        jerseyNumber?.takeIf { it in 0..99 }?.let { numeric ->
            jerseyNumberText?.takeIf { parseJerseyNumber(it)?.toInt() == numeric }
                ?: if (numeric == 0 && jerseyDoubleZero) "00" else numeric.toString()
        },
        playerName
    )

internal fun SaveState.updatePlayerJersey(name: String, number: String, design: JerseyDesign): Boolean {
    val parsedNumber = if (number.isEmpty()) null else parseJerseyNumber(number) ?: return false
    val normalizedName = name.trim()
    if ((normalizedName != playerName.trim() && normalizedName.length > 40) ||
        (normalizedName.isBlank() && playerName.isNotBlank())) return false
    val team = design.teamName.orEmpty().trim()
    if (team.length > 32 || (design.branding == ChestBrand.CUSTOM && team.isEmpty())) return false
    playerName = normalizedName
    jerseyNumber = parsedNumber?.toInt()
    jerseyDoubleZero = parsedNumber == "00"
    jerseyNumberText = parsedNumber
    jerseyDesign = design.copy(teamName = team.ifEmpty { null })
    return true
}

internal fun parseJerseyNumber(value: String): String? =
    value.takeIf {
        it.length in 1..2 && it.all { character -> character in '0'..'9' }
    }

internal fun parseJerseyColor(value: String): Int? {
    val hex = value.trim().removePrefix("#")
    if (hex.length != 6 || !hex.all { it in '0'..'9' || it.lowercaseChar() in 'a'..'f' }) return null
    return hex.toInt(16) or 0xFF000000.toInt()
}

internal fun jerseyHex(color: Int): String =
    "#" + (color and 0x00FFFFFF).toString(16).padStart(6, '0').uppercase(Locale.ROOT)

internal fun jerseyContrast(first: Int, second: Int): Double {
    fun linear(color: Int, shift: Int): Double {
        val channel = ((color shr shift) and 255) / 255.0
        return if (channel <= 0.04045) channel / 12.92 else ((channel + 0.055) / 1.055).pow(2.4)
    }
    fun luminance(color: Int) = .2126 * linear(color, 16) + .7152 * linear(color, 8) + .0722 * linear(color, 0)
    val a = luminance(first)
    val b = luminance(second)
    return (maxOf(a, b) + .05) / (minOf(a, b) + .05)
}
