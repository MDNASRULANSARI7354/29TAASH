package com.example.engine

import com.example.model.Bid
import com.example.model.Card
import com.example.model.GameState
import com.example.model.Rank
import com.example.model.Seat
import com.example.model.Suit

object AiPlayer {

    fun chooseBid(gameState: GameState, seat: Seat): Bid? {
        val hand = gameState.hands[seat].orEmpty()
        return BiddingEngine.evaluateAiBid(hand, gameState.highestBid, seat)
    }

    /**
     * Choose trump suit from bidder's initial 4 cards.
     * Prefers suit with Jack (highest trump) or 9, or suit with highest card count.
     */
    fun chooseTrump(hand: List<Card>): Pair<Suit, Card?> {
        if (hand.isEmpty()) return Pair(Suit.HEARTS, null)

        val suitScores = mutableMapOf<Suit, Int>()
        for (suit in Suit.entries) {
            suitScores[suit] = 0
        }

        for (card in hand) {
            val score = when (card.rank) {
                Rank.JACK -> 10
                Rank.NINE -> 6
                Rank.ACE -> 4
                Rank.TEN -> 3
                Rank.KING -> 2
                Rank.QUEEN -> 1
                else -> 0
            } + 3 // bonus for each card in suit (suit length)
            suitScores[card.suit] = (suitScores[card.suit] ?: 0) + score
        }

        val bestSuit = suitScores.maxByOrNull { it.value }?.key ?: hand.first().suit
        val chosenTrumpCard = hand.filter { it.suit == bestSuit }.maxByOrNull { it.power } ?: hand.first()

        return Pair(bestSuit, chosenTrumpCard)
    }

    /**
     * Decides whether AI bot should request/reveal trump when void in lead suit.
     */
    fun shouldRevealTrump(gameState: GameState, seat: Seat): Boolean {
        if (gameState.isTrumpRevealed || gameState.trumpSuit == null) return false
        val hand = gameState.hands[seat].orEmpty()
        val currentTrick = gameState.currentTrick
        val leadPlay = currentTrick.plays.firstOrNull() ?: return false

        // Only consider if void in lead suit
        val hasLeadSuit = hand.any { it.suit == leadPlay.card.suit }
        if (hasLeadSuit) return false

        val currentWinner = RulesEngine.getCurrentWinningPlay(
            currentTrick,
            gameState.trumpSuit,
            gameState.isTrumpRevealed,
            gameState.trumpRevealedAtTrick
        )
        // If partner is already winning, no need to reveal trump now
        if (currentWinner?.playerSeat == seat.partner) {
            return false
        }

        // Check if bot holds any trump cards (even if hidden, bot knows its own hand)
        val trumpsInHand = hand.filter { it.suit == gameState.trumpSuit }
        if (trumpsInHand.isEmpty()) {
            return false // No point in revealing if bot has no trump to cut
        }

        // Reveal if trick has good points (Jack or 9 or total points >= 2)
        val trickPoints = currentTrick.cardPoints
        val isOpponentHigh = currentWinner?.card?.rank in listOf(Rank.JACK, Rank.NINE, Rank.ACE)

        return trickPoints >= 2 || isOpponentHigh
    }

    /**
     * Choose card to play for AI bot turn.
     */
    fun chooseCard(gameState: GameState, seat: Seat): Card {
        val hint = HintEngine.computeHint(gameState, seat)
        if (hint.recommendedCard != null && RulesEngine.validateMove(gameState, seat, hint.recommendedCard).isValid) {
            return hint.recommendedCard
        }
        val legalCards = RulesEngine.getLegalCards(gameState, seat)
        return legalCards.firstOrNull() ?: gameState.hands[seat]?.firstOrNull() ?: error("No card available")
    }
}
