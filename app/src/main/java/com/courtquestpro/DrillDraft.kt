package com.courtquestpro

data class DrillDraft(
    val date: String,
    val slotId: String,
    val drillId: String,
    val successful: Int = 0,
    val attempted: Int = 0,
    val remainingSeconds: Int = 0,
    val targetConfirmed: Boolean = false
)
