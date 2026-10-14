package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.engine.ScoringEngine
import com.example.model.GamePhase
import com.example.model.GameState
import com.example.model.Team
import com.example.ui.theme.FeltDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TeamABlue
import com.example.ui.theme.TeamBRed

@Composable
fun RoundOverDialog(
    gameState: GameState,
    onNextRound: () -> Unit,
    onNewMatch: () -> Unit
) {
    val roundResult = ScoringEngine.evaluateRound(gameState)
    val isMatchFinished = gameState.phase == GamePhase.MATCH_FINISHED
    val matchWinner = gameState.matchWinner

    Dialog(onDismissRequest = { /* Must click button */ }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FeltDark),
            border = BorderStroke(2.dp, GoldPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("round_over_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isMatchFinished) "Match Concluded! 🏆" else "Round #${gameState.roundNumber} Finished",
                    color = GoldPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Summary banner
                val bannerBg = if (roundResult.contractSucceeded) Color(0x334CAF50) else Color(0x33F44336)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bannerBg, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = roundResult.summary,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Score comparison table
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Team A
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x332979FF), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Team A (You & Partner)",
                            color = TeamABlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${gameState.teamARoundPoints} / 29 pts",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Match Game Pts: ${gameState.teamAGamePoints}",
                            color = GoldPrimary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.padding(4.dp))

                    // Team B
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x33FF3D00), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Team B (East & West)",
                            color = TeamBRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${gameState.teamBRoundPoints} / 29 pts",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Match Game Pts: ${gameState.teamBGamePoints}",
                            color = GoldPrimary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (isMatchFinished && matchWinner != null) {
                    val winnerTitle = if (matchWinner == Team.TEAM_A) "Congratulations! Team A Won The Match! 🎉" else "Team B Won The Match!"
                    Text(
                        text = winnerTitle,
                        color = GoldPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNewMatch,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_match_btn")
                    ) {
                        Text("Start New Match", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onNextRound,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("next_round_btn")
                    ) {
                        Text("Next Round ➔", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
