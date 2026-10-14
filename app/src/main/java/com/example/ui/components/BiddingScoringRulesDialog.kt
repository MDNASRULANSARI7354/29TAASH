package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.FeltDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TeamABlue
import com.example.ui.theme.TeamBRed

@Composable
fun BiddingScoringRulesDialog(
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Points (29 अंक)", "Bidding (बोली)", "Scoring (+1 / -2)", "Jodi (Pair)")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FeltDark),
            border = BorderStroke(2.dp, GoldPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .padding(4.dp)
                .testTag("bidding_scoring_rules_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Scoreboard,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "29 Rules & Scoring",
                            color = GoldPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF142E1B),
                    contentColor = GoldPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GoldPrimary,
                            height = 3.dp
                        )
                    },
                    edgePadding = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) GoldPrimary else Color(0xFFA0B3A5)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> PointsSystemContent()
                        1 -> BiddingSystemContent()
                        2 -> ScoringSystemContent()
                        3 -> JodiPairSystemContent()
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Dismiss Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("rules_dismiss_btn")
                ) {
                    Text("Got It! (समझ गया)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun PointsSystemContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ExplanationBanner(
            title = "Where do the 29 points come from?",
            subtitle = "32-card deck (7, 8, 9, 10, J, Q, K, A) me 28 points cards me hote hain aur 1 point 8th (aakhri) trick jeetne par milta hai!"
        )

        // Point breakdown table
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xCC142E1B)),
            border = BorderStroke(1.dp, Color(0x44FFD54F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Card Point Values (अंक तालिका)",
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                PointRow(card = "Jack (गुलाम)", points = "3 Points", count = "4 cards = 12 pts", power = "Highest rank")
                HorizontalDivider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 4.dp))
                PointRow(card = "Nine (नहला)", points = "2 Points", count = "4 cards = 8 pts", power = "2nd highest")
                HorizontalDivider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 4.dp))
                PointRow(card = "Ace (इक्का)", points = "1 Point", count = "4 cards = 4 pts", power = "3rd highest")
                HorizontalDivider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 4.dp))
                PointRow(card = "Ten (दहला)", points = "1 Point", count = "4 cards = 4 pts", power = "4th highest")
                HorizontalDivider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 4.dp))
                PointRow(card = "K, Q, 8, 7", points = "0 Points", count = "16 cards = 0 pts", power = "Zero point cards")
                HorizontalDivider(color = Color(0x44FFD54F), modifier = Modifier.padding(vertical = 6.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Total Points in Cards", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("28 Points", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Last Trick Bonus (8th Trick)", color = Color(0xFFA0B3A5), fontSize = 11.sp)
                    Text("+1 Point", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x33FFD54F), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Total Round Points", color = GoldPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    Text("29 Points!", color = GoldPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }

        // Card ranking order
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xCC0E1A11)),
            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Trick Power Hierarchy (ताकत का क्रम):",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "J > 9 > A > 10 > K > Q > 8 > 7",
                    color = GoldPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Jack is the absolute strongest card in any suit or in trumps. 7 is the lowest card.",
                    color = Color(0xFFA0B3A5),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun BiddingSystemContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ExplanationBanner(
            title = "What is Bidding (बोली)?",
            subtitle = "Har round ke shuru me pehle 4-4 cards baante jaate hain. Players estimate karte hain ki unki team 29 me se kitne points jeet sakti hai."
        )

        InfoItem(
            icon = Icons.Default.Gavel,
            title = "Bid Range: 16 to 28",
            description = "Minimum bid 16 points hoti hai. Bidder 16 se 28 tak kisi bhi number ki boli laga sakta hai. Boli hamesha pichhli boli se badi honi chahiye."
        )

        InfoItem(
            icon = Icons.Default.Star,
            title = "Secret Trump Selection (छुपा हुकुम)",
            description = "Jo player highest bid jeetta hai, wo apne pehle 4 cards me se ek secret Trump suit chunta hai. Ye trump card tab tak ulta (hidden) rehta hai jab tak koi player 'Show Trump' na maang le!"
        )

        InfoItem(
            icon = Icons.Default.Scoreboard,
            title = "Passing & Forced Dealer Rule",
            description = "Agar player bid nahi lagana chahta, wo 'Pass' kar sakta hai. Agar sabhi 4 players pass kar dein, toh dealer ko standard 16 ki boli par khelna padta hai."
        )

        InfoItem(
            icon = Icons.Default.MilitaryTech,
            title = "Double & Redouble",
            description = "Opponents bidder ki boli par 'Double' laga sakte hain (match points double ho jate hain). Bidder chahe toh jawab me 'Redouble' laga sakta hai (4x game points)."
        )
    }
}

@Composable
private fun ScoringSystemContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ExplanationBanner(
            title = "Contract Scoring & Game Points",
            subtitle = "Match jeetne ke liye +6 game points banane hote hain (ya opponent ko -6 tak girana hota hai)."
        )

        // Success vs Fail cards
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x334CAF50)),
            border = BorderStroke(1.dp, Color(0xFF4CAF50)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Contract Success (बोली पूरी होने पर)",
                    color = Color(0xFF81C784),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Agar bidder ki team apne bid target ya usse zyada points jeetti hai:",
                    color = Color.White,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Bid 16 to 20: Awarded +1 Game Point", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("• High Bid 21 to 28: Awarded +2 Game Points", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("• If Doubled: 2x points (+2 / +4)", color = Color(0xFFA0B3A5), fontSize = 10.sp)
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x33F44336)),
            border = BorderStroke(1.dp, Color(0xFFE57373)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Contract Failure / Defeat (बोली टूटने पर)",
                    color = Color(0xFFFF8A80),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Agar bidder ki team target tak nahi pahunch paati:",
                    color = Color.White,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Bid 16 to 20: Penalized -2 Game Points", color = Color(0xFFFFCDD2), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("• High Bid 21+: Penalized -3 Game Points", color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("• Note: Harne par penalty jeetne ke reward se zyada hoti hai (-2 vs +1)!", color = Color(0xFFFFD54F), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun JodiPairSystemContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ExplanationBanner(
            title = "Royal Pair / Jodi Rule (मारी / जोड़ी)",
            subtitle = "Trump suit ke King aur Queen ko 'Jodi' kaha jata hai. Iska score par bada asar padta hai!"
        )

        InfoItem(
            icon = Icons.Default.Star,
            title = "Bidder's Jodi: -4 Target Points",
            description = "Agar bidder ke paas Trump ka King aur Queen dono hain, aur trump reveal ho chuka hai, toh wo 'Pair' claim kar sakta hai. Isse bidder ka required target 4 points kam ho jata hai (jaise 18 ka bid 14 ho jata hai)."
        )

        InfoItem(
            icon = Icons.Default.MilitaryTech,
            title = "Opponent's Jodi: +4 Target Points",
            description = "Agar opponent team trump ka King aur Queen reveal hone ke baad dikha de, toh bidder ka target 4 points badh jata hai (jaise 17 ban jaata hai 21)."
        )

        InfoItem(
            icon = Icons.Default.Scoreboard,
            title = "Condition to Declare Jodi",
            description = "1. Trump suit reveal hona zaroori hai.\n2. Player ke haath me usi trump suit ke King aur Queen dono hone chahiye.\n3. Jodi declare karne ke liye screen par 'Pair (Jodi)' button tap karein."
        )
    }
}

@Composable
private fun PointRow(card: String, points: String, count: String, power: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(card, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(power, color = Color(0xFFA0B3A5), fontSize = 10.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(points, color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(count, color = Color(0xFFA0B3A5), fontSize = 10.sp)
        }
    }
}

@Composable
private fun ExplanationBanner(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x33FFD54F))
            .border(1.dp, Color(0x66FFD54F), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(title, color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun InfoItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xCC0E1A11))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color(0x33FFD54F))
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}
