package com.example.ui.home

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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Card as ModelCard
import com.example.model.Rank
import com.example.model.Suit
import com.example.ui.components.PlayingCardView
import com.example.ui.theme.FeltDark
import com.example.ui.theme.FeltEmerald
import com.example.ui.theme.FeltLight
import com.example.ui.theme.FeltMedium
import com.example.ui.theme.GoldPrimary

@Composable
fun HomeScreen(
    onPlayOffline: (practiceMode: Boolean) -> Unit,
    onOpenMultiplayer: () -> Unit,
    onOpenHowToPlay: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(FeltLight, FeltMedium, FeltDark)
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // TITLE BADGE
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xDD0A1E11))
                    .border(2.dp, GoldPrimary, RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "29 TAASH",
                        color = GoldPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "उन्तीस ताश • Authentic Card Game",
                        color = Color(0xFFA0B3A5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CARD FAN DISPLAY (Jack of Spades & 9 of Hearts)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                PlayingCardView(
                    card = ModelCard(Suit.SPADES, Rank.JACK),
                    isSelected = true,
                    width = 62.dp,
                    height = 90.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                PlayingCardView(
                    card = ModelCard(Suit.HEARTS, Rank.NINE),
                    isSelected = true,
                    width = 62.dp,
                    height = 90.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                PlayingCardView(
                    card = ModelCard(Suit.DIAMONDS, Rank.ACE),
                    isSelected = true,
                    width = 62.dp,
                    height = 90.dp
                )
            }

            Text(
                text = "Jack = 3 pts • 9 = 2 pts • Ace = 1 pt • 10 = 1 pt\nTotal: 28 Card Pts + 1 Last Trick Bonus = 29 Pts",
                color = Color(0xFFD0E1D5),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // MAIN ACTION BUTTONS
            // 1. Quick Play vs AI (Offline)
            MenuActionButton(
                title = "Quick Play vs AI",
                subtitle = "Play standard 4-player 29 Taash offline",
                icon = Icons.Default.PlayArrow,
                buttonColor = GoldPrimary,
                textColor = Color.Black,
                testTag = "home_quick_play_btn",
                onClick = { onPlayOffline(false) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Practice Mode (with Hints & Undo)
            MenuActionButton(
                title = "Practice Mode (अभ्यास)",
                subtitle = "Learn strategy with unlimited Hints & Undo",
                icon = Icons.Default.School,
                buttonColor = Color(0xFF33691E),
                textColor = Color.White,
                testTag = "home_practice_mode_btn",
                onClick = { onPlayOffline(true) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Online Multiplayer
            MenuActionButton(
                title = "Online Multiplayer",
                subtitle = "Create or join 4-player room with code",
                icon = Icons.Default.Groups,
                buttonColor = Color(0xFF1565C0),
                textColor = Color.White,
                testTag = "home_multiplayer_btn",
                onClick = onOpenMultiplayer
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SECONDARY ACTIONS: How to Play, Stats, Settings
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SecondaryMenuButton(
                    title = "Rules",
                    icon = Icons.Default.HelpOutline,
                    testTag = "home_rules_btn",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenHowToPlay
                )
                SecondaryMenuButton(
                    title = "Stats",
                    icon = Icons.Default.BarChart,
                    testTag = "home_stats_btn",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenStats
                )
                SecondaryMenuButton(
                    title = "Settings",
                    icon = Icons.Default.Settings,
                    testTag = "home_settings_btn",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenSettings
                )
            }
        }
    }
}

@Composable
private fun MenuActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    buttonColor: Color,
    textColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = buttonColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0x33000000))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun SecondaryMenuButton(
    title: String,
    icon: ImageVector,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0x66FFD54F)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0x33000000),
            contentColor = Color.White
        ),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = GoldPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
