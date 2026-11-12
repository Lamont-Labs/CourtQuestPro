package com.courtquestpro

import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

data class DrillInput(val title: String, val category: String, val level: String,
    val minutes: Int, val goal: Int, val instructions: String)
data class UserDrill(val drill: Drill, val familyId: String, val archived: Boolean = false)
data class PracticeSession(val id: String, val drillId: String, val date: String, val draft: DrillDraft)
data class DrillSuggestion(val drill: Drill, val reason: String)

internal object TrainingLibrary {
    val categories = listOf("Shooting", "Ball Handling", "Finishing", "Defense", "Rebounding", "Passing")
    val levels = listOf("Beginner", "Intermediate", "Advanced", "Expert", "Elite")

    /** Player count is a minimum; 3+ is represented by a minimum of three. */
    fun byPlayerCount(items: List<Drill>, players: Int): List<Drill> {
        require(players >= 1) { "Player count must be positive." }
        return items.filter { it.minimumPlayers.coerceAtLeast(1) <= players }
    }

    fun playerRequirement(drill: Drill): String = when (drill.minimumPlayers.coerceAtLeast(1)) {
        1 -> "Solo"
        2 -> "2 players"
        else -> "${drill.minimumPlayers} players minimum"
    }


    fun validation(input: DrillInput): String? = when {
        input.title.trim().length !in 1..60 -> "Give your drill a name of 1–60 characters."
        input.category !in categories -> "Choose a skill category."
        input.level !in levels -> "Choose a difficulty level."
        input.minutes !in 1..60 -> "Set planned minutes between 1 and 60."
        input.goal !in 1..500 -> "Set a target between 1 and 500 successful actions."
        input.instructions.trim().length !in 8..1500 -> "Write instructions of 8–1,500 characters."
        else -> null
    }
    private fun valid(drill: Drill): Boolean = drill.id.startsWith("custom_") &&
        validation(DrillInput(drill.title, drill.category, drill.level, drill.minutes, drill.minimumSuccesses, drill.instructions)) == null

    fun catalog(save: SaveState): List<Drill> = (drills + save.customDrills.orEmpty().map { it.drill }.filter(::valid)).distinctBy { it.id }
    fun available(save: SaveState): List<Drill> = (drills + save.customDrills.orEmpty()
        .filter { !it.archived && valid(it.drill) }.map { it.drill }).distinctBy { it.id }

    fun saveCustom(save: SaveState, input: DrillInput, editingId: String? = null): Drill? {
        if (validation(input) != null) return null
        val previous = editingId?.let { id -> save.customDrills.orEmpty().firstOrNull { it.drill.id == id && !it.archived } }
        if (editingId != null && previous == null) return null
        val id = "custom_${UUID.randomUUID()}"
        val drill = Drill(id, input.category, input.title.trim(), input.level, input.minutes,
            input.instructions.trim(), "Complete ${input.goal} successful actions with controlled technique.",
            "${input.title.trim()} basketball drill", input.goal)
        save.customDrills = (save.customDrills.orEmpty().map {
            if (it.drill.id == editingId) it.copy(archived = true) else it
        } + UserDrill(drill, previous?.familyId ?: id)).toMutableList()
        if (editingId != null && editingId in save.favoriteDrillIds.orEmpty()) {
            save.favoriteDrillIds = (save.favoriteDrillIds.orEmpty().filterNot { it == editingId } + id).distinct().toMutableList()
        }
        return drill
    }
    fun archive(save: SaveState, id: String): Boolean {
        if (save.customDrills.orEmpty().none { it.drill.id == id && !it.archived }) return false
        save.customDrills = save.customDrills.orEmpty().map { if (it.drill.id == id) it.copy(archived = true) else it }.toMutableList()
        save.favoriteDrillIds = save.favoriteDrillIds.orEmpty().filterNot { it == id }.toMutableList()
        return true
    }
    fun toggleFavorite(save: SaveState, id: String) {
        if (available(save).none { it.id == id }) return
        val current = save.favoriteDrillIds.orEmpty()
        save.favoriteDrillIds = (if (id in current) current.filterNot { it == id } else current + id).distinct().toMutableList()
    }
    private fun levelAllowed(save: SaveState, drill: Drill): Boolean =
        levels.indexOf(drill.level) in 0..levels.indexOf(save.difficulty).coerceAtLeast(0)

    fun locked(save: SaveState, slot: Slot): Boolean = slot.complete || save.drillDrafts.orEmpty().any {
        it.date == save.activeDate && it.slotId == slot.id && it.drillId == slot.drillId
    }
    fun swappableSlot(save: SaveState, drill: Drill, requestedId: String? = null): Slot? {
        if (!levelAllowed(save, drill) || available(save).none { it.id == drill.id }) return null
        val all = catalog(save)
        return save.slots.firstOrNull { slot ->
            (requestedId == null || slot.id == requestedId) && !locked(save, slot) &&
                slot.drillId != drill.id && all.firstOrNull { it.id == slot.drillId }?.category == drill.category
        }
    }
    fun swap(save: SaveState, slotId: String, drillId: String): Boolean {
        val drill = available(save).firstOrNull { it.id == drillId } ?: return false
        val slot = swappableSlot(save, drill, slotId) ?: return false
        save.slots = save.slots.map { if (it.id == slot.id) it.copy(drillId = drill.id) else it }.toMutableList()
        return true
    }
    fun shuffle(save: SaveState, random: Random = Random.Default): Int {
        var changed = 0
        val all = catalog(save)
        val available = available(save).filter { levelAllowed(save, it) }
        save.slots = save.slots.map { slot ->
            val category = all.firstOrNull { it.id == slot.drillId }?.category
            val choices = available.filter { it.category == category && it.id != slot.drillId }
            if (locked(save, slot) || choices.isEmpty()) slot
            else { changed++; slot.copy(drillId = choices.random(random).id) }
        }.toMutableList()
        return changed
    }
    fun suggestions(save: SaveState): List<DrillSuggestion> {
        val recent = save.drillResults.orEmpty().takeLast(6).map { it.drillId }.toSet()
        val favorites = save.favoriteDrillIds.orEmpty()
        return available(save).filter { levelAllowed(save, it) }
            .sortedWith(compareByDescending<Drill> { it.category == save.weeklyPlan }
                .thenBy { it.id in recent }.thenByDescending { it.id in favorites }.thenBy { it.id })
            .take(4).map { drill -> DrillSuggestion(drill, when {
                drill.category == save.weeklyPlan -> "Matches your ${save.weeklyPlan.lowercase()} focus."
                drill.id in favorites -> "A favorite at your selected level."
                drill.id !in recent -> "Adds variety to your recent training."
                else -> "Fits your selected training level."
            }) }
    }
    fun startPractice(save: SaveState, drillId: String, date: String): Boolean {
        if (MissionTraining.active(save) != null || save.practiceSession != null || runCatching { LocalDate.parse(date) }.isFailure) return false
        val drill = available(save).firstOrNull { it.id == drillId } ?: return false
        val id = "practice_${UUID.randomUUID()}"
        save.practiceSession = PracticeSession(id, drillId, date,
            DrillDraft(date, id, drillId, remainingSeconds = drill.minutes * 60))
        return true
    }
    fun updatePracticeDraft(save: SaveState, draft: DrillDraft): Boolean {
        val session = save.practiceSession ?: return false
        val drill = catalog(save).firstOrNull { it.id == session.drillId } ?: return false
        if (!DrillDraftRecovery.valid(draft, session.date, Slot(session.id, session.drillId), drill.minutes * 60)) return false
        save.practiceSession = session.copy(draft = draft)
        return true
    }
    fun finishPractice(save: SaveState, sessionId: String, date: String): DrillResult? {
        val session = save.practiceSession?.takeIf { it.id == sessionId } ?: return null
        val drill = catalog(save).firstOrNull { it.id == session.drillId } ?: return null
        val draft = session.draft
        if (runCatching { LocalDate.parse(date) }.isFailure || !draft.targetConfirmed ||
            !DrillTargets.eligible(drill, draft.successful, draft.attempted) ||
            save.drillResults.orEmpty().any { it.slotId == sessionId }) return null
        val result = MissionTraining.legacyEvidence(save, DrillResult(date, sessionId, drill.id, draft.successful, draft.attempted, true, drill), java.time.Instant.now().toString())
        save.drillResults = (save.drillResults.orEmpty() + result).toMutableList()
        save.practiceSession = null
        return result
    }
    fun discardPractice(save: SaveState, sessionId: String): Boolean {
        if (save.practiceSession?.id != sessionId) return false
        save.practiceSession = null
        return true
    }
}
