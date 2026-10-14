package com.example

import com.example.engine.DeckManager
import com.example.engine.HintEngine
import com.example.engine.RulesEngine
import com.example.engine.ScoringEngine
import com.example.model.Bid
import com.example.model.Card
import com.example.model.GamePhase
import com.example.model.GameState
import com.example.model.PlayedCard
import com.example.model.Rank
import com.example.model.Seat
import com.example.model.Suit
import com.example.model.Team
import com.example.model.Trick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Taash29EngineTest {

    private val deckManager = DeckManager()

    @Test
    fun testDeckContainsExactly32UniqueCards() {
        val deck = deckManager.createDeck()
        assertEquals("Deck must contain exactly 32 cards in 29 Taash", 32, deck.size)
        val uniqueCards = deck.distinctBy { it.id }
        assertEquals("All 32 cards must be unique", 32, uniqueCards.size)

        // Verify suits count
        for (suit in Suit.entries) {
            val suitCards = deck.filter { it.suit == suit }
            assertEquals("Each suit must have 8 cards", 8, suitCards.size)
        }
    }

    @Test
    fun testCardPointValuesAndTotal29Points() {
        // Jack = 3, Nine = 2, Ace = 1, Ten = 1, K = 0, Q = 0, 8 = 0, 7 = 0
        assertEquals(3, Rank.JACK.points)
        assertEquals(2, Rank.NINE.points)
        assertEquals(1, Rank.ACE.points)
        assertEquals(1, Rank.TEN.points)
        assertEquals(0, Rank.KING.points)
        assertEquals(0, Rank.QUEEN.points)
        assertEquals(0, Rank.EIGHT.points)
        assertEquals(0, Rank.SEVEN.points)

        val deck = deckManager.createDeck()
        val totalCardPoints = deck.sumOf { it.points }
        assertEquals("Sum of card points in 32-card deck must be 28", 28, totalCardPoints)

        val totalPointsWithLastTrick = totalCardPoints + ScoringEngine.LAST_TRICK_BONUS
        assertEquals("Total points in a 29 Taash round must equal 29", 29, totalPointsWithLastTrick)
    }

    @Test
    fun testCardRankingOrder() {
        // J > 9 > A > 10 > K > Q > 8 > 7
        assertTrue(Rank.JACK.power > Rank.NINE.power)
        assertTrue(Rank.NINE.power > Rank.ACE.power)
        assertTrue(Rank.ACE.power > Rank.TEN.power)
        assertTrue(Rank.TEN.power > Rank.KING.power)
        assertTrue(Rank.KING.power > Rank.QUEEN.power)
        assertTrue(Rank.QUEEN.power > Rank.EIGHT.power)
        assertTrue(Rank.EIGHT.power > Rank.SEVEN.power)
    }

    @Test
    fun testTwoPhaseDealingGivesEachPlayer8Cards() {
        val fullDeck = deckManager.shuffleDeck()
        val (firstHands, remainingDeck) = deckManager.dealFirstPhase(fullDeck, Seat.WEST)

        for (seat in Seat.entries) {
            assertEquals("Phase 1: Each player gets 4 cards", 4, firstHands[seat]?.size)
        }
        assertEquals("Remaining deck after phase 1 has 16 cards", 16, remainingDeck.size)

        val fullHands = deckManager.dealSecondPhase(firstHands, remainingDeck, Seat.WEST)
        for (seat in Seat.entries) {
            assertEquals("Phase 2: Each player has 8 cards", 8, fullHands[seat]?.size)
        }

        // Verify no duplicate cards across all hands
        val allDealtCards = fullHands.values.flatten()
        assertEquals(32, allDealtCards.size)
        assertEquals(32, allDealtCards.distinctBy { it.id }.size)
    }

    @Test
    fun testLegalAndIllegalMovesFollowingSuit() {
        val leadCard = Card(Suit.SPADES, Rank.JACK)
        val trick = Trick(trickIndex = 0, leadSeat = Seat.NORTH, plays = listOf(PlayedCard(Seat.NORTH, leadCard)))

        val handWithSpades = listOf(
            Card(Suit.SPADES, Rank.SEVEN),
            Card(Suit.HEARTS, Rank.JACK),
            Card(Suit.DIAMONDS, Rank.ACE)
        )

        val state = GameState(
            phase = GamePhase.TRICK_PLAY,
            currentTurn = Seat.EAST,
            currentTrick = trick,
            hands = mapOf(Seat.EAST to handWithSpades),
            trumpSuit = Suit.HEARTS,
            isTrumpRevealed = false
        )

        // EAST has Spade 7, so EAST MUST play Spade 7
        val legalMove = RulesEngine.validateMove(state, Seat.EAST, Card(Suit.SPADES, Rank.SEVEN))
        assertTrue("Playing lead suit when holding it is valid", legalMove.isValid)

        val illegalMove = RulesEngine.validateMove(state, Seat.EAST, Card(Suit.HEARTS, Rank.JACK))
        assertFalse("Must follow lead suit: playing Heart when holding Spade is illegal", illegalMove.isValid)
        assertTrue(illegalMove.reason.contains("Must follow suit"))

        // Playing a card not in hand is illegal
        val notInHandMove = RulesEngine.validateMove(state, Seat.EAST, Card(Suit.CLUBS, Rank.NINE))
        assertFalse("Cannot play card not in hand", notInHandMove.isValid)
    }

    @Test
    fun testTrickWinnerSelectionWithLeadSuitAndTrumps() {
        // Case 1: Trump not revealed, highest lead suit wins
        val playsCase1 = listOf(
            PlayedCard(Seat.SOUTH, Card(Suit.CLUBS, Rank.TEN)),  // power 5
            PlayedCard(Seat.EAST, Card(Suit.CLUBS, Rank.ACE)),   // power 6
            PlayedCard(Seat.NORTH, Card(Suit.CLUBS, Rank.JACK)), // power 8 (Jack wins!)
            PlayedCard(Seat.WEST, Card(Suit.CLUBS, Rank.NINE))   // power 7
        )
        val trickCase1 = Trick(0, Seat.SOUTH, playsCase1)
        val winner1 = RulesEngine.determineTrickWinner(trickCase1, Suit.HEARTS, isTrumpRevealed = false, null)
        assertEquals("Jack of lead suit should win", Seat.NORTH, winner1)

        // Case 2: Trump IS revealed, player cuts with Trump
        val playsCase2 = listOf(
            PlayedCard(Seat.SOUTH, Card(Suit.CLUBS, Rank.JACK)), // Jack of Clubs (Lead suit, power 8)
            PlayedCard(Seat.EAST, Card(Suit.HEARTS, Rank.SEVEN)), // 7 of Hearts (Trump cut, power 1)
            PlayedCard(Seat.NORTH, Card(Suit.CLUBS, Rank.ACE)),
            PlayedCard(Seat.WEST, Card(Suit.CLUBS, Rank.NINE))
        )
        val trickCase2 = Trick(1, Seat.SOUTH, playsCase2)
        val winner2 = RulesEngine.determineTrickWinner(trickCase2, Suit.HEARTS, isTrumpRevealed = true, trumpRevealedAtTrick = 0)
        assertEquals("Trump card should win over high lead suit", Seat.EAST, winner2)

        // Case 3: Over-trumping: higher trump wins
        val playsCase3 = listOf(
            PlayedCard(Seat.SOUTH, Card(Suit.CLUBS, Rank.JACK)),
            PlayedCard(Seat.EAST, Card(Suit.HEARTS, Rank.SEVEN)), // 7 of Trump
            PlayedCard(Seat.NORTH, Card(Suit.HEARTS, Rank.JACK)),  // Jack of Trump!
            PlayedCard(Seat.WEST, Card(Suit.CLUBS, Rank.NINE))
        )
        val trickCase3 = Trick(2, Seat.SOUTH, playsCase3)
        val winner3 = RulesEngine.determineTrickWinner(trickCase3, Suit.HEARTS, isTrumpRevealed = true, trumpRevealedAtTrick = 0)
        assertEquals("Highest trump should win", Seat.NORTH, winner3)
    }

    @Test
    fun testScoringContractSuccessAndFailure() {
        // Test Bidder makes 17 points for bid of 16 -> SUCCESS (+1 game point)
        val stateSuccess = GameState(
            highestBid = Bid(Seat.SOUTH, 16),
            bidder = Seat.SOUTH,
            teamARoundPoints = 17,
            teamBRoundPoints = 12
        )
        val resultSuccess = ScoringEngine.evaluateRound(stateSuccess)
        assertTrue(resultSuccess.contractSucceeded)
        assertEquals(1, resultSuccess.gamePointsDeltaTeamA)
        assertEquals(0, resultSuccess.gamePointsDeltaTeamB)

        // Test Bidder makes 15 points for bid of 17 -> FAILURE (-2 game points)
        val stateFail = GameState(
            highestBid = Bid(Seat.SOUTH, 17),
            bidder = Seat.SOUTH,
            teamARoundPoints = 15,
            teamBRoundPoints = 14
        )
        val resultFail = ScoringEngine.evaluateRound(stateFail)
        assertFalse(resultFail.contractSucceeded)
        assertEquals(-2, resultFail.gamePointsDeltaTeamA)
        assertEquals(0, resultFail.gamePointsDeltaTeamB)
    }

    @Test
    fun testHintEngineRecommendsValidCard() {
        val hand = listOf(
            Card(Suit.SPADES, Rank.JACK),
            Card(Suit.SPADES, Rank.SEVEN),
            Card(Suit.HEARTS, Rank.ACE)
        )
        val trick = Trick(
            trickIndex = 0,
            leadSeat = Seat.WEST,
            plays = listOf(PlayedCard(Seat.WEST, Card(Suit.SPADES, Rank.TEN)))
        )
        val state = GameState(
            phase = GamePhase.TRICK_PLAY,
            currentTurn = Seat.SOUTH,
            currentTrick = trick,
            hands = mapOf(Seat.SOUTH to hand),
            trumpSuit = Suit.DIAMONDS,
            isTrumpRevealed = false
        )

        val hint = HintEngine.computeHint(state, Seat.SOUTH)
        assertNotNull(hint.recommendedCard)
        assertTrue("Hint card must be legal", RulesEngine.validateMove(state, Seat.SOUTH, hint.recommendedCard!!).isValid)
        assertTrue("Hint must explain rationale", hint.rationale.isNotEmpty())
    }

    @Test
    fun testPracticeModeUndoRevertsPlayedCard() {
        val gameEngine = com.example.engine.GameEngine()
        val cardToPlay = Card(Suit.SPADES, Rank.SEVEN)
        val hand = listOf(cardToPlay, Card(Suit.HEARTS, Rank.ACE))
        val trick = Trick(0, Seat.SOUTH)

        val stateBeforePlay = GameState(
            phase = GamePhase.TRICK_PLAY,
            currentTurn = Seat.SOUTH,
            hands = mapOf(Seat.SOUTH to hand),
            currentTrick = trick,
            isPracticeMode = true
        )

        // Human plays card
        val stateAfterPlay = gameEngine.playCard(stateBeforePlay, Seat.SOUTH, cardToPlay)
        assertEquals("Hand should decrease by 1", 1, stateAfterPlay.hands[Seat.SOUTH]?.size)
        assertEquals("Trick should have 1 played card", 1, stateAfterPlay.currentTrick.plays.size)
        assertTrue("Undo stack should have 1 snapshot", stateAfterPlay.undoStack.isNotEmpty())

        // Human presses Undo
        val stateAfterUndo = gameEngine.undoLastHumanMove(stateAfterPlay)
        assertEquals("Hand should be restored to 2 cards", 2, stateAfterUndo.hands[Seat.SOUTH]?.size)
        assertTrue("Played card should be returned to hand", stateAfterUndo.hands[Seat.SOUTH]?.contains(cardToPlay) == true)
        assertEquals("Trick plays should be reverted to 0", 0, stateAfterUndo.currentTrick.plays.size)
        assertEquals("Turn should return to South", Seat.SOUTH, stateAfterUndo.currentTurn)
    }

    @Test
    fun testMultiplayerRoomCreationAndSeatAssignment() {
        val manager = com.example.data.remote.MultiplayerManager()
        val room = manager.createRoom("Vikram")
        assertEquals(4, room.roomCode.length)
        assertEquals(Seat.SOUTH, manager.myAssignedSeat)
        assertEquals("Vikram", room.hostName)

        // Fill seats
        manager.fillEmptySeatsWithBots()
        val filledRoom = manager.currentRoom.value
        assertNotNull(filledRoom)
        assertTrue("Room should now be full with 4 players", filledRoom!!.isFull)
        assertEquals(4, filledRoom.players.size)

        // Start game
        val started = manager.startGame()
        assertTrue("Game should start when 4 seats are ready", started)
        assertTrue(manager.currentRoom.value?.isGameStarted == true)
    }
}
