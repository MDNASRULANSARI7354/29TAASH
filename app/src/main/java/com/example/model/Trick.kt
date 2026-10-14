package com.example.model

data class PlayedCard(
    val playerSeat: Seat,
    val card: Card
)

data class Trick(
    val trickIndex: Int, // 0 to 7 (8 tricks total in 29)
    val leadSeat: Seat,
    val plays: List<PlayedCard> = emptyList(),
    val winnerSeat: Seat? = null,
    val isClosed: Boolean = false
) {
    val leadSuit: Suit? get() = plays.firstOrNull()?.card?.suit

    val isFull: Boolean get() = plays.size == 4

    // Jack=3, 9=2, A=1, 10=1, others=0
    val cardPoints: Int get() = plays.sumOf { it.card.points }

    // In 29 Taash, trick 7 (last trick of round, 8th trick) gives 1 bonus point
    val isLastTrick: Boolean get() = trickIndex == 7

    val totalPointsAwarded: Int get() = cardPoints + (if (isLastTrick) 1 else 0)

    fun hasPlayed(seat: Seat): Boolean = plays.any { it.playerSeat == seat }
}

data class Bid(
    val bidderSeat: Seat,
    val amount: Int, // 16 to 28, or 29
    val isPass: Boolean = false,
    val isDouble: Boolean = false,
    val isRedouble: Boolean = false
) {
    override fun toString(): String {
        return when {
            isPass -> "Pass"
            isRedouble -> "Redouble ($amount)"
            isDouble -> "Double ($amount)"
            else -> "$amount"
        }
    }
}
