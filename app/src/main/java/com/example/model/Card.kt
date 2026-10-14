package com.example.model

enum class Suit(val symbol: String, val displayName: String, val isRed: Boolean) {
    SPADES("♠", "Spades", false),
    HEARTS("♥", "Hearts", true),
    DIAMONDS("♦", "Diamonds", true),
    CLUBS("♣", "Clubs", false);

    companion object {
        fun fromSymbol(symbol: String): Suit? = entries.find { it.symbol == symbol }
    }
}

enum class Rank(
    val symbol: String,
    val displayName: String,
    val points: Int,
    val power: Int // Higher power wins trick: J(8) > 9(7) > A(6) > 10(5) > K(4) > Q(3) > 8(2) > 7(1)
) {
    SEVEN("7", "Seven", points = 0, power = 1),
    EIGHT("8", "Eight", points = 0, power = 2),
    QUEEN("Q", "Queen", points = 0, power = 3),
    KING("K", "King", points = 0, power = 4),
    TEN("10", "Ten", points = 1, power = 5),
    ACE("A", "Ace", points = 1, power = 6),
    NINE("9", "Nine", points = 2, power = 7),
    JACK("J", "Jack", points = 3, power = 8);

    companion object {
        fun fromSymbol(symbol: String): Rank? = entries.find { it.symbol == symbol }
    }
}

data class Card(
    val suit: Suit,
    val rank: Rank
) {
    val id: String get() = "${rank.symbol}_${suit.name}"
    val points: Int get() = rank.points
    val power: Int get() = rank.power
    val isRed: Boolean get() = suit.isRed

    override fun toString(): String = "${rank.symbol}${suit.symbol}"
}
