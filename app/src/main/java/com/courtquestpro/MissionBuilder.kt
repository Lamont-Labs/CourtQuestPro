package com.courtquestpro

import java.time.Instant
import java.util.Collections
import java.util.Random
import java.util.UUID

internal object MissionBuilder {
    const val algorithm = "courtquest-solo-v1"
    val equipmentOptions = listOf("Ball", "Hoop", "Partner")
    val spaces = listOf("Small space", "Half court")
    private data class Needs(val equipment: Set<String>, val court: Boolean = false)
    // Conservative, authored metadata; custom free text is never treated as an equipment declaration.
    private val needs = mapOf(
        "s1" to Needs(setOf("Ball", "Hoop"), true), "s2" to Needs(setOf("Ball", "Hoop"), true), "s3" to Needs(setOf("Ball", "Hoop"), true),
        "b1" to Needs(setOf("Ball")), "b2" to Needs(setOf("Ball"), true), "b3" to Needs(setOf("Ball"), true),
        "f1" to Needs(setOf("Ball", "Hoop"), true), "f2" to Needs(setOf("Ball", "Hoop"), true), "f3" to Needs(setOf("Ball", "Hoop"), true),
        "d1" to Needs(emptySet()), "d2" to Needs(emptySet()), "d3" to Needs(setOf("Partner")),
        "r1" to Needs(setOf("Partner")), "r2" to Needs(setOf("Ball", "Partner"), true), "r3" to Needs(setOf("Ball", "Hoop", "Partner"), true),
        "p1" to Needs(setOf("Ball", "Partner")), "p2" to Needs(setOf("Ball", "Partner"), true), "p3" to Needs(setOf("Ball", "Partner"), true))

    fun snapshot(drill: Drill, id: String = "step_${UUID.randomUUID()}", required: Boolean = false): MissionStep? {
        val minimum = DrillTargets.minimum(drill) ?: return null
        if (drill.minutes !in 1..60) return null
        val vocabulary = measurementFor(drill.category)
        return MissionStep(id, drill.copy(), MissionMeasurement("success-attempt-v1", minimum,
            vocabulary.successLabel, vocabulary.attemptLabel, vocabulary.successButton, vocabulary.description), required)
    }
    fun ensureDraft(save: SaveState): MissionBuilderDraft = save.missionDraft ?: MissionBuilderDraft(
        "mission_${UUID.randomUUID()}", "My Mission", emptyList(),
        constraints = MissionConstraints(maxLevel = save.difficulty.takeIf { it in TrainingLibrary.levels } ?: "Beginner"))
        .also { save.missionDraft = it }
    fun rename(save: SaveState, title: String) { save.missionDraft = ensureDraft(save).copy(title = title.take(60)) }
    fun constraints(save: SaveState, inputs: MissionConstraints) { save.missionDraft = ensureDraft(save).copy(constraints = inputs) }
    fun newDraft(save: SaveState) { save.missionDraft = null; ensureDraft(save) }
    fun add(save: SaveState, drillId: String): Boolean {
        val draft = ensureDraft(save)
        if (draft.steps.size >= 12 || draft.steps.any { it.drill.id == drillId }) return false
        val drill = TrainingLibrary.available(save).firstOrNull { it.id == drillId } ?: return false
        val step = snapshot(drill) ?: return false
        save.missionDraft = draft.copy(steps = draft.steps + step, generation = null)
        return true
    }
    fun remove(save: SaveState, stepId: String): Boolean {
        val draft = ensureDraft(save)
        if (draft.steps.none { it.id == stepId }) return false
        save.missionDraft = draft.copy(steps = draft.steps.filterNot { it.id == stepId }, generation = null)
        return true
    }
    fun move(save: SaveState, stepId: String, delta: Int): Boolean {
        val draft = ensureDraft(save); val index = draft.steps.indexOfFirst { it.id == stepId }; val target = index + delta
        if (index < 0 || delta !in listOf(-1, 1) || target !in draft.steps.indices) return false
        val steps = draft.steps.toMutableList(); Collections.swap(steps, index, target)
        save.missionDraft = draft.copy(steps = steps.toList(), generation = null)
        return true
    }
    fun toggleRequired(save: SaveState, stepId: String) {
        val draft = ensureDraft(save)
        save.missionDraft = draft.copy(steps = draft.steps.map { if (it.id == stepId) it.copy(required = !it.required) else it }, generation = null)
    }
    fun open(save: SaveState, versionId: String): Boolean {
        val version = save.missionVersions.orEmpty().firstOrNull { it.id == versionId && it.source in listOf("PERSONAL", "RECOMMENDED") } ?: return false
        save.missionDraft = MissionBuilderDraft(version.missionId, version.title, version.steps.toList(), version.id,
            version.generation, version.generation?.inputs ?: MissionConstraints(maxLevel = save.difficulty))
        return true
    }
    fun savedVersion(save: SaveState): MissionVersion? {
        val draft = save.missionDraft ?: return null
        return save.missionVersions.orEmpty().firstOrNull { it.id == draft.baseVersionId && it.title == draft.title.trim() &&
            it.steps == draft.steps && it.generation == draft.generation }
    }
    fun publish(save: SaveState, at: String): MissionVersion? {
        val draft = ensureDraft(save)
        if (draft.title.trim().isEmpty() || draft.steps.size !in 1..12 ||
            draft.steps.distinctBy { it.id }.size != draft.steps.size ||
            draft.steps.distinctBy { it.drill.id }.size != draft.steps.size ||
            draft.steps.any { it.measurement.minimumSuccesses !in 1..500 || it.drill.minutes !in 1..60 } ||
            runCatching { Instant.parse(at) }.isFailure) return null
        savedVersion(save)?.let { return it }
        val number = (save.missionVersions.orEmpty().filter { it.missionId == draft.missionId }.maxOfOrNull { it.version } ?: 0) + 1
        val version = MissionVersion("${draft.missionId}:v$number", draft.missionId, number, draft.title.trim(),
            if (draft.generation == null) "PERSONAL" else "RECOMMENDED", draft.steps.toList(), at, draft.generation)
        save.missionVersions = (save.missionVersions.orEmpty() + version).toMutableList()
        save.missionDraft = draft.copy(title = version.title, baseVersionId = version.id)
        return version
    }
    private fun fits(step: MissionStep, input: MissionConstraints): Boolean {
        val requirement = needs[step.drill.id] ?: return false
        return (input.skill == "Any skill" || step.drill.category == input.skill) &&
            TrainingLibrary.levels.indexOf(step.drill.level) in 0..TrainingLibrary.levels.indexOf(input.maxLevel) &&
            input.equipment.containsAll(requirement.equipment) && (!requirement.court || input.space == "Half court")
    }
    fun generate(catalog: List<Drill>, inputs: MissionConstraints, seed: Long, required: List<MissionStep> = emptyList()): MissionGeneration {
        if (inputs.minutes !in 5..120 || inputs.skill !in (listOf("Any skill") + TrainingLibrary.categories) ||
            inputs.maxLevel !in TrainingLibrary.levels || inputs.space !in spaces || inputs.equipment.any { it !in equipmentOptions })
            return MissionGeneration(emptyList(), error = "Choose a skill, 5–120 minutes, available equipment, space and level.")
        val candidates = catalog.distinctBy { it.id }.sortedBy { it.id }.mapNotNull { snapshot(it, "generated_${seed}_${it.id}") }
            .filter { fits(it, inputs) }
        return replay(MissionGenerationProvenance(algorithm, seed, inputs.copy(equipment = inputs.equipment.sorted()), candidates, required.toList()))
    }
    fun replay(provenance: MissionGenerationProvenance): MissionGeneration {
        val inputs = provenance.inputs; val required = provenance.requiredSteps
        if (provenance.algorithmVersion != algorithm) return MissionGeneration(emptyList(), error = "This generator version is unavailable. Keep your saved Mission or build one manually.")
        if (required.size > 12 || required.distinctBy { it.drill.id }.size != required.size ||
            required.any { !fits(it, inputs) } || required.sumOf { it.drill.minutes } > inputs.minutes)
            return MissionGeneration(emptyList(), error = "Your required drills do not fit. Add time, equipment or space, change the skill, or unpin a drill.")
        val choices = provenance.candidates.filter { fits(it, inputs) && required.none { r -> r.drill.id == it.drill.id } }.toMutableList()
        Collections.shuffle(choices, Random(provenance.seed))
        val selected = required.toMutableList(); var remaining = inputs.minutes - selected.sumOf { it.drill.minutes }
        choices.forEach { step -> if (step.drill.minutes <= remaining && selected.size < 12) { selected += step; remaining -= step.drill.minutes } }
        if (selected.isEmpty()) return MissionGeneration(emptyList(), error = "No drills fit yet. Add time or equipment, choose more space, or change the skill.")
        return MissionGeneration(selected.toList(), provenance)
    }
    fun generateIntoDraft(save: SaveState, inputs: MissionConstraints, seed: Long): MissionGeneration {
        val draft = ensureDraft(save)
        val generated = generate(TrainingLibrary.available(save), inputs, seed, draft.steps.filter { it.required })
        if (generated.error == null) save.missionDraft = draft.copy(steps = generated.steps, generation = generated.provenance, constraints = inputs)
        return generated
    }
}
