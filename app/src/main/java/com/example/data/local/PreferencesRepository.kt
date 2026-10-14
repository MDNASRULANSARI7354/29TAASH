package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.model.GameSettings

class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("taash29_preferences", Context.MODE_PRIVATE)

    fun loadSettings(): GameSettings {
        return GameSettings(
            minBid = prefs.getInt("pref_min_bid", 16),
            targetScore = prefs.getInt("pref_target_score", 6),
            practiceMode = prefs.getBoolean("pref_practice_mode", true),
            hiddenTrumpEnabled = prefs.getBoolean("pref_hidden_trump", true),
            pairBonusEnabled = prefs.getBoolean("pref_pair_bonus", true),
            soundEffects = prefs.getBoolean("pref_sound", true),
            vibration = prefs.getBoolean("pref_vibration", true),
            aiSpeedMs = prefs.getLong("pref_ai_speed", 700L),
            autoAdvanceTrickMs = prefs.getLong("pref_trick_advance", 1400L)
        )
    }

    fun saveSettings(settings: GameSettings) {
        prefs.edit()
            .putInt("pref_min_bid", settings.minBid)
            .putInt("pref_target_score", settings.targetScore)
            .putBoolean("pref_practice_mode", settings.practiceMode)
            .putBoolean("pref_hidden_trump", settings.hiddenTrumpEnabled)
            .putBoolean("pref_pair_bonus", settings.pairBonusEnabled)
            .putBoolean("pref_sound", settings.soundEffects)
            .putBoolean("pref_vibration", settings.vibration)
            .putLong("pref_ai_speed", settings.aiSpeedMs)
            .putLong("pref_trick_advance", settings.autoAdvanceTrickMs)
            .apply()
    }

    fun loadStats(): GameStats {
        return GameStats(
            matchesPlayed = prefs.getInt("stats_matches_played", 0),
            matchesWon = prefs.getInt("stats_matches_won", 0),
            contractsMade = prefs.getInt("stats_contracts_made", 0),
            contractsFailed = prefs.getInt("stats_contracts_failed", 0),
            highestBidWon = prefs.getInt("stats_highest_bid_won", 16),
            pairsDeclared = prefs.getInt("stats_pairs_declared", 0),
            hintsUsed = prefs.getInt("stats_hints_used", 0),
            undosUsed = prefs.getInt("stats_undos_used", 0)
        )
    }

    fun recordMatchEnd(won: Boolean) {
        val current = loadStats()
        prefs.edit()
            .putInt("stats_matches_played", current.matchesPlayed + 1)
            .putInt("stats_matches_won", current.matchesWon + if (won) 1 else 0)
            .apply()
    }

    fun recordContract(made: Boolean, bidAmount: Int) {
        val current = loadStats()
        val editor = prefs.edit()
        if (made) {
            editor.putInt("stats_contracts_made", current.contractsMade + 1)
            if (bidAmount > current.highestBidWon) {
                editor.putInt("stats_highest_bid_won", bidAmount)
            }
        } else {
            editor.putInt("stats_contracts_failed", current.contractsFailed + 1)
        }
        editor.apply()
    }

    fun recordHintUsed() {
        val current = loadStats()
        prefs.edit().putInt("stats_hints_used", current.hintsUsed + 1).apply()
    }

    fun recordUndoUsed() {
        val current = loadStats()
        prefs.edit().putInt("stats_undos_used", current.undosUsed + 1).apply()
    }
}
