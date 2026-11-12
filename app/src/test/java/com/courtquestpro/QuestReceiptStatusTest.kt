package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class QuestReceiptStatusTest {
    private fun saved(): SaveState = SaveState(drillResults = mutableListOf(
        DrillResult("2026-11-12", "one", "s1", 5, 10)
    ))
    private fun first(save: SaveState) = QuestMilestones.evaluate(save)
        .first { it.id == "first_recorded_drill_v1" }

    @Test fun noEvidenceIsInProgress() {
        val save = SaveState()
        assertEquals(QuestReceiptStatus.State.IN_PROGRESS,
            QuestReceiptStatus.resolve(save, first(save)))
    }

    @Test fun earnedWithoutReceiptIsPendingSave() {
        val save = saved()
        assertEquals(QuestReceiptStatus.State.PENDING_SAVE,
            QuestReceiptStatus.resolve(save, first(save)))
    }

    @Test fun validReceiptIsLocallyVerified() {
        val save = saved()
        QuestAwards.reconcile(save, "2026-11-12T20:00:00Z")
        assertEquals(QuestReceiptStatus.State.VERIFIED_LOCAL,
            QuestReceiptStatus.resolve(save, first(save)))
    }

    @Test fun modifiedEvidenceRequiresReview() {
        val save = saved()
        QuestAwards.reconcile(save, "2026-11-12T20:00:00Z")
        save.drillResults!![0] = save.drillResults!![0].copy(attempted = 11)
        assertEquals(QuestReceiptStatus.State.EVIDENCE_REVIEW,
            QuestReceiptStatus.resolve(save, first(save)))
    }

    @Test fun duplicateReceiptsRequireReview() {
        val save = saved()
        QuestAwards.reconcile(save, "2026-11-12T20:00:00Z")
        save.questAwardReceipts!!.add(save.questAwardReceipts!!.single())
        assertEquals(QuestReceiptStatus.State.EVIDENCE_REVIEW,
            QuestReceiptStatus.resolve(save, first(save)))
    }
}
