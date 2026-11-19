package com.courtquestpro

import androidx.compose.runtime.saveable.listSaver

/** Cosmetic choices, never measurements, ratings or training inputs. */
internal enum class AvatarHair(val label: String) {
    ORIGINAL("Original cut"), BALD("Shaved"), FADE("Close fade"),
    CURLS("Textured curls"), LOCS("Short locs"), BRAIDS("Braided rows"),
    AFRO("Natural volume"), TOPKNOT("Twisted topknot")
}
internal enum class AvatarHeadwear(val label: String, val ink: Int?) {
    NONE("No headwear",null), BLACK("Black headband",0xFF161719.toInt()),
    WHITE("White headband",0xFFF1EEE5.toInt()), ORANGE("Orange headband",0xFFFF5B0A.toInt()),
    WRAP("Tied performance wrap",0xFF252A30.toInt())
}
internal enum class AvatarBuild(val label: String, val width: Float) {
    ORIGINAL("Original proportions",1f), LEAN("Lean build",.86f), POWER("Power build",1.18f),
    SHOULDERS("Broad shoulders",1.30f)
}
internal enum class AvatarSkin(val label: String, val color: Int?) {
    ORIGINAL("Original complexion",null), DEEP("Deep",0xFF70442E.toInt()),
    RICH("Rich brown",0xFF925735.toInt()), WARM("Warm brown",0xFFB87A50.toInt()),
    GOLDEN("Golden",0xFFD69D70.toInt()), TAN("Tan",0xFFE6B58D.toInt()),
    LIGHT("Light",0xFFF5CCAD.toInt()), UMBER("Deep umber",0xFF4C3027.toInt()),
    COOL("Cool beige",0xFFCBA38C.toInt())
}
internal enum class AvatarShoes(val label: String, val color: Int?) {
    ORIGINAL("Original shoes",null), BLACK("Onyx",0xFF34363C.toInt()),
    WHITE("Chalk",0xFFE8E6DE.toInt()), ORANGE("Court orange",0xFFFF6922.toInt()),
    BLUE("Cobalt",0xFF346FE1.toInt()), VIOLET("Amethyst",0xFF9B68DC.toInt()),
    CRIMSON("Crimson",0xFFC62E48.toInt())
}
internal enum class AvatarFootwear(val label:String) {
    ORIGINAL("Original footwear"), LOW("Court Low · mesh and outrigger"),
    HIGH("Elevate High · padded collar"), STRAP("Lockdown · molded strap")
}
internal enum class AvatarGear(val label: String) {
    NONE("No accessories"), SLEEVE("Compression sleeve"),
    WRISTS("Wrist tape"), BOTH("Sleeve and wrist tape"), KNEES("Knee supports"),
    FULL("Complete performance kit")
}
internal enum class AvatarInspection { NONE, HEAD, SHOES }

internal data class AvatarAppearance(
    val hair: AvatarHair = AvatarHair.ORIGINAL,
    val headwear: AvatarHeadwear = AvatarHeadwear.NONE,
    val build: AvatarBuild = AvatarBuild.ORIGINAL,
    val skin: AvatarSkin = AvatarSkin.ORIGINAL,
    val shoes: AvatarShoes = AvatarShoes.ORIGINAL,
    val gear: AvatarGear = AvatarGear.NONE,
    val footwear:AvatarFootwear = AvatarFootwear.ORIGINAL
) {
    val values get() = listOf(hair.name,headwear.name,build.name,skin.name,shoes.name,gear.name,footwear.name)
    val description get() = "${hair.label}, ${headwear.label}, ${build.label}, ${skin.label}, ${shoes.label}, ${gear.label}, ${footwear.label}"
    companion object {
        val keys = listOf("hair","headwear","build","skin","shoes","gear","footwear")
        private inline fun <reified T:Enum<T>> entry(value:String?,fallback:T):T =
            enumValues<T>().firstOrNull { it.name==value } ?: fallback
        fun restore(values:List<String?>) = AvatarAppearance(
            entry(values.getOrNull(0),AvatarHair.ORIGINAL),
            entry(values.getOrNull(1),AvatarHeadwear.NONE),
            entry(values.getOrNull(2),AvatarBuild.ORIGINAL),
            entry(values.getOrNull(3),AvatarSkin.ORIGINAL),
            entry(values.getOrNull(4),AvatarShoes.ORIGINAL),
            entry(values.getOrNull(5),AvatarGear.NONE),
            entry(values.getOrNull(6),AvatarFootwear.ORIGINAL))
        val Saver = listSaver<AvatarAppearance,String>(save={it.values},restore={restore(it)})
    }
}
