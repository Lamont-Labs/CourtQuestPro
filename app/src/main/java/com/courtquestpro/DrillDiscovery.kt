package com.courtquestpro

/** Match each word independently so queries such as "beginner layup" span fields. */
internal object DrillDiscovery {
    private val separators = Regex("[^\\p{L}\\p{N}]+")

    fun matches(drill: Drill, query: String): Boolean {
        val terms = query.trim().split(separators).filter(String::isNotBlank)
        if (terms.isEmpty()) return true
        val fields = listOf(drill.title, drill.category, drill.level, drill.instructions, drill.target) +
            DrillCoachingLibrary.forDrill(drill)?.let { listOf(it.setup, it.movement, it.checkpoint, it.correction) }.orEmpty()
        return terms.all { term -> fields.any { it.contains(term, ignoreCase = true) } }
    }
}
