package com.example.engine

import com.example.model.Card
import com.example.model.Rank
import com.example.model.Seat
import com.example.model.Suit
import java.util.Random

class DeckManager(private val random: Random = Random()) {

    /**
     * Standard 32-card deck for 29 Taash:
     * 8 cards per suit (7, 8, 9, 10, J, Q, K, A) * 4 suits = 32 cards.
     */
    fun createDeck(): List<Card> {
        val deck = ArrayList<Card>(32)
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                deck.add(Card(suit = suit, rank = rank))
            }
        }
        return deck
    }

    fun shuffleDeck(deck: List<Card> = createDeck()): List<Card> {
        val shuffled = deck.toMutableList()
        shuffled.shuffle(random)
        return shuffled
    }

    /**
     * Deal first 4 cards to each of the 4 players (total 16 cards).
     * Used for bidding and trump selection.
     */
    fun dealFirstPhase(shuffledDeck: List<Card>, dealer: Seat): Pair<Map<Seat, List<Card>>, List<Card>> {
        require(shuffledDeck.size == 32) { "Deck must have 32 cards, had ${shuffledDeck.size}" }
        val hands = mutableMapOf<Seat, MutableList<Card>>()
        for (seat in Seat.entries) {
            hands[seat] = mutableListOf()
        }

        var cardIndex = 0
        // Dealing starts from player to the left of dealer
        var currentSeat = dealer.next
        for (round in 0 until 4) {
            for (i in 0 until 4) {
                hands[currentSeat]?.add(shuffledDeck[cardIndex++])
                currentSeat = currentSeat.next
            }
        }

        val remainingDeck = shuffledDeck.subList(cardIndex, shuffledDeck.size)
        return Pair(hands.mapValues { it.value.toList() }, remainingDeck)
    }

    /**
     * Deal remaining 4 cards to each of the 4 players (total 16 cards).
     * After this, each player has exactly 8 cards.
     */
    fun dealSecondPhase(
        existingHands: Map<Seat, List<Card>>,
        remainingDeck: List<Card>,
        dealer: Seat
    ): Map<Seat, List<Card>> {
        require(remainingDeck.size == 16) { "Remaining deck must have 16 cards, had ${remainingDeck.size}" }
        val updatedHands = existingHands.mapValues { it.value.toMutableList() }.toMutableMap()

        var cardIndex = 0
        var currentSeat = dealer.next
        for (round in 0 until 4) {
            for (i in 0 until 4) {
                updatedHands[currentSeat]?.add(remainingDeck[cardIndex++])
                currentSeat = currentSeat.next
            }
        }

        // Sort each player's hand by suit and descending power for clean display
        return updatedHands.mapValues { (_, cards) ->
            cards.sortedWith(
                compareBy<Card> { it.suit.ordinal }
                    .thenByDescending { it.power }
            )
        }
    }
}
