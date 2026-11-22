package com.courtquestpro

/** Frozen evidence vocabulary. A later catalog or measurement change cannot reinterpret a result. */
data class MissionMeasurement(val schemaId: String, val minimumSuccesses: Int, val successLabel: String,
    val attemptLabel: String, val successButton: String, val description: String) {
    internal fun vocabulary() = DrillMeasurement(successLabel, attemptLabel, successButton, description)
}
data class MissionStep(val id: String, val drill: Drill, val measurement: MissionMeasurement, val required: Boolean = false)
data class MissionConstraints(val skill: String = "Any skill", val minutes: Int = 15,
    val equipment: List<String> = listOf("Ball"), val space: String = "Small space", val maxLevel: String = "Beginner")
data class MissionGenerationProvenance(val algorithmVersion: String, val seed: Long, val inputs: MissionConstraints,
    val candidates: List<MissionStep>, val requiredSteps: List<MissionStep>)
data class MissionGeneration(val steps: List<MissionStep>, val provenance: MissionGenerationProvenance? = null, val error: String? = null)
data class MissionBuilderDraft(val missionId: String, val title: String, val steps: List<MissionStep>,
    val baseVersionId: String? = null, val generation: MissionGenerationProvenance? = null,
    val constraints: MissionConstraints = MissionConstraints())
data class MissionVersion(val id: String, val missionId: String, val version: Int, val title: String,
    val source: String, val steps: List<MissionStep>, val createdAt: String,
    val generation: MissionGenerationProvenance? = null)
data class MissionSession(val id: String, val missionVersionId: String, val state: String,
    val startedAt: String, val drafts: List<DrillDraft>)
data class MissionReceipt(val id: String, val sessionId: String, val missionVersionId: String,
    val resultIds: List<String>, val resultHash: String, val savedAt: String,
    val evidenceClass: String = "SELF_RECORDED", val syncStatus: String = "SAVED_LOCAL")
