package com.example.engine

import com.example.model.Bid
import com.example.model.Card
import com.example.model.GamePhase
import com.example.model.GameSettings
import com.example.model.GameState
import com.example.model.GameStateSnapshot
import com.example.model.PlayedCard
import com.example.model.Seat
import com.example.model.Suit
import com.example.model.Team
import com.example.model.Trick

class GameEngine(
    private val deckManager: DeckManager = DeckManager(),
    var settings: GameSettings = GameSettings()
) {
    private var remainingDeckAfterPhase1: List<Card> = emptyList()

    /**
     * Start a brand new match from score 0-0.
     */
    fun startNewMatch(isPracticeMode: Boolean = settings.practiceMode): GameState {
        val initial = GameState(
            phase = GamePhase.ROUND_DEAL_1,
            dealer = Seat.WEST,
            currentTurn = Seat.NORTH,
            teamAGamePoints = 0,
            teamBGamePoints = 0,
            roundNumber = 1,
            isPracticeMode = isPracticeMode,
            statusMessage = "Starting New Match! Dealing cards..."
        )
        return startNewRound(initial)
    }

    /**
     * Start next round within current match.
     */
    fun startNewRound(currentState: GameState): GameState {
        val nextDealer = currentState.dealer.next
        val firstBidder = nextDealer.next
        val fullDeck = deckManager.shuffleDeck()

        val (firstHands, remainingDeck) = deckManager.dealFirstPhase(fullDeck, nextDealer)
        remainingDeckAfterPhase1 = remainingDeck

        return currentState.copy(
            phase = GamePhase.BIDDING,
            dealer = nextDealer,
            currentTurn = firstBidder,
            hands = firstHands,
            currentTrick = Trick(0, firstBidder),
            completedTricks = emptyList(),
            highestBid = null,
            bidder = null,
            trumpSuit = null,
            trumpCard = null,
            isTrumpRevealed = false,
            trumpRevealedBy = null,
            trumpRevealedAtTrick = null,
            bidsHistory = emptyList(),
            passedSeats = emptySet(),
            teamARoundPoints = 0,
            teamBRoundPoints = 0,
            pairDeclaredBy = null,
            isPairDeclared = false,
            undoStack = emptyList(),
            statusMessage = "Bidding started. Minimum bid is ${settings.minBid}."
        )
    }

    /**
     * Process a bid from a player.
     */
    fun placeBid(currentState: GameState, seat: Seat, amount: Int): GameState {
        if (currentState.phase != GamePhase.BIDDING || currentState.currentTurn != seat) {
            return currentState
        }

        if (!BiddingEngine.isValidBid(currentState.highestBid, seat, amount)) {
            return currentState.copy(statusMessage = "Invalid bid of $amount. Must be higher than current bid.")
        }

        val newBid = Bid(bidderSeat = seat, amount = amount)
        val updatedHistory = currentState.bidsHistory + newBid
        val updatedPassed = currentState.passedSeats // seat bid, so not passed

        // Determine next bidder
        val nextTurn = BiddingEngine.getNextBiddingSeat(seat, updatedPassed)

        // Check if bidding is done (e.g. 3 players passed already and we have a bid)
        val activeBidders = Seat.entries.filter { !updatedPassed.contains(it) }
        val isFinished = activeBidders.size == 1 && activeBidders.contains(seat)

        return if (isFinished) {
            finishBidding(
                currentState.copy(
                    highestBid = newBid,
                    bidder = seat,
                    bidsHistory = updatedHistory
                )
            )
        } else {
            currentState.copy(
                highestBid = newBid,
                bidder = seat,
                bidsHistory = updatedHistory,
                currentTurn = nextTurn ?: seat,
                statusMessage = "${seat.displayName} bid $amount."
            )
        }
    }

    /**
     * Process a player passing on their bid turn.
     */
    fun passBid(currentState: GameState, seat: Seat): GameState {
        if (currentState.phase != GamePhase.BIDDING || currentState.currentTurn != seat) {
            return currentState
        }

        val passBid = Bid(bidderSeat = seat, amount = 0, isPass = true)
        val updatedHistory = currentState.bidsHistory + passBid
        val updatedPassed = currentState.passedSeats + seat

        val remainingActive = Seat.entries.filter { !updatedPassed.contains(it) }

        // All 4 passed without any bid
        if (remainingActive.isEmpty() && currentState.highestBid == null) {
            // Standard rule: dealer is forced to bid minBid
            val forcedBid = Bid(bidderSeat = currentState.dealer, amount = settings.minBid)
            val stateWithForcedBid = currentState.copy(
                highestBid = forcedBid,
                bidder = currentState.dealer,
                bidsHistory = updatedHistory + forcedBid,
                statusMessage = "All passed. Dealer ${currentState.dealer.displayName} forced to bid ${settings.minBid}."
            )
            return finishBidding(stateWithForcedBid)
        }

        // 3 passed and we have a winning bidder
        if (remainingActive.size == 1 && currentState.highestBid != null) {
            val winner = currentState.highestBid.bidderSeat
            return finishBidding(
                currentState.copy(
                    passedSeats = updatedPassed,
                    bidsHistory = updatedHistory
                )
            )
        }

        val nextTurn = BiddingEngine.getNextBiddingSeat(seat, updatedPassed) ?: seat

        return currentState.copy(
            passedSeats = updatedPassed,
            bidsHistory = updatedHistory,
            currentTurn = nextTurn,
            statusMessage = "${seat.displayName} passed."
        )
    }

    private fun finishBidding(state: GameState): GameState {
        val winningBidder = state.bidder ?: state.dealer
        val winningBid = state.highestBid ?: Bid(winningBidder, settings.minBid)

        return state.copy(
            phase = GamePhase.TRUMP_SELECTION,
            currentTurn = winningBidder,
            bidder = winningBidder,
            highestBid = winningBid,
            statusMessage = "${winningBidder.displayName} won bid at ${winningBid.amount}! Selecting secret trump..."
        )
    }

    /**
     * Bidder chooses trump suit and optionally sets secret trump card.
     */
    fun selectTrump(currentState: GameState, seat: Seat, suit: Suit, card: Card? = null): GameState {
        if (currentState.phase != GamePhase.TRUMP_SELECTION || currentState.bidder != seat) {
            return currentState
        }

        // Deal phase 2 (remaining 16 cards, 4 to each player)
        val fullHands = deckManager.dealSecondPhase(
            currentState.hands,
            remainingDeckAfterPhase1,
            currentState.dealer
        )

        val firstLeader = currentState.dealer.next

        return currentState.copy(
            phase = GamePhase.TRICK_PLAY,
            trumpSuit = suit,
            trumpCard = card,
            isTrumpRevealed = false,
            trumpRevealedBy = null,
            trumpRevealedAtTrick = null,
            hands = fullHands,
            currentTurn = firstLeader,
            currentTrick = Trick(trickIndex = 0, leadSeat = firstLeader),
            completedTricks = emptyList(),
            statusMessage = "Trump selected secretly. Round begins! ${firstLeader.displayName} leads first trick."
        )
    }

    /**
     * Reveal hidden trump.
     */
    fun revealTrump(currentState: GameState, seat: Seat): GameState {
        if (currentState.phase != GamePhase.TRICK_PLAY || currentState.isTrumpRevealed || currentState.trumpSuit == null) {
            return currentState
        }

        return currentState.copy(
            isTrumpRevealed = true,
            trumpRevealedBy = seat,
            trumpRevealedAtTrick = currentState.currentTrick.trickIndex,
            statusMessage = "${seat.displayName} revealed Trump: ${currentState.trumpSuit.displayName} ${currentState.trumpSuit.symbol}!"
        )
    }

    /**
     * Play a card from hand.
     */
    fun playCard(currentState: GameState, seat: Seat, card: Card): GameState {
        if (currentState.phase != GamePhase.TRICK_PLAY || currentState.currentTurn != seat) {
            return currentState
        }

        val validation = RulesEngine.validateMove(currentState, seat, card)
        if (!validation.isValid) {
            return currentState.copy(statusMessage = validation.reason)
        }

        // Save undo snapshot if this is human player's move in practice mode
        val undoStack = if (currentState.isPracticeMode && seat == currentState.humanSeat) {
            currentState.undoStack + GameStateSnapshot(
                currentTurn = currentState.currentTurn,
                hands = currentState.hands,
                currentTrick = currentState.currentTrick,
                completedTricks = currentState.completedTricks,
                isTrumpRevealed = currentState.isTrumpRevealed,
                trumpRevealedBy = currentState.trumpRevealedBy,
                trumpRevealedAtTrick = currentState.trumpRevealedAtTrick,
                statusMessage = currentState.statusMessage
            )
        } else {
            currentState.undoStack
        }

        // Remove card from hand
        val updatedHand = (currentState.hands[seat].orEmpty() - card)
        val updatedHands = currentState.hands + (seat to updatedHand)

        // Add to current trick
        val play = PlayedCard(seat, card)
        val updatedPlays = currentState.currentTrick.plays + play
        val updatedTrick = currentState.currentTrick.copy(plays = updatedPlays)

        // If trick is NOT full yet: advance to next player
        if (updatedPlays.size < 4) {
            return currentState.copy(
                hands = updatedHands,
                currentTrick = updatedTrick,
                currentTurn = seat.next,
                undoStack = undoStack,
                statusMessage = "${seat.displayName} played ${card.rank.symbol}${card.suit.symbol}."
            )
        }

        // Trick is full (4 cards played):
        val winnerSeat = RulesEngine.determineTrickWinner(
            updatedTrick,
            currentState.trumpSuit,
            currentState.isTrumpRevealed,
            currentState.trumpRevealedAtTrick
        )
        val completedTrick = updatedTrick.copy(winnerSeat = winnerSeat, isClosed = true)
        val pointsAwarded = completedTrick.totalPointsAwarded

        val newTeamARound = currentState.teamARoundPoints + (if (winnerSeat.team == Team.TEAM_A) pointsAwarded else 0)
        val newTeamBRound = currentState.teamBRoundPoints + (if (winnerSeat.team == Team.TEAM_B) pointsAwarded else 0)
        val updatedCompletedTricks = currentState.completedTricks + completedTrick

        val bonusMsg = if (completedTrick.isLastTrick) " (+1 last trick bonus!)" else ""
        val trickWinMsg = "${winnerSeat.displayName} won Trick #${completedTrick.trickIndex + 1} ($pointsAwarded pts)$bonusMsg."

        // Check if round is over (8 tricks completed)
        if (updatedCompletedTricks.size >= 8) {
            val stateAfter8Tricks = currentState.copy(
                hands = updatedHands,
                currentTrick = completedTrick,
                completedTricks = updatedCompletedTricks,
                teamARoundPoints = newTeamARound,
                teamBRoundPoints = newTeamBRound,
                currentTurn = winnerSeat,
                undoStack = emptyList() // clear undo on round end
            )
            return finishRound(stateAfter8Tricks, trickWinMsg)
        }

        // Start next trick
        val nextTrickIndex = updatedCompletedTricks.size
        return currentState.copy(
            hands = updatedHands,
            currentTrick = Trick(trickIndex = nextTrickIndex, leadSeat = winnerSeat),
            completedTricks = updatedCompletedTricks,
            teamARoundPoints = newTeamARound,
            teamBRoundPoints = newTeamBRound,
            currentTurn = winnerSeat,
            undoStack = emptyList(), // clear undo once a full trick resolves
            statusMessage = trickWinMsg
        )
    }

    /**
     * Finish round, evaluate contract and scores.
     */
    private fun finishRound(state: GameState, lastTrickMsg: String): GameState {
        val roundResult = ScoringEngine.evaluateRound(state)
        val newTeamAGame = state.teamAGamePoints + roundResult.gamePointsDeltaTeamA
        val newTeamBGame = state.teamBGamePoints + roundResult.gamePointsDeltaTeamB

        val matchWinner = ScoringEngine.checkMatchWinner(newTeamAGame, newTeamBGame, settings.targetScore)

        val finalPhase = if (matchWinner != null) GamePhase.MATCH_FINISHED else GamePhase.ROUND_FINISHED
        val summaryMsg = "$lastTrickMsg ${roundResult.summary}"

        return state.copy(
            phase = finalPhase,
            teamAGamePoints = newTeamAGame,
            teamBGamePoints = newTeamBGame,
            matchWinner = matchWinner,
            statusMessage = summaryMsg
        )
    }

    /**
     * Revert human player's last played card in Practice Mode!
     */
    fun undoLastHumanMove(currentState: GameState): GameState {
        if (!currentState.isPracticeMode || currentState.undoStack.isEmpty() || currentState.phase != GamePhase.TRICK_PLAY) {
            return currentState
        }

        val lastSnapshot = currentState.undoStack.last()
        val remainingStack = currentState.undoStack.dropLast(1)

        return currentState.copy(
            currentTurn = lastSnapshot.currentTurn,
            hands = lastSnapshot.hands,
            currentTrick = lastSnapshot.currentTrick,
            completedTricks = lastSnapshot.completedTricks,
            isTrumpRevealed = lastSnapshot.isTrumpRevealed,
            trumpRevealedBy = lastSnapshot.trumpRevealedBy,
            trumpRevealedAtTrick = lastSnapshot.trumpRevealedAtTrick,
            undoStack = remainingStack,
            statusMessage = "Move undone. Choose another card to play."
        )
    }

    /**
     * Declare Jodi / Pair (King + Queen of trump).
     */
    fun declarePair(currentState: GameState, seat: Seat): GameState {
        if (!RulesEngine.canDeclarePair(currentState, seat)) {
            return currentState.copy(statusMessage = "Cannot declare Pair. Trump must be revealed and you must hold King & Queen of Trump.")
        }

        return currentState.copy(
            isPairDeclared = true,
            pairDeclaredBy = seat,
            statusMessage = "${seat.displayName} declared Royal Pair (King & Queen of ${currentState.trumpSuit?.displayName})! Contract target adjusted by 4 points."
        )
    }
}
