package com.courtquestpro

/** Match each word independently so queries such as "beginner layup" span fields. */
internal object DrillDiscovery {
    private val separators = Regex("[^\\p{L}\\p{N}]+")

    // Match word prefixes, not arbitrary interior substrings (e.g. pass in compass).
    // Keep punctuation-separated terms and multi-field queries supported.
    internal fun matchesFields(fields: List<String>, query: String): Boolean {
        val terms = query.trim().split(separators).filter(String::isNotBlank)
        if (terms.isEmpty()) return true
        val words = fields.flatMap { it.split(separators) }.filter(String::isNotBlank)
        return terms.all { term -> words.any { it.startsWith(term, ignoreCase = true) } }
    }

    fun matches(drill: Drill, query: String): Boolean {
        val terms = query.trim().split(separators).filter(String::isNotBlank)
        if (terms.isEmpty()) return true
        val fields = listOf(drill.title, drill.category, drill.level, drill.instructions, drill.target) +
            DrillCoachingLibrary.forDrill(drill)?.let { listOf(it.setup, it.movement, it.checkpoint, it.correction) }.orEmpty()
        return matchesFields(fields, query)
    }
}
