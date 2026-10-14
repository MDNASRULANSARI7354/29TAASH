package com.example.model

data class GameSettings(
    val minBid: Int = 16,
    val maxBid: Int = 28,
    val targetScore: Int = 6,
    val practiceMode: Boolean = true,
    val hiddenTrumpEnabled: Boolean = true,
    val pairBonusEnabled: Boolean = true,
    val soundEffects: Boolean = true,
    val vibration: Boolean = true,
    val aiSpeedMs: Long = 700L,
    val autoAdvanceTrickMs: Long = 1400L
)
