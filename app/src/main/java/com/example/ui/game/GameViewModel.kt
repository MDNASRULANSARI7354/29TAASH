package com.example.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesRepository
import com.example.engine.AiPlayer
import com.example.engine.GameEngine
import com.example.engine.HintEngine
import com.example.engine.HintRecommendation
import com.example.engine.RulesEngine
import com.example.model.Card
import com.example.model.GamePhase
import com.example.model.GameSettings
import com.example.model.GameState
import com.example.model.Seat
import com.example.model.Suit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = PreferencesRepository(application)
    private val gameEngine = GameEngine(settings = prefsRepo.loadSettings())

    private val _gameState = MutableStateFlow(gameEngine.startNewMatch())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _currentHint = MutableStateFlow<HintRecommendation?>(null)
    val currentHint: StateFlow<HintRecommendation?> = _currentHint.asStateFlow()

    private val _showHintDialog = MutableStateFlow(false)
    val showHintDialog: StateFlow<Boolean> = _showHintDialog.asStateFlow()

    private val _illegalMoveToast = MutableStateFlow<String?>(null)
    val illegalMoveToast: StateFlow<String?> = _illegalMoveToast.asStateFlow()

    private var aiLoopJob: Job? = null

    init {
        triggerAiLoopIfNeeded()
    }

    fun startNewMatch(practiceMode: Boolean = _gameState.value.isPracticeMode) {
        aiLoopJob?.cancel()
        val newState = gameEngine.startNewMatch(isPracticeMode = practiceMode)
        _gameState.value = newState
        _currentHint.value = null
        _showHintDialog.value = false
        triggerAiLoopIfNeeded()
    }

    fun nextRound() {
        aiLoopJob?.cancel()
        val current = _gameState.value
        val newState = gameEngine.startNewRound(current)
        _gameState.value = newState
        _currentHint.value = null
        triggerAiLoopIfNeeded()
    }

    fun placeHumanBid(amount: Int) {
        val current = _gameState.value
        if (current.phase != GamePhase.BIDDING || current.currentTurn != Seat.SOUTH) return

        val newState = gameEngine.placeBid(current, Seat.SOUTH, amount)
        _gameState.value = newState
        triggerAiLoopIfNeeded()
    }

    fun passHumanBid() {
        val current = _gameState.value
        if (current.phase != GamePhase.BIDDING || current.currentTurn != Seat.SOUTH) return

        val newState = gameEngine.passBid(current, Seat.SOUTH)
        _gameState.value = newState
        triggerAiLoopIfNeeded()
    }

    fun selectHumanTrump(suit: Suit) {
        val current = _gameState.value
        if (current.phase != GamePhase.TRUMP_SELECTION || current.bidder != Seat.SOUTH) return

        val trumpCard = current.hands[Seat.SOUTH]?.filter { it.suit == suit }?.maxByOrNull { it.power }
        val newState = gameEngine.selectTrump(current, Seat.SOUTH, suit, trumpCard)
        _gameState.value = newState
        triggerAiLoopIfNeeded()
    }

    fun playHumanCard(card: Card) {
        val current = _gameState.value
        if (current.phase != GamePhase.TRICK_PLAY || current.currentTurn != Seat.SOUTH) return

        val validation = RulesEngine.validateMove(current, Seat.SOUTH, card)
        if (!validation.isValid) {
            _illegalMoveToast.value = validation.reason
            return
        }

        val newState = gameEngine.playCard(current, Seat.SOUTH, card)
        _gameState.value = newState
        checkRoundFinish(newState)
        triggerAiLoopIfNeeded()
    }

    fun revealTrump() {
        val current = _gameState.value
        if (current.phase != GamePhase.TRICK_PLAY || current.isTrumpRevealed) return

        val newState = gameEngine.revealTrump(current, Seat.SOUTH)
        _gameState.value = newState
    }

    fun undoLastHumanMove() {
        val current = _gameState.value
        if (!current.canHumanUndo) return

        val restoredState = gameEngine.undoLastHumanMove(current)
        _gameState.value = restoredState
        prefsRepo.recordUndoUsed()
    }

    fun declarePair() {
        val current = _gameState.value
        val newState = gameEngine.declarePair(current, Seat.SOUTH)
        _gameState.value = newState
    }

    fun requestHint() {
        val current = _gameState.value
        if (current.phase != GamePhase.TRICK_PLAY) return

        val hint = HintEngine.computeHint(current, Seat.SOUTH)
        _currentHint.value = hint
        _showHintDialog.value = true
        prefsRepo.recordHintUsed()
    }

    fun dismissHint() {
        _showHintDialog.value = false
    }

    fun clearIllegalMoveToast() {
        _illegalMoveToast.value = null
    }

    fun togglePracticeMode() {
        val current = _gameState.value
        val toggled = !current.isPracticeMode
        _gameState.value = current.copy(isPracticeMode = toggled)
    }

    private fun triggerAiLoopIfNeeded() {
        aiLoopJob?.cancel()
        aiLoopJob = viewModelScope.launch {
            delay(150)
            while (true) {
                val state = _gameState.value
                val turn = state.currentTurn

                if (turn == Seat.SOUTH ||
                    state.phase == GamePhase.ROUND_FINISHED ||
                    state.phase == GamePhase.MATCH_FINISHED
                ) {
                    break
                }

                delay(gameEngine.settings.aiSpeedMs)

                when (state.phase) {
                    GamePhase.BIDDING -> {
                        val aiBid = AiPlayer.chooseBid(state, turn)
                        val nextState = if (aiBid != null) {
                            gameEngine.placeBid(state, turn, aiBid.amount)
                        } else {
                            gameEngine.passBid(state, turn)
                        }
                        _gameState.value = nextState
                    }

                    GamePhase.TRUMP_SELECTION -> {
                        val hand = state.hands[turn].orEmpty()
                        val (suit, card) = AiPlayer.chooseTrump(hand)
                        val nextState = gameEngine.selectTrump(state, turn, suit, card)
                        _gameState.value = nextState
                    }

                    GamePhase.TRICK_PLAY -> {
                        var intermediateState = state
                        // Check if AI should reveal trump before playing
                        if (!state.isTrumpRevealed && AiPlayer.shouldRevealTrump(state, turn)) {
                            intermediateState = gameEngine.revealTrump(state, turn)
                            _gameState.value = intermediateState
                            delay(400)
                        }

                        val cardToPlay = AiPlayer.chooseCard(intermediateState, turn)
                        val nextState = gameEngine.playCard(intermediateState, turn, cardToPlay)
                        _gameState.value = nextState
                        checkRoundFinish(nextState)
                    }

                    else -> break
                }
            }
        }
    }

    private fun checkRoundFinish(state: GameState) {
        if (state.phase == GamePhase.ROUND_FINISHED || state.phase == GamePhase.MATCH_FINISHED) {
            val made = state.highestBid?.let {
                val target = it.amount
                val scored = if (it.bidderSeat.team == com.example.model.Team.TEAM_A) state.teamARoundPoints else state.teamBRoundPoints
                scored >= target
            } ?: false

            prefsRepo.recordContract(made, state.highestBid?.amount ?: 16)
            if (state.phase == GamePhase.MATCH_FINISHED) {
                val won = state.matchWinner == com.example.model.Team.TEAM_A
                prefsRepo.recordMatchEnd(won)
            }
        }
    }
}
