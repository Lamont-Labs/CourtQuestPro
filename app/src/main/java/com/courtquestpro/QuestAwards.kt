package com.courtquestpro

import java.security.MessageDigest

/** Local evidence snapshot, not a trusted server attestation. */
data class QuestAwardReceipt(
    val id: String,
    val ruleVersion: Int,
    val evidenceFingerprint: String,
    val evidenceCount: Int,
    val awardedAt: String,
    val evidenceClass: String = "SELF_RECORDED",
    val syncStatus: String = "SAVED_LOCAL",
    val evidenceSnapshot: List<String> = emptyList()
)

internal object QuestAwards {
    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    private fun fingerprint(id: String, version: Int, evidence: List<String>): String =
        sha256((listOf("quest-award", version.toString(), id) + evidence)
            .joinToString("") { "${it.length}:$it" })

    /** Checks snapshot integrity and whether its original evidence still exists. */
    fun receiptValid(save: SaveState, receipt: QuestAwardReceipt): Boolean {
        if (receipt.ruleVersion != QuestMilestones.RULESET_VERSION ||
            receipt.evidenceSnapshot.isEmpty() ||
            receipt.evidenceCount != receipt.evidenceSnapshot.size ||
            receipt.evidenceSnapshot != receipt.evidenceSnapshot.sorted() ||
            receipt.evidenceFingerprint != fingerprint(receipt.id, receipt.ruleVersion, receipt.evidenceSnapshot)) return false
        return QuestMilestones.evidenceSnapshot(save, receipt.id).toSet()
            .containsAll(receipt.evidenceSnapshot)
    }

    /** Idempotent issuance, preserving previously issued receipts and existing XP. */
    fun reconcile(save: SaveState, at: String): Int {
        if (runCatching { java.time.Instant.parse(at) }.isFailure) return 0
        val existing = save.questAwardReceipts.orEmpty()
        val earned = QuestMilestones.evaluate(save).filter { it.achieved }
        val newReceipts = earned.filter { milestone -> existing.none { it.id == milestone.id } }.map { milestone ->
            val evidence = QuestMilestones.evidenceSnapshot(save, milestone.id)
            QuestAwardReceipt(
                id = milestone.id,
                ruleVersion = QuestMilestones.RULESET_VERSION,
                evidenceFingerprint = fingerprint(milestone.id, QuestMilestones.RULESET_VERSION, evidence),
                evidenceCount = evidence.size,
                awardedAt = at,
                evidenceSnapshot = evidence
            )
        }
        if (newReceipts.isNotEmpty()) save.questAwardReceipts = (existing + newReceipts).toMutableList()
        return newReceipts.size
    }
}
