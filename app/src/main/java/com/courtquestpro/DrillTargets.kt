package com.courtquestpro

/** Minimum total successful actions for the drill's written target.
 * Counts cannot prove left/right balance, form, or elapsed intervals.
 */
internal object DrillTargets {
    private val minimums = mapOf(
        "s1" to 15, "s2" to 15, "s3" to 20,
        "b1" to 60, "b2" to 6, "b3" to 16,
        "f1" to 10, "f2" to 16, "f3" to 12,
        "d1" to 4, "d2" to 16, "d3" to 4,
        "r1" to 10, "r2" to 10, "r3" to 10,
        "p1" to 20, "p2" to 10, "p3" to 8
    )
    fun minimum(drillId: String): Int? = minimums[drillId]
    fun eligible(drillId: String, successful: Int, attempted: Int): Boolean {
        val minimum = minimum(drillId) ?: return false
        return attempted >= successful && successful >= minimum
    }
}
