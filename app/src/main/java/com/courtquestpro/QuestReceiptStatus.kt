package com.courtquestpro

/** Pure presentation policy: keeps Quest status semantics consistent and testable. */
internal object QuestReceiptStatus {
    enum class State { IN_PROGRESS, PENDING_SAVE, VERIFIED_LOCAL, EVIDENCE_REVIEW }

    fun resolve(save: SaveState, milestone: QuestMilestones.Milestone): State {
        val receipts = save.questAwardReceipts.orEmpty().filter { it.id == milestone.id }
        if (receipts.isNotEmpty()) {
            return if (receipts.size == 1 && QuestAwards.receiptValid(save, receipts.single()))
                State.VERIFIED_LOCAL else State.EVIDENCE_REVIEW
        }
        return if (milestone.achieved) State.PENDING_SAVE else State.IN_PROGRESS
    }

    fun label(state: State, evidenceCount: Int): String = when (state) {
        State.IN_PROGRESS -> "In progress · $evidenceCount recorded"
        State.PENDING_SAVE -> "Goal reached · Receipt pending save"
        State.VERIFIED_LOCAL -> "Saved receipt checked · Local evidence matches"
        State.EVIDENCE_REVIEW -> "Evidence needs review · Saved receipt no longer matches"
    }
}
