package com.courtquestpro

/** Vocabulary for the same successful/attempted counters, without changing saved data. */
internal data class DrillMeasurement(
    val successLabel: String,
    val attemptLabel: String,
    val successButton: String,
    val description: String
)

internal fun measurementFor(category: String): DrillMeasurement = when (category) {
    "Shooting" -> DrillMeasurement("Shots made", "Shots attempted", "+ MAKE",
        "Record every shot attempt and each basket made.")
    "Finishing" -> DrillMeasurement("Finishes made", "Finishes attempted", "+ MAKE",
        "Record each finish attempt and each successful basket.")
    "Passing" -> DrillMeasurement("Accurate passes", "Passes attempted", "+ ACCURATE",
        "Record each pass and count passes reaching the intended target.")
    "Ball Handling" -> DrillMeasurement("Clean repetitions", "Repetitions attempted", "+ CLEAN",
        "Count a clean repetition when the movement is completed under control.")
    "Defense" -> DrillMeasurement("Controlled repetitions", "Repetitions attempted", "+ CLEAN",
        "Count repetitions completed with balanced, controlled footwork.")
    "Rebounding" -> DrillMeasurement("Successful rebounds", "Rebounds attempted", "+ SECURED",
        "Count rebounds secured with balance and control.")
    else -> DrillMeasurement("Successful repetitions", "Repetitions attempted", "+ SUCCESS",
        "Record attempts and successful repetitions.")
}
