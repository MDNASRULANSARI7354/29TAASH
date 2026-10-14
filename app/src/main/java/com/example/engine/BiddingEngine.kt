package com.example.engine

import com.example.model.Bid
import com.example.model.GameState
import com.example.model.Seat

object BiddingEngine {

    const val MIN_BID = 16
    const val MAX_BID = 28

    fun isValidBid(currentHighest: Bid?, seat: Seat, amount: Int): Boolean {
        if (amount < MIN_BID || amount > MAX_BID) return false
        if (currentHighest == null) return true
        return amount > currentHighest.amount
    }

    /**
     * Determines next active bidder who has not passed.
     */
    fun getNextBiddingSeat(currentTurn: Seat, passedSeats: Set<Seat>): Seat? {
        var candidate = currentTurn.next
        for (i in 0 until 4) {
            if (!passedSeats.contains(candidate)) {
                return candidate
            }
            candidate = candidate.next
        }
        return null
    }

    /**
     * AI bidding evaluation based on dealt 4 cards:
     * Counts honour points:
     * - Jack = 3 pts
     * - Nine = 2 pts
     * - Ace = 1 pt
     * - Ten = 1 pt
     * - Extra points for length (3 cards of same suit)
     */
    fun evaluateAiBid(hand: List<com.example.model.Card>, currentHighestBid: Bid?, seat: Seat): Bid? {
        val jackCount = hand.count { it.rank == com.example.model.Rank.JACK }
        val nineCount = hand.count { it.rank == com.example.model.Rank.NINE }
        val aceCount = hand.count { it.rank == com.example.model.Rank.ACE }
        val tenCount = hand.count { it.rank == com.example.model.Rank.TEN }

        val points = hand.sumOf { it.points }
        val suitsGrouped = hand.groupBy { it.suit }
        val maxSuitLength = suitsGrouped.values.maxOfOrNull { it.size } ?: 0

        // Estimated bid threshold based on 4-card strength
        val maxPotentialBid = when {
            jackCount >= 2 && nineCount >= 1 -> 19
            jackCount >= 2 || (jackCount >= 1 && nineCount >= 1 && aceCount >= 1) -> 18
            jackCount >= 1 && (nineCount >= 1 || aceCount >= 1) -> 17
            points >= 4 || (jackCount >= 1 && maxSuitLength >= 2) -> 16
            else -> 0
        }

        val currentAmount = currentHighestBid?.amount ?: (MIN_BID - 1)
        if (maxPotentialBid > currentAmount) {
            return Bid(
                bidderSeat = seat,
                amount = currentAmount + 1
            )
        }
        // Pass
        return null
    }
}
