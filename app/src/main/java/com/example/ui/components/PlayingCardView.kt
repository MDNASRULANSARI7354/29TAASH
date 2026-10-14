package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Card as ModelCard
import com.example.ui.theme.CardBorderGray
import com.example.ui.theme.CardFaceWhite
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuitBlack
import com.example.ui.theme.SuitRed

@Composable
fun PlayingCardView(
    card: ModelCard?,
    modifier: Modifier = Modifier,
    isFaceUp: Boolean = true,
    isSelected: Boolean = false,
    isPlayable: Boolean = true,
    width: Dp = 64.dp,
    height: Dp = 92.dp,
    showPointsBadge: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val offsetY by animateDpAsState(
        targetValue = if (isSelected) (-14).dp else 0.dp,
        animationSpec = spring(),
        label = "card_elevation"
    )

    val cardShape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .size(width, height)
            .offset(y = offsetY)
            .alpha(if (isPlayable) 1.0f else 0.6f)
            .testTag(if (card != null) "card_${card.id}" else "card_facedown")
            .then(
                if (onClick != null && isPlayable) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
    ) {
        if (!isFaceUp || card == null) {
            // Card Back (Patterned Felt Navy Blue)
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16324F)),
                border = BorderStroke(1.5.dp, GoldPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF0F2338), Color(0xFF1E4369), Color(0xFF0F2338))
                            ),
                            shape = RoundedCornerShape(5.dp)
                        )
                        .border(1.dp, Color(0x66FFD54F), RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "29",
                        color = GoldPrimary,
                        fontSize = (width.value * 0.35f).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Card Face
            val textColor = if (card.isRed) SuitRed else SuitBlack

            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = CardFaceWhite),
                border = BorderStroke(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) GoldPrimary else CardBorderGray
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 8.dp else 3.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    // Top-left rank & suit
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = card.rank.symbol,
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = card.suit.symbol,
                            color = textColor,
                            fontSize = 12.sp,
                            lineHeight = 12.sp
                        )
                    }

                    // Center big suit symbol
                    Text(
                        text = card.suit.symbol,
                        color = textColor,
                        fontSize = (width.value * 0.42f).sp,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Points badge if Jack, 9, Ace, or 10
                    if (showPointsBadge && card.points > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(
                                    color = if (card.points >= 2) Color(0xFFFF8F00) else Color(0xFF1976D2),
                                    shape = CircleShape
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${card.points}p",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bottom-right inverted rank & suit
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .rotate(180f)
                    ) {
                        Text(
                            text = card.rank.symbol,
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = card.suit.symbol,
                            color = textColor,
                            fontSize = 12.sp,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }
    }
}
