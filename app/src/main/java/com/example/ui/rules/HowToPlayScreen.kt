package com.example.ui.rules

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FeltDark
import com.example.ui.theme.FeltLight
import com.example.ui.theme.FeltMedium
import com.example.ui.theme.GoldPrimary

@Composable
fun HowToPlayScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                // Header
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
                        text = "How to Play 29 Taash (नियम)",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bidding & Scoring Quick Sheet Button
                var showRulesDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                if (showRulesDialog) {
                    com.example.ui.components.BiddingScoringRulesDialog(onDismiss = { showRulesDialog = false })
                }

                androidx.compose.material3.Button(
                    onClick = { showRulesDialog = true },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("open_rules_sheet_btn")
                ) {
                    Text("📊 Open Interactive Bidding & Scoring Sheet", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Rule Card 1: The 32 Cards & 29 Points
                RuleSectionCard(
                    number = "1",
                    title = "32 Cards & 29 Points (ताश और अंक)",
                    content = """
                    • 29 Taash uses 32 cards: 7, 8, 9, 10, Jack, Queen, King, Ace from all 4 suits.
                    • Points in cards:
                        - Jack (J) = 3 Points (गुलाम)
                        - Nine (9) = 2 Points (नहला)
                        - Ace (A) = 1 Point (इक्का)
                        - Ten (10) = 1 Point (दहला)
                        - K, Q, 8, 7 = 0 Points
                    • Total points in 32 cards = 28 points.
                    • The team that wins the 8th (last) trick gets +1 bonus point!
                    • Total points = 28 + 1 = 29 Total Points!
                    """.trimIndent()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rule Card 2: Card Strength
                RuleSectionCard(
                    number = "2",
                    title = "Card Ranking / Strength (ताकत)",
                    content = """
                    In each suit or in trumps, the card strength from highest to lowest is:
                    J > 9 > A > 10 > K > Q > 8 > 7
                    
                    Notice that Jack is the strongest card, followed by 9, then Ace, then 10!
                    """.trimIndent()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rule Card 3: Teams & Dealing
                RuleSectionCard(
                    number = "3",
                    title = "Teams & Bidding System (बोली प्रणाली)",
                    content = """
                    • 4 Players divided into 2 Teams:
                        - Team A: South (You) & North (Partner)
                        - Team B: East & West (Opponents)
                    • Phase 1: 4 cards dealt to each player.
                    • Bidding starts at 16 up to 28 points.
                    • Each player either bids higher than the current bid or passes.
                    • The winning bidder selects a secret Trump suit (हुकुम).
                    • Phase 2: Remaining 4 cards dealt (8 cards total per player).
                    """.trimIndent()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rule Card 4: Contract Scoring System
                RuleSectionCard(
                    number = "4",
                    title = "Scoring & Game Points System (+1 / -2)",
                    content = """
                    Match jeetne ke liye teams ko +6 Game Points banane hote hain:
                    
                    • Contract Success (Bidder team scores >= Bid):
                        - Bid 16 to 20: Awarded +1 Game Point
                        - Bid 21 to 28 (High Bid): Awarded +2 Game Points
                        - If Doubled: 2x points (+2 or +4)
                    
                    • Contract Failure (Bidder team scores < Bid):
                        - Bid 16 to 20: Penalized -2 Game Points
                        - Bid 21+: Penalized -3 Game Points
                        - If Doubled: 2x penalty (-4 or -6)
                    
                    Note: Bid harne par penalty jeetne se dugni hoti hai (-2 vs +1)!
                    """.trimIndent()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rule Card 5: Trick Play & Trump Reveal
                RuleSectionCard(
                    number = "5",
                    title = "Trick Play & Trump Reveal (खेल और हुकुम)",
                    content = """
                    • Must follow suit: If you hold a card of the lead suit, you MUST play it.
                    • If you do NOT hold the lead suit:
                        - You can ask to 'Reveal Trump' (हुकुम खोलें)!
                        - Once trump is revealed, you must play a trump card if you hold one to cut and win the trick!
                        - Highest trump card played wins the trick.
                    """.trimIndent()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rule Card 6: Pair / Jodi Bonus
                RuleSectionCard(
                    number = "6",
                    title = "Royal Pair / Jodi (जोड़ी / मारी)",
                    content = """
                    • If the bidder holds both the King and Queen of the trump suit:
                        - After trump is revealed, they can declare 'Pair (Jodi)'.
                        - This reduces their target bid by 4 points (making the contract easier to achieve)!
                    • If an opponent declares Pair, the bidder's required target increases by 4 points!
                    """.trimIndent()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rule Card 7: Hints & Practice Undo
                RuleSectionCard(
                    number = "7",
                    title = "Hint & Undo in Practice Mode (सलाह और अनडू)",
                    content = """
                    • Tap 'Hint' (💡) at any time to receive AI advice on which card to play and why.
                    • In Practice Mode: Tap 'Undo' (↩️) to take back your last played card before the trick finishes if you want to try another strategy!
                    """.trimIndent()
                )
            }
        }
    }
}

@Composable
private fun RuleSectionCard(
    number: String,
    title: String,
    content: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD0E1A11)),
        border = BorderStroke(1.dp, Color(0x44FFD54F)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary)
                ) {
                    Text(
                        text = number,
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = GoldPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}
