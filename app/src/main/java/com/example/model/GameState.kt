package com.example.model

enum class GamePhase {
    ROUND_DEAL_1,
    BIDDING,
    TRUMP_SELECTION,
    ROUND_DEAL_2,
    TRICK_PLAY,
    ROUND_FINISHED,
    MATCH_FINISHED
}

data class GameStateSnapshot(
    val currentTurn: Seat,
    val hands: Map<Seat, List<Card>>,
    val currentTrick: Trick,
    val completedTricks: List<Trick>,
    val isTrumpRevealed: Boolean,
    val trumpRevealedBy: Seat?,
    val trumpRevealedAtTrick: Int?,
    val statusMessage: String
)

data class GameState(
    val phase: GamePhase = GamePhase.ROUND_DEAL_1,
    val dealer: Seat = Seat.WEST,
    val currentTurn: Seat = Seat.NORTH,
    val hands: Map<Seat, List<Card>> = Seat.entries.associateWith { emptyList() },
    val currentTrick: Trick = Trick(0, Seat.NORTH),
    val completedTricks: List<Trick> = emptyList(),
    val highestBid: Bid? = null,
    val bidder: Seat? = null,
    val trumpSuit: Suit? = null,
    val trumpCard: Card? = null,
    val isTrumpRevealed: Boolean = false,
    val trumpRevealedBy: Seat? = null,
    val trumpRevealedAtTrick: Int? = null,
    val bidsHistory: List<Bid> = emptyList(),
    val passedSeats: Set<Seat> = emptySet(),
    val teamAGamePoints: Int = 0, // Match score for Team South & North
    val teamBGamePoints: Int = 0, // Match score for Team East & West
    val teamARoundPoints: Int = 0, // Points collected in current round (0 to 29)
    val teamBRoundPoints: Int = 0, // Points collected in current round (0 to 29)
    val pairDeclaredBy: Seat? = null,
    val isPairDeclared: Boolean = false,
    val statusMessage: String = "Welcome to 29 Taash!",
    val isPracticeMode: Boolean = true,
    val undoStack: List<GameStateSnapshot> = emptyList(),
    val matchWinner: Team? = null,
    val roundNumber: Int = 1
) {
    val humanSeat: Seat get() = Seat.SOUTH
    val humanHand: List<Card> get() = hands[humanSeat].orEmpty()
    val isHumanTurn: Boolean get() = currentTurn == humanSeat && phase == GamePhase.TRICK_PLAY
    val canHumanUndo: Boolean get() = isPracticeMode && undoStack.isNotEmpty() && phase == GamePhase.TRICK_PLAY
    val totalRoundPointsInPlay: Int = 29
}
