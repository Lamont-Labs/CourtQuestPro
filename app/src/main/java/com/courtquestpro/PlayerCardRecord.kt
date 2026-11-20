package com.courtquestpro

internal data class CardMetric(val label: String, val value: String?)
internal data class PlayerCardRecord(val training: List<CardMetric>, val games: List<CardMetric>, val progress: List<CardMetric>) {
    val description get() = (training + games + progress).joinToString(". ") { "${it.label}: ${it.value ?: "No record"}" }

    companion object {
        fun from(save: SaveState): PlayerCardRecord {
            val results = save.drillResults.orEmpty().filter { it.attempted > 0 && it.completed in 0..it.attempted }
            val catalog = TrainingLibrary.catalog(save).associateBy { it.id }
            val shots = results.filter { result ->
                val drill = result.drillSnapshot ?: catalog[result.drillId]
                val vocabulary = result.measurementSnapshot?.vocabulary()
                    ?: drill?.let { drillSpecificMeasurement(it) ?: measurementFor(it.category) }
                vocabulary?.successLabel == "Shots made" && vocabulary.attemptLabel == "Shots attempted"
            }
            val shotAttempts = shots.sumOf { it.attempted.toLong() }
            val games = save.matches.filter { it.playerScore >= 0 && it.opponentScore >= 0 }
            val wins = games.count { it.playerScore > it.opponentScore }
            val losses = games.count { it.playerScore < it.opponentScore }
            val ties = games.size - wins - losses
            val receipts = save.missionReceipts.orEmpty().filter { MissionTraining.receiptValid(save, it) }.distinctBy { it.sessionId }
            fun count(value: Number, evidence: Boolean) = value.toString().takeIf { evidence }
            return PlayerCardRecord(
                training = listOf(
                    CardMetric("Saved drills", count(results.size, results.isNotEmpty())),
                    CardMetric("Attempts", count(results.sumOf { it.attempted.toLong() }, results.isNotEmpty())),
                    CardMetric("Successful reps", count(results.sumOf { it.completed.toLong() }, results.isNotEmpty())),
                    CardMetric("Shooting accuracy", if (shotAttempts > 0) "${(shots.sumOf { it.completed.toLong() } * 100 + shotAttempts/2) / shotAttempts}%" else null),
                    CardMetric("Completed missions", count(receipts.size, receipts.isNotEmpty()))),
                games = listOf(CardMetric("Games played", count(games.size, games.isNotEmpty())),
                    CardMetric("Wins / losses", if (games.isNotEmpty()) "$wins / $losses" else null)) +
                    (if (ties > 0) listOf(CardMetric("Tied games", ties.toString())) else emptyList()),
                progress = listOf(CardMetric("Lifetime XP", save.totalXp.toString()), CardMetric("Rank", rankFor(save.totalXp)),
                    CardMetric("Best streak", save.longestStreak.takeIf { it > 0 }?.let { "$it d" }))
            )
        }
    }
}
