package com.courtquestpro

import java.security.MessageDigest

/** Locally sealed evidence reference. Not a server attestation or verified athletic result. */
data class QuestAwardReceipt(
    val id: String,
    val ruleVersion: Int,
    val evidenceFingerprint: String,
    val evidenceCount: Int,
    val awardedAt: String,
    val evidenceClass: String = "SELF_RECORDED",
    val syncStatus: String = "SAVED_LOCAL"
)

internal object QuestAwards {
    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    /**
     * Idempotent, append-only award issuance for evidence already present on this device.
     * Does not grant XP or mutate legacy awardedKeys. Call from the normal save path.
     */
    fun reconcile(save: SaveState, at: String): Int {
        if (runCatching { java.time.Instant.parse(at) }.isFailure) return 0
        val existing = save.questAwardReceipts.orEmpty()
        val earned = QuestMilestones.evaluate(save).filter { it.achieved }
        val newReceipts = earned.filter { milestone -> existing.none { it.id == milestone.id } }.map { milestone ->
            QuestAwardReceipt(
                id = milestone.id,
                ruleVersion = QuestMilestones.RULESET_VERSION,
                evidenceFingerprint = sha256("quest-v1|${milestone.id}|${milestone.evidenceCount}"),
                evidenceCount = milestone.evidenceCount,
                awardedAt = at
            )
        }
        if (newReceipts.isNotEmpty()) save.questAwardReceipts = (existing + newReceipts).toMutableList()
        return newReceipts.size
    }
}
