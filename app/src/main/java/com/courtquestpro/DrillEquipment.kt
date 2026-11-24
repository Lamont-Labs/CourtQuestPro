package com.courtquestpro

/** Explicit equipment requirements for built-in drills; never infer custom equipment. */
internal object DrillEquipment {
    private val equipment = mapOf(
        "s1" to "Basketball · Hoop", "s2" to "Basketball · Hoop", "s3" to "Basketball · Hoop",
        "b1" to "Basketball · Clear space", "b2" to "Basketball · Court lane", "b3" to "Basketball · Clear lane",
        "f1" to "Basketball · Hoop", "f2" to "Basketball · Hoop · Cone or court line", "f3" to "Basketball · Hoop",
        "d1" to "Clear space · No ball required", "d2" to "Clear court · No ball required", "d3" to "Clear court · No ball required",
        "r1" to "Clear court · Basketball reference", "r2" to "Basketball · Clear outlet lane", "r3" to "Basketball · Hoop",
        "p1" to "Basketball · Wall target", "p2" to "Basketball · Wall target · Clear lane", "p3" to "Basketball · Wall targets"
    )
    fun label(id: String): String? = equipment[id]
    fun searchTerms(id: String): List<String> = equipment[id]?.let { listOf(it) }.orEmpty()
}
