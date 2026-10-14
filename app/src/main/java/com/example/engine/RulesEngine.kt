package com.example.engine

import com.example.model.Card
import com.example.model.GameState
import com.example.model.PlayedCard
import com.example.model.Rank
import com.example.model.Seat
import com.example.model.Suit
import com.example.model.Trick

data class MoveValidation(
    val isValid: Boolean,
    val reason: String = ""
)

object RulesEngine {

    /**
     * Determines if a card move is legal according to standard 29 Taash rules.
     */
    fun validateMove(
        gameState: GameState,
        playerSeat: Seat,
        card: Card
    ): MoveValidation {
        if (gameState.currentTurn != playerSeat) {
            return MoveValidation(false, "It is not ${playerSeat.displayName}'s turn.")
        }

        val hand = gameState.hands[playerSeat].orEmpty()
        if (!hand.contains(card)) {
            return MoveValidation(false, "You do not hold ${card.rank.symbol}${card.suit.symbol} in your hand.")
        }

        val currentTrick = gameState.currentTrick
        val leadPlay = currentTrick.plays.firstOrNull()

        // If leading trick: player can play any card from hand
        if (leadPlay == null) {
            // In 29 Taash, if trump is not revealed, bidder cannot lead trump suit unless they hold only trump cards
            if (!gameState.isTrumpRevealed &&
                gameState.bidder == playerSeat &&
                gameState.trumpSuit != null &&
                card.suit == gameState.trumpSuit
            ) {
                val nonTrumpCards = hand.filter { it.suit != gameState.trumpSuit }
                if (nonTrumpCards.isNotEmpty()) {
                    return MoveValidation(
                        false,
                        "As the bidder, you cannot lead with the secret trump suit before it has been revealed, unless you hold only trump cards."
                    )
                }
            }
            return MoveValidation(true)
        }

        val leadSuit = leadPlay.card.suit
        val hasLeadSuit = hand.any { it.suit == leadSuit }

        // Rule: Must follow suit if player holds any cards of the lead suit
        if (hasLeadSuit) {
            if (card.suit != leadSuit) {
                return MoveValidation(
                    false,
                    "Must follow suit: You hold ${leadSuit.displayName} cards and must play ${leadSuit.displayName}."
                )
            }
            return MoveValidation(true)
        }

        // Out of lead suit:
        // If trump is revealed:
        // Any card is valid (can trump or discard).
        // If trump was just revealed on this turn by this player:
        // Under 29 rules, if the player requested trump reveal, they should play a trump if they have one!
        if (gameState.isTrumpRevealed &&
            gameState.trumpRevealedBy == playerSeat &&
            gameState.currentTrick.trickIndex == gameState.trumpRevealedAtTrick &&
            gameState.trumpSuit != null
        ) {
            val trumpsInHand = hand.filter { it.suit == gameState.trumpSuit }
            if (trumpsInHand.isNotEmpty() && card.suit != gameState.trumpSuit) {
                return MoveValidation(
                    false,
                    "Since you revealed the trump suit (${gameState.trumpSuit.displayName}), you must play a trump card if you hold one."
                )
            }
        }

        return MoveValidation(true)
    }

    /**
     * Returns the list of all legal cards that the given player can play right now.
     */
    fun getLegalCards(gameState: GameState, playerSeat: Seat): List<Card> {
        val hand = gameState.hands[playerSeat].orEmpty()
        return hand.filter { validateMove(gameState, playerSeat, it).isValid }
    }

    /**
     * Determines the winner of a completed trick (4 plays).
     * In 29 Taash:
     * - Trumps only have power if trump has been revealed.
     * - If trump was revealed at or before this trick, and any trump cards were played,
     *   the highest trump card wins.
     * - Otherwise, the highest card of the lead suit wins.
     */
    fun determineTrickWinner(
        trick: Trick,
        trumpSuit: Suit?,
        isTrumpRevealed: Boolean,
        trumpRevealedAtTrick: Int?
    ): Seat {
        require(trick.plays.isNotEmpty()) { "Trick cannot be empty to determine winner" }
        val leadPlay = trick.plays.first()
        val leadSuit = leadPlay.card.suit

        val trumpCounted = isTrumpRevealed && trumpSuit != null &&
                (trumpRevealedAtTrick == null || trumpRevealedAtTrick <= trick.trickIndex)

        if (trumpCounted) {
            val trumpPlays = trick.plays.filter { it.card.suit == trumpSuit }
            if (trumpPlays.isNotEmpty()) {
                val winningPlay = trumpPlays.maxByOrNull { it.card.power }
                return winningPlay?.playerSeat ?: leadPlay.playerSeat
            }
        }

        // Highest card of lead suit
        val leadSuitPlays = trick.plays.filter { it.card.suit == leadSuit }
        val winningPlay = leadSuitPlays.maxByOrNull { it.card.power }
        return winningPlay?.playerSeat ?: leadPlay.playerSeat
    }

    /**
     * Current winning play so far in the trick.
     */
    fun getCurrentWinningPlay(
        trick: Trick,
        trumpSuit: Suit?,
        isTrumpRevealed: Boolean,
        trumpRevealedAtTrick: Int?
    ): PlayedCard? {
        if (trick.plays.isEmpty()) return null
        val leadPlay = trick.plays.first()
        val leadSuit = leadPlay.card.suit

        val trumpCounted = isTrumpRevealed && trumpSuit != null &&
                (trumpRevealedAtTrick == null || trumpRevealedAtTrick <= trick.trickIndex)

        if (trumpCounted) {
            val trumpPlays = trick.plays.filter { it.card.suit == trumpSuit }
            if (trumpPlays.isNotEmpty()) {
                return trumpPlays.maxByOrNull { it.card.power }
            }
        }

        val leadSuitPlays = trick.plays.filter { it.card.suit == leadSuit }
        return leadSuitPlays.maxByOrNull { it.card.power }
    }

    /**
     * Check if a player can claim Pair / Jodi (King + Queen of Trump).
     * In 29 Taash, Pair can only be claimed if:
     * 1. Trump has been revealed.
     * 2. Player has won at least one trick (or holds the cards in hand).
     * 3. Player holds both King and Queen of the trump suit.
     */
    fun canDeclarePair(gameState: GameState, playerSeat: Seat): Boolean {
        if (!gameState.isTrumpRevealed || gameState.isPairDeclared || gameState.trumpSuit == null) {
            return false
        }
        val hand = gameState.hands[playerSeat].orEmpty()
        val hasTrumpKing = hand.any { it.suit == gameState.trumpSuit && it.rank == Rank.KING }
        val hasTrumpQueen = hand.any { it.suit == gameState.trumpSuit && it.rank == Rank.QUEEN }
        return hasTrumpKing && hasTrumpQueen
    }
}
