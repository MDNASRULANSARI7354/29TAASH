package com.example.engine

import com.example.model.Card
import com.example.model.GameState
import com.example.model.Rank
import com.example.model.Seat
import com.example.model.Suit

data class HintRecommendation(
    val recommendedCard: Card?,
    val shouldRevealTrump: Boolean = false,
    val rationale: String
)

object HintEngine {

    fun computeHint(gameState: GameState, playerSeat: Seat = Seat.SOUTH): HintRecommendation {
        val hand = gameState.hands[playerSeat].orEmpty()
        if (hand.isEmpty()) {
            return HintRecommendation(null, false, "Hand is empty.")
        }

        val legalCards = RulesEngine.getLegalCards(gameState, playerSeat)
        if (legalCards.isEmpty()) {
            return HintRecommendation(null, false, "No legal card moves available.")
        }

        if (legalCards.size == 1) {
            val single = legalCards.first()
            return HintRecommendation(
                single,
                false,
                "Play ${single.rank.symbol}${single.suit.symbol}: This is your only legal card to follow suit."
            )
        }

        val currentTrick = gameState.currentTrick
        val leadPlay = currentTrick.plays.firstOrNull()

        // 1. LEADING A TRICK
        if (leadPlay == null) {
            // Find best lead
            // Safe Aces or Jacks
            val aces = legalCards.filter { it.rank == Rank.ACE }
            if (aces.isNotEmpty()) {
                val chosen = aces.first()
                return HintRecommendation(
                    chosen,
                    false,
                    "Lead ${chosen.rank.symbol}${chosen.suit.symbol}: Aces are strong trick winners (1 pt) and force out lower cards."
                )
            }

            // Lead high card of long suit
            val suitCounts = legalCards.groupBy { it.suit }
            val longestSuitEntry = suitCounts.maxByOrNull { it.value.size }
            val chosen = longestSuitEntry?.value?.maxByOrNull { it.power } ?: legalCards.first()

            return HintRecommendation(
                chosen,
                false,
                "Lead ${chosen.rank.symbol}${chosen.suit.symbol}: Establishes control in your ${chosen.suit.displayName} suit."
            )
        }

        // 2. FOLLOWING IN TRICK
        val leadSuit = leadPlay.card.suit
        val hasLeadSuit = hand.any { it.suit == leadSuit }
        val partnerSeat = playerSeat.partner
        val currentWinner = RulesEngine.getCurrentWinningPlay(
            currentTrick,
            gameState.trumpSuit,
            gameState.isTrumpRevealed,
            gameState.trumpRevealedAtTrick
        )
        val isPartnerCurrentlyWinning = currentWinner != null && currentWinner.playerSeat == partnerSeat
        val trickPoints = currentTrick.cardPoints

        // Case A: You have the lead suit (must follow suit)
        if (hasLeadSuit) {
            val suitCards = legalCards.filter { it.suit == leadSuit }

            if (isPartnerCurrentlyWinning) {
                // Partner is winning:
                // If partner played Jack or Ace and no trump cut, partner will likely win.
                // Feed points if trick has 3+ players played or partner played Jack
                val isHighPartnerCard = currentWinner?.card?.rank in listOf(Rank.JACK, Rank.NINE)
                if (isHighPartnerCard) {
                    val pointCards = suitCards.filter { it.points > 0 }
                    if (pointCards.isNotEmpty()) {
                        val smearingCard = pointCards.minByOrNull { it.power } ?: pointCards.first()
                        return HintRecommendation(
                            smearingCard,
                            false,
                            "Play ${smearingCard.rank.symbol}${smearingCard.suit.symbol} (+${smearingCard.points} pt): Partner is currently winning with ${currentWinner?.card}, so smear points to your team!"
                        )
                    }
                }
                // Otherwise play lowest card to save high cards
                val lowest = suitCards.minByOrNull { it.power } ?: suitCards.first()
                return HintRecommendation(
                    lowest,
                    false,
                    "Play ${lowest.rank.symbol}${lowest.suit.symbol}: Partner is currently winning this trick; conserve your high cards."
                )
            } else {
                // Opponent is winning or you need to win
                val winningCards = suitCards.filter { candidate ->
                    val winningPower = currentWinner?.card?.power ?: 0
                    // If current winner is lead suit:
                    if (currentWinner?.card?.suit == leadSuit) {
                        candidate.power > winningPower
                    } else false // If opponent already trumped, lead suit card cannot win
                }

                if (winningCards.isNotEmpty()) {
                    // Pick the lowest winning card to win cheaply!
                    val lowestWinner = winningCards.minByOrNull { it.power } ?: winningCards.first()
                    return HintRecommendation(
                        lowestWinner,
                        false,
                        "Play ${lowestWinner.rank.symbol}${lowestWinner.suit.symbol}: Lowest card that secures this trick worth $trickPoints points!"
                    )
                } else {
                    // Cannot win this trick, play lowest non-point card
                    val lowest = suitCards.minByOrNull { it.power } ?: suitCards.first()
                    return HintRecommendation(
                        lowest,
                        false,
                        "Play ${lowest.rank.symbol}${lowest.suit.symbol}: Cannot beat opponent's ${currentWinner?.card}; dump your lowest card."
                    )
                }
            }
        }

        // Case B: Void in lead suit (cannot follow suit)
        if (!gameState.isTrumpRevealed) {
            // Trump is hidden
            if (isPartnerCurrentlyWinning) {
                // Partner winning: safely discard a low card from a non-trump suit
                val nonPointCards = legalCards.filter { it.points == 0 }.minByOrNull { it.power }
                val chosen = nonPointCards ?: legalCards.minByOrNull { it.power } ?: legalCards.first()
                return HintRecommendation(
                    chosen,
                    false,
                    "Discard ${chosen.rank.symbol}${chosen.suit.symbol}: Partner is winning; discard a low card safely without revealing trump."
                )
            } else {
                // Opponent is winning
                if (trickPoints >= 2 || currentWinner?.card?.rank == Rank.JACK) {
                    return HintRecommendation(
                        null,
                        true,
                        "Reveal Trump: High-value trick with ${trickPoints} points! Reveal Trump now to cut and take this trick."
                    )
                } else {
                    // Low value trick, safe to discard
                    val safeDiscard = legalCards.filter { it.points == 0 }.minByOrNull { it.power } ?: legalCards.first()
                    return HintRecommendation(
                        safeDiscard,
                        false,
                        "Discard ${safeDiscard.rank.symbol}${safeDiscard.suit.symbol}: Low-value trick; hold back trump reveal for bigger tricks."
                    )
                }
            }
        } else {
            // Trump is already revealed
            val trumpSuit = gameState.trumpSuit
            val trumpsInHand = legalCards.filter { it.suit == trumpSuit }

            if (isPartnerCurrentlyWinning) {
                // Partner is winning: smear points or dump trash
                val trashCard = legalCards.filter { it.suit != trumpSuit && it.points == 0 }.minByOrNull { it.power }
                    ?: legalCards.minByOrNull { it.power } ?: legalCards.first()
                return HintRecommendation(
                    trashCard,
                    false,
                    "Discard ${trashCard.rank.symbol}${trashCard.suit.symbol}: Partner is winning; discard a harmless card."
                )
            } else {
                // Opponent is winning
                if (trumpsInHand.isNotEmpty()) {
                    // Can we trump?
                    val currentWinnerCard = currentWinner?.card
                    val minWinningTrump = if (currentWinnerCard != null && currentWinnerCard.suit == trumpSuit) {
                        trumpsInHand.filter { it.power > currentWinnerCard.power }.minByOrNull { it.power }
                    } else {
                        trumpsInHand.minByOrNull { it.power }
                    }

                    if (minWinningTrump != null) {
                        return HintRecommendation(
                            minWinningTrump,
                            false,
                            "Play Trump ${minWinningTrump.rank.symbol}${minWinningTrump.suit.symbol}: Cut opponent's trick and win $trickPoints points!"
                        )
                    }
                }
                // Cannot win or out-trump
                val lowest = legalCards.filter { it.suit != trumpSuit }.minByOrNull { it.power }
                    ?: legalCards.minByOrNull { it.power } ?: legalCards.first()
                return HintRecommendation(
                    lowest,
                    false,
                    "Discard ${lowest.rank.symbol}${lowest.suit.symbol}: Cannot out-trump; save your trumps for later."
                )
            }
        }
    }
}
