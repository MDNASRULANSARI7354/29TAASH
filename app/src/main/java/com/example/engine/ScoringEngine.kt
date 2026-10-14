package com.example.engine

import com.example.model.Bid
import com.example.model.GameState
import com.example.model.Seat
import com.example.model.Team

data class RoundResult(
    val bidderTeam: Team,
    val contractBid: Int,
    val effectiveTarget: Int,
    val teamARoundPoints: Int,
    val teamBRoundPoints: Int,
    val contractSucceeded: Boolean,
    val gamePointsDeltaTeamA: Int,
    val gamePointsDeltaTeamB: Int,
    val summary: String
)

object ScoringEngine {

    const val TOTAL_ROUND_POINTS = 29
    const val LAST_TRICK_BONUS = 1
    const val HIGH_BID_THRESHOLD = 21

    fun evaluateRound(gameState: GameState): RoundResult {
        val highestBid = gameState.highestBid ?: Bid(Seat.SOUTH, 16)
        val bidder = gameState.bidder ?: Seat.SOUTH
        val bidderTeam = bidder.team

        var target = highestBid.amount
        if (gameState.isPairDeclared && gameState.pairDeclaredBy != null) {
            if (gameState.pairDeclaredBy.team == bidderTeam) {
                target = maxOf(16, target - 4) // Pair reduces target for bidder
            } else {
                target = minOf(29, target + 4) // Opponent pair increases target
            }
        }

        val bidderPoints = if (bidderTeam == Team.TEAM_A) gameState.teamARoundPoints else gameState.teamBRoundPoints
        val contractSucceeded = bidderPoints >= target

        // Multiplier for double / redouble
        val multiplier = when {
            highestBid.isRedouble -> 4
            highestBid.isDouble -> 2
            else -> 1
        }

        val basePoints = if (highestBid.amount >= HIGH_BID_THRESHOLD) 2 else 1
        val penaltyPoints = if (highestBid.amount >= HIGH_BID_THRESHOLD) 3 else 2

        var deltaTeamA = 0
        var deltaTeamB = 0

        if (contractSucceeded) {
            val awarded = basePoints * multiplier
            if (bidderTeam == Team.TEAM_A) {
                deltaTeamA = awarded
            } else {
                deltaTeamB = awarded
            }
        } else {
            val penalty = penaltyPoints * multiplier
            if (bidderTeam == Team.TEAM_A) {
                deltaTeamA = -penalty
            } else {
                deltaTeamB = -penalty
            }
        }

        val summary = if (contractSucceeded) {
            "${bidderTeam.shortName} made the contract! Won $bidderPoints / $target target points (+$basePoints game pts)."
        } else {
            "${bidderTeam.shortName} failed contract! Needed $target pts, scored $bidderPoints (-$penaltyPoints game pts)."
        }

        return RoundResult(
            bidderTeam = bidderTeam,
            contractBid = highestBid.amount,
            effectiveTarget = target,
            teamARoundPoints = gameState.teamARoundPoints,
            teamBRoundPoints = gameState.teamBRoundPoints,
            contractSucceeded = contractSucceeded,
            gamePointsDeltaTeamA = deltaTeamA,
            gamePointsDeltaTeamB = deltaTeamB,
            summary = summary
        )
    }

    fun checkMatchWinner(teamAGamePoints: Int, teamBGamePoints: Int, target: Int = 6): Team? {
        if (teamAGamePoints >= target || teamBGamePoints <= -target) return Team.TEAM_A
        if (teamBGamePoints >= target || teamAGamePoints <= -target) return Team.TEAM_B
        return null
    }
}
