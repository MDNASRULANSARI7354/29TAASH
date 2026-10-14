package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.engine.BiddingEngine
import com.example.model.Bid
import com.example.model.Seat
import com.example.ui.theme.FeltDark
import com.example.ui.theme.GoldPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BiddingDialog(
    currentHighestBid: Bid?,
    minBid: Int = 16,
    onBidSelected: (Int) -> Unit,
    onPass: () -> Unit
) {
    val highestAmount = currentHighestBid?.amount ?: (minBid - 1)
    val nextValidBid = maxOf(minBid, highestAmount + 1)

    var showRulesDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showRulesDialog) {
        BiddingScoringRulesDialog(onDismiss = { showRulesDialog = false })
    }

    Dialog(onDismissRequest = { /* Modal, must make choice */ }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FeltDark),
            border = androidx.compose.foundation.BorderStroke(2.dp, GoldPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("bidding_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Bidding Phase (बोली)",
                        color = GoldPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Rules help chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FFD54F))
                        .border(1.dp, Color(0x66FFD54F), RoundedCornerShape(12.dp))
                        .clickable { showRulesDialog = true }
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                        .testTag("bidding_rules_help_btn")
                ) {
                    Text(
                        text = "📖 Bidding & Scoring Rules",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val currentBidText = if (currentHighestBid != null) {
                    "Current Highest: ${currentHighestBid.amount} by ${currentHighestBid.bidderSeat.displayName}"
                } else {
                    "No bids placed yet. Minimum bid: $minBid"
                }

                Text(
                    text = currentBidText,
                    color = Color.White,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select Bid Amount (16 - 28 points):",
                    color = Color(0xFFA0B3A5),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.Center,
                    maxItemsInEachRow = 5,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (amount in minBid..28) {
                        val isSelectable = amount > highestAmount
                        Button(
                            onClick = { onBidSelected(amount) },
                            enabled = isSelectable,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (amount == nextValidBid) GoldPrimary else Color(0xFF1B4D2E),
                                contentColor = if (amount == nextValidBid) Color.Black else Color.White,
                                disabledContainerColor = Color(0x33FFFFFF),
                                disabledContentColor = Color(0x66FFFFFF)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(3.dp)
                                .testTag("bid_btn_$amount")
                        ) {
                            Text(
                                text = "$amount",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onPass,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bid_pass_btn")
                    ) {
                        Text(text = "Pass", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    if (nextValidBid <= 28) {
                        Button(
                            onClick = { onBidSelected(nextValidBid) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bid_quick_btn")
                        ) {
                            Text(text = "Bid $nextValidBid", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
