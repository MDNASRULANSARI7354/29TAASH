package com.example.data.remote

import com.example.model.Seat

data class MultiplayerRoom(
    val roomCode: String,
    val hostId: String,
    val hostName: String,
    val players: Map<Seat, RemotePlayer> = emptyMap(),
    val isGameStarted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isFull: Boolean get() = players.size == 4
    val availableSeats: List<Seat> get() = Seat.entries.filter { !players.containsKey(it) }
}

data class RemotePlayer(
    val id: String,
    val name: String,
    val seat: Seat,
    val isReady: Boolean = false,
    val isHost: Boolean = false,
    val isConnected: Boolean = true
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderSeat: Seat,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
