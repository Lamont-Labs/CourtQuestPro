package com.courtquestpro

internal object DrillDraftRecovery {
    fun valid(draft: DrillDraft, date: String, slot: Slot, maxSeconds: Int): Boolean =
        draft.date == date &&
            draft.slotId == slot.id &&
            draft.drillId == slot.drillId &&
            !slot.complete &&
            draft.successful >= 0 &&
            draft.attempted >= draft.successful &&
            draft.remainingSeconds in 0..maxSeconds

    fun recover(drafts: List<DrillDraft>, date: String, slot: Slot, maxSeconds: Int): DrillDraft? =
        drafts.lastOrNull { valid(it, date, slot, maxSeconds) }

    fun prune(drafts: List<DrillDraft>, date: String, slots: List<Slot>,
              duration: (String) -> Int?): List<DrillDraft> {
        val retained = mutableMapOf<String, DrillDraft>()
        drafts.forEach { draft ->
            val slot = slots.firstOrNull { it.id == draft.slotId } ?: return@forEach
            val max = duration(slot.drillId) ?: return@forEach
            if (valid(draft, date, slot, max)) retained[slot.id] = draft
        }
        return retained.values.toList()
    }
}
