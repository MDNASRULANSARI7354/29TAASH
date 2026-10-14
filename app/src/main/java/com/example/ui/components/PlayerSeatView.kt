package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Seat
import com.example.model.Team
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TeamABlue
import com.example.ui.theme.TeamBRed

@Composable
fun PlayerSeatView(
    seat: Seat,
    playerName: String,
    cardCount: Int,
    tricksWon: Int,
    isCurrentTurn: Boolean,
    isDealer: Boolean,
    isBidder: Boolean,
    bidAmount: Int?,
    speechBubbleText: String? = null,
    modifier: Modifier = Modifier
) {
    val isTeamA = seat.team == Team.TEAM_A
    val teamColor = if (isTeamA) TeamABlue else TeamBRed
    val teamLabel = if (isTeamA) "Team A" else "Team B"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag("seat_${seat.name.lowercase()}")
    ) {
        // Speech Bubble
        AnimatedVisibility(
            visible = !speechBubbleText.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = speechBubbleText.orEmpty(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Avatar & Info Box
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .shadow(if (isCurrentTurn) 8.dp else 2.dp, RoundedCornerShape(20.dp))
                .background(Color(0xCC0E1A11), RoundedCornerShape(20.dp))
                .border(
                    width = if (isCurrentTurn) 2.5.dp else 1.dp,
                    color = if (isCurrentTurn) GoldPrimary else Color(0x44FFFFFF),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Avatar Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(teamColor)
            ) {
                Icon(
                    imageVector = if (seat == Seat.SOUTH) Icons.Default.Person else Icons.Default.SmartToy,
                    contentDescription = playerName,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )

                if (isDealer) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary)
                    ) {
                        Text(
                            text = "D",
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = playerName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = teamLabel,
                        color = teamColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (cardCount > 0) {
                        Text(
                            text = "$cardCount cards",
                            color = Color(0xFFA0B3A5),
                            fontSize = 10.sp
                        )
                    }
                    if (tricksWon > 0) {
                        Text(
                            text = "• $tricksWon tricks",
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (isBidder && bidAmount != null) {
                        Text(
                            text = "• Bid $bidAmount",
                            color = Color(0xFFFF9800),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
