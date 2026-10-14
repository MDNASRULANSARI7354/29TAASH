package com.example.data.remote

import com.example.model.Seat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MultiplayerManager {

    private val _currentRoom = MutableStateFlow<MultiplayerRoom?>(null)
    val currentRoom: StateFlow<MultiplayerRoom?> = _currentRoom.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Offline / Local Engine Ready")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    val myPlayerId: String = UUID.randomUUID().toString().take(8)
    var myPlayerName: String = "Player 1"
    var myAssignedSeat: Seat = Seat.SOUTH

    fun createRoom(hostName: String): MultiplayerRoom {
        myPlayerName = hostName.ifBlank { "Host" }
        myAssignedSeat = Seat.SOUTH

        val roomCode = generateRoomCode()
        val hostPlayer = RemotePlayer(
            id = myPlayerId,
            name = myPlayerName,
            seat = Seat.SOUTH,
            isReady = true,
            isHost = true
        )

        val room = MultiplayerRoom(
            roomCode = roomCode,
            hostId = myPlayerId,
            hostName = myPlayerName,
            players = mapOf(Seat.SOUTH to hostPlayer)
        )
        _currentRoom.value = room
        _connectionStatus.value = "Room #$roomCode created. Waiting for players to join seats..."

        addChatMessage("System", Seat.SOUTH, "Room $roomCode created! Share code with friends.")
        return room
    }

    fun joinRoom(code: String, playerName: String, preferredSeat: Seat? = null): Boolean {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.length != 4) return false

        myPlayerName = playerName.ifBlank { "Player" }
        val room = _currentRoom.value ?: MultiplayerRoom(
            roomCode = cleanCode,
            hostId = "host_1",
            hostName = "Room Host",
            players = mapOf(
                Seat.SOUTH to RemotePlayer("host_1", "Room Host", Seat.SOUTH, isReady = true, isHost = true)
            )
        )

        val targetSeat = preferredSeat ?: room.availableSeats.firstOrNull() ?: return false
        myAssignedSeat = targetSeat

        val joiningPlayer = RemotePlayer(
            id = myPlayerId,
            name = myPlayerName,
            seat = targetSeat,
            isReady = true,
            isHost = false
        )

        val updatedPlayers = room.players + (targetSeat to joiningPlayer)
        val updatedRoom = room.copy(players = updatedPlayers)
        _currentRoom.value = updatedRoom
        _connectionStatus.value = "Joined room #$cleanCode at seat ${targetSeat.displayName}"

        addChatMessage("System", targetSeat, "$myPlayerName joined as ${targetSeat.displayName}")
        return true
    }

    /**
     * Add bot players to remaining seats for quick multiplayer testing
     */
    fun fillEmptySeatsWithBots() {
        val room = _currentRoom.value ?: return
        val updatedPlayers = room.players.toMutableMap()

        val botNames = mapOf(
            Seat.EAST to "Vikram Bot",
            Seat.NORTH to "Aarav Bot (Partner)",
            Seat.WEST to "Rohan Bot"
        )

        for (seat in Seat.entries) {
            if (!updatedPlayers.containsKey(seat)) {
                val name = botNames[seat] ?: "AI Player"
                updatedPlayers[seat] = RemotePlayer(
                    id = "bot_${seat.name.lowercase()}",
                    name = name,
                    seat = seat,
                    isReady = true,
                    isHost = false
                )
            }
        }

        _currentRoom.value = room.copy(players = updatedPlayers)
        _connectionStatus.value = "All 4 seats filled! Host can start match."
        addChatMessage("System", Seat.SOUTH, "All seats ready for 29 Taash!")
    }

    fun startGame(): Boolean {
        val room = _currentRoom.value ?: return false
        if (room.players.size != 4) return false

        _currentRoom.value = room.copy(isGameStarted = true)
        _connectionStatus.value = "Match live!"
        addChatMessage("System", Seat.SOUTH, "Cards dealt! Good luck teams.")
        return true
    }

    fun leaveRoom() {
        _currentRoom.value = null
        _chatMessages.value = emptyList()
        _connectionStatus.value = "Left room."
    }

    fun sendChat(text: String) {
        if (text.isBlank()) return
        addChatMessage(myPlayerName, myAssignedSeat, text)
    }

    private fun addChatMessage(sender: String, seat: Seat, text: String) {
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = sender,
            senderSeat = seat,
            text = text
        )
        _chatMessages.value = _chatMessages.value + msg
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..4).map { chars.random() }.joinToString("")
    }
}
