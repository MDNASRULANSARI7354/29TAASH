package com.example.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RulesEngine
import com.example.model.GamePhase
import com.example.model.GameState
import com.example.model.Seat
import com.example.model.Suit
import com.example.ui.components.BiddingDialog
import com.example.ui.components.BiddingScoringRulesDialog
import com.example.ui.components.HintDialog
import com.example.ui.components.PlayerSeatView
import com.example.ui.components.PlayingCardView
import com.example.ui.components.RoundOverDialog
import com.example.ui.components.TrickAreaView
import com.example.ui.components.TrumpSelectionDialog
import com.example.ui.theme.FeltDark
import com.example.ui.theme.FeltEmerald
import com.example.ui.theme.FeltLight
import com.example.ui.theme.FeltMedium
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuitRed
import com.example.ui.theme.TeamABlue
import com.example.ui.theme.TeamBRed
import com.example.ui.theme.WoodBorder

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val currentHint by viewModel.currentHint.collectAsState()
    val showHintDialog by viewModel.showHintDialog.collectAsState()
    val illegalToast by viewModel.illegalMoveToast.collectAsState()
    var showRulesDialog by remember { androidx.compose.runtime.mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(illegalToast) {
        illegalToast?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearIllegalMoveToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(FeltLight, FeltMedium, FeltDark)
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // TOP HEADER: Match Score, Trump & Contract Status
                GameHeader(
                    gameState = gameState,
                    onBack = onNavigateBack,
                    onRestart = { viewModel.startNewMatch() },
                    onTogglePractice = { viewModel.togglePracticeMode() },
                    onOpenRules = { showRulesDialog = true }
                )

                // TABLE FELT AREA: North, East, West and Center Trick Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // NORTH (Partner) - Top Center
                    PlayerSeatView(
                        seat = Seat.NORTH,
                        playerName = "Partner (North)",
                        cardCount = gameState.hands[Seat.NORTH]?.size ?: 0,
                        tricksWon = gameState.completedTricks.count { it.winnerSeat == Seat.NORTH },
                        isCurrentTurn = gameState.currentTurn == Seat.NORTH && gameState.phase == GamePhase.TRICK_PLAY,
                        isDealer = gameState.dealer == Seat.NORTH,
                        isBidder = gameState.bidder == Seat.NORTH,
                        bidAmount = if (gameState.bidder == Seat.NORTH) gameState.highestBid?.amount else null,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                    // WEST - Left Center
                    PlayerSeatView(
                        seat = Seat.WEST,
                        playerName = "West",
                        cardCount = gameState.hands[Seat.WEST]?.size ?: 0,
                        tricksWon = gameState.completedTricks.count { it.winnerSeat == Seat.WEST },
                        isCurrentTurn = gameState.currentTurn == Seat.WEST && gameState.phase == GamePhase.TRICK_PLAY,
                        isDealer = gameState.dealer == Seat.WEST,
                        isBidder = gameState.bidder == Seat.WEST,
                        bidAmount = if (gameState.bidder == Seat.WEST) gameState.highestBid?.amount else null,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    // EAST - Right Center
                    PlayerSeatView(
                        seat = Seat.EAST,
                        playerName = "East",
                        cardCount = gameState.hands[Seat.EAST]?.size ?: 0,
                        tricksWon = gameState.completedTricks.count { it.winnerSeat == Seat.EAST },
                        isCurrentTurn = gameState.currentTurn == Seat.EAST && gameState.phase == GamePhase.TRICK_PLAY,
                        isDealer = gameState.dealer == Seat.EAST,
                        isBidder = gameState.bidder == Seat.EAST,
                        bidAmount = if (gameState.bidder == Seat.EAST) gameState.highestBid?.amount else null,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )

                    // CENTER TRICK AREA
                    TrickAreaView(
                        trick = gameState.currentTrick,
                        trumpSuit = gameState.trumpSuit,
                        isTrumpRevealed = gameState.isTrumpRevealed,
                        trumpRevealedAtTrick = gameState.trumpRevealedAtTrick,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // BOTTOM AREA: Status Message, Tactical Action Bar, and Player Hand
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0x00000000), Color(0xCC000000), FeltDark)
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    // Status bar
                    Text(
                        text = gameState.statusMessage,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Action Controls: Hint, Undo, Reveal Trump, Pair
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        // HINT BUTTON
                        Button(
                            onClick = { viewModel.requestHint() },
                            enabled = gameState.phase == GamePhase.TRICK_PLAY,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF33691E),
                                contentColor = GoldPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("hint_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint",
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hint", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // UNDO BUTTON (Practice Mode)
                        if (gameState.isPracticeMode) {
                            Button(
                                onClick = { viewModel.undoLastHumanMove() },
                                enabled = gameState.canHumanUndo,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF263238),
                                    contentColor = if (gameState.canHumanUndo) Color.White else Color(0x66FFFFFF),
                                    disabledContainerColor = Color(0x22FFFFFF),
                                    disabledContentColor = Color(0x44FFFFFF)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("undo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Undo,
                                    contentDescription = "Undo",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Undo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // REVEAL TRUMP BUTTON (Active if player is void in lead suit & trump is hidden)
                        val canRevealTrump = !gameState.isTrumpRevealed &&
                                gameState.phase == GamePhase.TRICK_PLAY &&
                                gameState.currentTurn == Seat.SOUTH &&
                                gameState.currentTrick.leadSuit != null &&
                                gameState.humanHand.none { it.suit == gameState.currentTrick.leadSuit }

                        if (canRevealTrump) {
                            Button(
                                onClick = { viewModel.revealTrump() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9800),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(38.dp)
                                    .testTag("reveal_trump_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Reveal Trump",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Show Trump", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // DECLARE PAIR / JODI
                        if (RulesEngine.canDeclarePair(gameState, Seat.SOUTH)) {
                            Button(
                                onClick = { viewModel.declarePair() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("declare_pair_btn")
                            ) {
                                Text("Pair (Jodi)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // HUMAN PLAYER HAND (South)
                    val legalCards = if (gameState.isHumanTurn) {
                        RulesEngine.getLegalCards(gameState, Seat.SOUTH)
                    } else emptyList()

                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-16).dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(105.dp)
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 4.dp)
                            .testTag("human_hand")
                    ) {
                        val hand = gameState.humanHand
                        for (card in hand) {
                            val isLegal = legalCards.contains(card) || !gameState.isHumanTurn
                            val isRecommended = currentHint?.recommendedCard == card

                            PlayingCardView(
                                card = card,
                                isFaceUp = true,
                                isSelected = isRecommended,
                                isPlayable = if (gameState.isHumanTurn) isLegal else true,
                                width = 56.dp,
                                height = 82.dp,
                                onClick = {
                                    if (gameState.isHumanTurn) {
                                        viewModel.playHumanCard(card)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // DIALOGS & OVERLAYS
        if (gameState.phase == GamePhase.BIDDING && gameState.currentTurn == Seat.SOUTH) {
            BiddingDialog(
                currentHighestBid = gameState.highestBid,
                onBidSelected = { amount -> viewModel.placeHumanBid(amount) },
                onPass = { viewModel.passHumanBid() }
            )
        }

        if (gameState.phase == GamePhase.TRUMP_SELECTION && gameState.bidder == Seat.SOUTH) {
            TrumpSelectionDialog(
                initialCards = gameState.humanHand,
                onSuitSelected = { suit -> viewModel.selectHumanTrump(suit) }
            )
        }

        if (showHintDialog && currentHint != null) {
            HintDialog(
                recommendation = currentHint!!,
                onPlayRecommendedCard = { card ->
                    if (gameState.isHumanTurn) {
                        viewModel.playHumanCard(card)
                    }
                },
                onRevealTrump = { viewModel.revealTrump() },
                onDismiss = { viewModel.dismissHint() }
            )
        }

        if (gameState.phase == GamePhase.ROUND_FINISHED || gameState.phase == GamePhase.MATCH_FINISHED) {
            RoundOverDialog(
                gameState = gameState,
                onNextRound = { viewModel.nextRound() },
                onNewMatch = { viewModel.startNewMatch() }
            )
        }

        if (showRulesDialog) {
            BiddingScoringRulesDialog(onDismiss = { showRulesDialog = false })
        }
    }
}

@Composable
private fun GameHeader(
    gameState: GameState,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    onTogglePractice: () -> Unit,
    onOpenRules: () -> Unit
) {
    Surface(
        color = FeltDark,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column {
                    Text(
                        text = "Round #${gameState.roundNumber}",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pts: ${gameState.teamARoundPoints} - ${gameState.teamBRoundPoints} / 29",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }

            // TRUMP & BID STATUS
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Trump suit display
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Trump: ",
                        color = Color(0xFFA0B3A5),
                        fontSize = 11.sp
                    )
                    if (gameState.isTrumpRevealed && gameState.trumpSuit != null) {
                        val suitColor = if (gameState.trumpSuit.isRed) SuitRed else Color.White
                        Text(
                            text = "${gameState.trumpSuit.displayName} ${gameState.trumpSuit.symbol}",
                            color = suitColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Hidden 🔒",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Bid info
                val bidText = if (gameState.highestBid != null) {
                    "Bid: ${gameState.highestBid.amount} (${gameState.bidder?.team?.shortName})"
                } else "Bidding..."
                Text(
                    text = bidText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // RIGHT ACTIONS: Match points, Practice mode chip, Restart
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Match score badge
                Column(horizontalAlignment = Alignment.End) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "A:${gameState.teamAGamePoints}",
                            color = TeamABlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "B:${gameState.teamBGamePoints}",
                            color = TeamBRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Practice indicator
                    Text(
                        text = if (gameState.isPracticeMode) "Practice ON" else "Standard",
                        color = if (gameState.isPracticeMode) Color(0xFF4CAF50) else Color(0xFFA0B3A5),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onTogglePractice)
                    )
                }

                IconButton(
                    onClick = onOpenRules,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("header_rules_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Bidding & Scoring Rules",
                        tint = GoldPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(onClick = onRestart, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
