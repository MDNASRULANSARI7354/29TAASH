package com.example.data.local

data class GameStats(
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val contractsMade: Int = 0,
    val contractsFailed: Int = 0,
    val highestBidWon: Int = 16,
    val pairsDeclared: Int = 0,
    val hintsUsed: Int = 0,
    val undosUsed: Int = 0
) {
    val winRatePercent: Int
        get() = if (matchesPlayed > 0) (matchesWon * 100) / matchesPlayed else 0
}
