package com.example.model

enum class Seat(val index: Int, val displayName: String) {
    SOUTH(0, "You (South)"),
    EAST(1, "East"),
    NORTH(2, "Partner (North)"),
    WEST(3, "West");

    val next: Seat
        get() = entries[(index + 1) % 4]

    val partner: Seat
        get() = entries[(index + 2) % 4]

    val team: Team
        get() = if (this == SOUTH || this == NORTH) Team.TEAM_A else Team.TEAM_B

    companion object {
        fun fromIndex(index: Int): Seat = entries[index % 4]
    }
}

enum class Team(val displayName: String, val shortName: String) {
    TEAM_A("Team South & North", "Team A"),
    TEAM_B("Team East & West", "Team B");

    val opponent: Team
        get() = if (this == TEAM_A) TEAM_B else TEAM_A
}

data class Player(
    val seat: Seat,
    val name: String,
    val isHuman: Boolean = false,
    val isOnline: Boolean = true
) {
    val team: Team get() = seat.team
}
