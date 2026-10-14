package com.example.ui.lobby

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.MultiplayerManager
import com.example.model.Seat
import com.example.model.Team
import com.example.ui.theme.FeltDark
import com.example.ui.theme.FeltLight
import com.example.ui.theme.FeltMedium
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TeamABlue
import com.example.ui.theme.TeamBRed

@Composable
fun LobbyScreen(
    multiplayerManager: MultiplayerManager,
    onStartGame: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val room by multiplayerManager.currentRoom.collectAsState()
    val connectionStatus by multiplayerManager.connectionStatus.collectAsState()
    val chatMessages by multiplayerManager.chatMessages.collectAsState()

    var inputPlayerName by remember { mutableStateOf("Player 1") }
    var inputRoomCode by remember { mutableStateOf("") }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(FeltLight, FeltMedium, FeltDark)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // HEADER
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Multiplayer Lobby (मल्टीप्लेयर)",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Connection status chip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x33000000), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0x33FFD54F), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "● $connectionStatus",
                        color = Color(0xFFA0B3A5),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (room == null) {
                    // NO ROOM JOINED: CREATE OR JOIN
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xDD0E1A11)),
                        border = BorderStroke(1.dp, GoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Your Display Name:",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = inputPlayerName,
                                onValueChange = { inputPlayerName = it },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0x22FFFFFF),
                                    unfocusedContainerColor = Color(0x22FFFFFF)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("lobby_name_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { multiplayerManager.createRoom(inputPlayerName) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("lobby_create_room_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create New Room", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Or Join Existing Room:",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = inputRoomCode,
                                    onValueChange = { inputRoomCode = it.take(4).uppercase() },
                                    placeholder = { Text("Code (e.g. ABCD)", color = Color.Gray) },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0x22FFFFFF),
                                        unfocusedContainerColor = Color(0x22FFFFFF)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("lobby_code_input")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        multiplayerManager.joinRoom(inputRoomCode, inputPlayerName)
                                    },
                                    enabled = inputRoomCode.length == 4,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1976D2),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("lobby_join_room_btn")
                                ) {
                                    Text("Join")
                                }
                            }
                        }
                    }
                } else {
                    // ROOM CREATED / JOINED
                    val currentRoom = room!!

                    // ROOM CODE BANNER
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xEE0A1E11)),
                        border = BorderStroke(2.dp, GoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "ROOM CODE",
                                color = Color(0xFFA0B3A5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentRoom.roomCode,
                                color = GoldPrimary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 4.sp
                            )
                            Text(
                                text = "Share this 4-letter code with other players",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Players & Seats (4 Required):",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4 SEAT CARDS
                    for (seat in Seat.entries) {
                        val player = currentRoom.players[seat]
                        val isTeamA = seat.team == Team.TEAM_A
                        val teamColor = if (isTeamA) TeamABlue else TeamBRed
                        val teamLabel = if (isTeamA) "Team A" else "Team B"

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (player != null) Color(0xCC182A1D) else Color(0x660E1A11)
                            ),
                            border = BorderStroke(1.dp, if (player != null) teamColor else Color(0x33FFFFFF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (player != null) teamColor else Color(0x33FFFFFF))
                                ) {
                                    Icon(
                                        imageVector = if (player?.id?.startsWith("bot") == true) Icons.Default.SmartToy else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = player?.name ?: "Empty Seat (${seat.displayName})",
                                        color = if (player != null) Color.White else Color.Gray,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${seat.displayName} • $teamLabel",
                                        color = teamColor,
                                        fontSize = 11.sp
                                    )
                                }

                                if (player != null) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Ready",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // HELPER ACTION: Fill empty seats with bots
                    if (!currentRoom.isFull) {
                        OutlinedButton(
                            onClick = { multiplayerManager.fillEmptySeatsWithBots() },
                            border = BorderStroke(1.dp, GoldPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fill_bots_btn")
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fill Empty Seats with Bots for Testing")
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // START MATCH BUTTON
                    Button(
                        onClick = {
                            if (multiplayerManager.startGame()) {
                                onStartGame()
                            }
                        },
                        enabled = currentRoom.isFull,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0x33FFFFFF),
                            disabledContentColor = Color(0x66FFFFFF)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_online_match_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentRoom.isFull) "Start Match Now" else "Waiting for 4 Players...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { multiplayerManager.leaveRoom() },
                        border = BorderStroke(1.dp, Color(0xFFFF5252)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Leave Room")
                    }
                }
            }
        }
    }
}
