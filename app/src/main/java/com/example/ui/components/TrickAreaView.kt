package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RulesEngine
import com.example.model.PlayedCard
import com.example.model.Seat
import com.example.model.Suit
import com.example.model.Trick
import com.example.ui.theme.FeltEmerald
import com.example.ui.theme.GoldPrimary

@Composable
fun TrickAreaView(
    trick: Trick,
    trumpSuit: Suit?,
    isTrumpRevealed: Boolean,
    trumpRevealedAtTrick: Int?,
    modifier: Modifier = Modifier
) {
    val winningPlay = RulesEngine.getCurrentWinningPlay(
        trick,
        trumpSuit,
        isTrumpRevealed,
        trumpRevealedAtTrick
    )

    val playBySeat = trick.plays.associateBy { it.playerSeat }

    Box(
        modifier = modifier
            .size(240.dp, 200.dp)
            .shadow(12.dp, RoundedCornerShape(32.dp))
            .background(Color(0x33000000), RoundedCornerShape(32.dp))
            .border(1.5.dp, Color(0x33FFD54F), RoundedCornerShape(32.dp))
            .testTag("trick_area"),
        contentAlignment = Alignment.Center
    ) {
        // Center info badge (Trick index & Points)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xDD0E1A11))
                .border(1.dp, GoldPrimary, CircleShape)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Trick #${trick.trickIndex + 1} / 8",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            val bonusText = if (trick.isLastTrick) " (+1 bonus)" else ""
            Text(
                text = "${trick.cardPoints} pts$bonusText",
                color = Color.White,
                fontSize = 10.sp
            )
        }

        // NORTH card (Top)
        playBySeat[Seat.NORTH]?.let { play ->
            TrickCardSlot(
                play = play,
                isWinning = winningPlay == play,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }

        // SOUTH card (Bottom)
        playBySeat[Seat.SOUTH]?.let { play ->
            TrickCardSlot(
                play = play,
                isWinning = winningPlay == play,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }

        // WEST card (Left)
        playBySeat[Seat.WEST]?.let { play ->
            TrickCardSlot(
                play = play,
                isWinning = winningPlay == play,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp)
            )
        }

        // EAST card (Right)
        playBySeat[Seat.EAST]?.let { play ->
            TrickCardSlot(
                play = play,
                isWinning = winningPlay == play,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            )
        }
    }
}

@Composable
private fun TrickCardSlot(
    play: PlayedCard,
    isWinning: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        PlayingCardView(
            card = play.card,
            isFaceUp = true,
            isSelected = isWinning,
            width = 48.dp,
            height = 68.dp,
            showPointsBadge = true
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = play.playerSeat.name.take(1),
            color = if (isWinning) GoldPrimary else Color(0xCCFFFFFF),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
