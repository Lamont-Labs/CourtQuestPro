package com.courtquestpro

/** One route for Court and receipts, so the next-drill preview matches Continue. */
internal fun nextDailySlot(save: SaveState): Int? {
    val pending = save.slots.indices.filter { !save.slots[it].complete }
    val catalog = TrainingLibrary.catalog(save)
    return pending.firstOrNull { index ->
        val slot = save.slots[index]
        val drill = catalog.firstOrNull { it.id == slot.drillId } ?: return@firstOrNull false
        DrillDraftRecovery.recover(save.drillDrafts.orEmpty(), save.activeDate, slot,
            drill.minutes.coerceAtLeast(1) * 60) != null
    } ?: pending.firstOrNull()
}
