package com.courtquestpro

/** Vocabulary for the same successful/attempted counters, without changing saved data. */
internal data class DrillMeasurement(
    val successLabel: String,
    val attemptLabel: String,
    val successButton: String,
    val description: String
)

/** Updated counting labels for multi-step drills, without reinterpreting historical results. */
internal fun drillSpecificMeasurement(drill: Drill): DrillMeasurement? = when (drill.id) {
    "s2", "s3" -> DrillMeasurement("Controlled shots", "Shots attempted", "+ CONTROLLED",
        "Count a balanced, controlled shot regardless of whether it goes in.")
    "f2", "f3" -> DrillMeasurement("Controlled finishes", "Finishes attempted", "+ CONTROLLED",
        "Count a controlled finish that follows the drill's footwork and release cues.")
    "d1", "d3" -> DrillMeasurement("Completed rounds", "Rounds attempted", "+ ROUND",
        "Count one completed round, not each step.")
    "r1" -> DrillMeasurement("Controlled box-outs", "Box-outs attempted", "+ CONTROLLED",
        "Count legal positioning and a rebound read, not a catch.")
    "r2" -> DrillMeasurement("Clean sequences", "Sequences attempted", "+ CLEAN",
        "Count one secure rebound and controlled outlet together.")
    "p2" -> DrillMeasurement("Clean sequences", "Sequences attempted", "+ CLEAN",
        "Count one full pass, cut and controlled return catch.")
    else -> null
}

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
