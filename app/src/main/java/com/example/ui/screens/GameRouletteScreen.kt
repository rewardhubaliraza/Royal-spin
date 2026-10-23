package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.GamePhase
import com.example.model.RouletteSlot
import com.example.ui.theme.CasinoCyan
import com.example.ui.theme.CasinoGold
import com.example.ui.theme.CasinoGoldDark
import com.example.ui.theme.CasinoGoldLight
import com.example.ui.theme.CasinoGreen
import com.example.ui.theme.CasinoPurpleCard
import com.example.ui.theme.CasinoPurpleCardBorder
import com.example.ui.theme.CasinoPurpleDark
import com.example.ui.theme.CasinoPurpleSurface
import com.example.ui.theme.CasinoRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.GameViewModel

@Composable
fun GameRouletteScreen(viewModel: GameViewModel) {
    androidx.activity.compose.BackHandler { viewModel.navigateTo("LOBBY") }

    val currentUser by viewModel.currentUser.collectAsState()
    val gamePhase by viewModel.gamePhase.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val highlightIndex by viewModel.highlightIndex.collectAsState()
    val userBets by viewModel.userBets.collectAsState()
    val slotTotalBets by viewModel.slotTotalBets.collectAsState()
    val selectedChip by viewModel.selectedChip.collectAsState()
    val winningSlot by viewModel.winningSlot.collectAsState()
    val lastRoundWin by viewModel.lastRoundWin.collectAsState()
    val lastRoundGrossWin by viewModel.lastRoundGrossWin.collectAsState()
    val lastRoundWinFee by viewModel.lastRoundWinFee.collectAsState()
    val liveTickers by viewModel.liveTickers.collectAsState()

    val currentSlotInWheel = RouletteSlot.WHEEL_SEQUENCE.getOrNull(highlightIndex)
    val totalPlacedBet = userBets.values.sum()

    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CasinoPurpleDark, Color(0xFF130728), Color(0xFF090214))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
        ) {
            // 1. TOP HEADER BAR
            TopPlayerHeader(
                userName = currentUser?.name ?: "Player",
                phone = currentUser?.phone ?: "",
                balance = currentUser?.balance ?: 0.0,
                isAdmin = currentUser?.phone == "03073715192",
                onBackToLobby = { viewModel.navigateTo("LOBBY") },
                onDepositClick = { viewModel.navigateTo("DEPOSIT") },
                onWithdrawClick = { viewModel.navigateTo("WITHDRAWAL") },
                onHistoryClick = { viewModel.navigateTo("HISTORY") },
                onAdminClick = { viewModel.navigateTo("ADMIN") },
                onLogoutClick = { viewModel.logout() }
            )

            // 2. LIVE TICKER (Deposits, Withdrawals, Wins)
            LiveTickerBar(tickers = liveTickers)

            Spacer(modifier = Modifier.height(8.dp))

            // Realistic Roulette Table Artwork
            Card(
                colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .padding(horizontal = 12.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_roulette_banner),
                        contentDescription = "Realistic Roulette Table",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xE6100522), Color(0x66100522), Color(0xCC100522))
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "ROYAL MULTIPLIER ROULETTE",
                                color = CasinoGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Max 4 items bet • 10% Winning Commission Fee",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CasinoGold)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "UP TO 25X",
                                color = CasinoPurpleDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. ROULETTE STATUS & PHASE BANNER
            RouletteStatusCard(
                phase = gamePhase,
                countdown = countdown,
                winningSlot = winningSlot,
                lastRoundWin = lastRoundWin,
                pulseScale = pulseScale
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. SPECIAL ZERO (0) SLOT
            val isZeroHighlighted = currentSlotInWheel == RouletteSlot.ZERO
            ZeroSlotCard(
                slot = RouletteSlot.ZERO,
                isHighlighted = isZeroHighlighted,
                userBet = userBets[RouletteSlot.ZERO] ?: 0,
                totalBets = slotTotalBets[RouletteSlot.ZERO] ?: 0,
                onBetClick = { viewModel.placeBet(RouletteSlot.ZERO) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 5. TOP ROW SLOTS (High Multipliers: Panther 10x, Dragon 15x, Shark 25x, Elephant 20x)
            RowHeader(title = "★ TOP MULTIPLIERS (HIGH REWARDS)")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RouletteSlot.TOP_ROW.forEach { slot ->
                    val isHighlighted = currentSlotInWheel == slot
                    SlotCard(
                        slot = slot,
                        isHighlighted = isHighlighted,
                        userBet = userBets[slot] ?: 0,
                        totalBet = (slotTotalBets[slot] ?: 0) + (userBets[slot] ?: 0),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.placeBet(slot) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. BOTTOM ROW SLOTS (Frequent Multipliers: Horse 5x, Dog 3x, Mercedes 3x, Bike 4x)
            RowHeader(title = "♦ CLASSIC MULTIPLIERS (FREQUENT WINS)")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RouletteSlot.BOTTOM_ROW.forEach { slot ->
                    val isHighlighted = currentSlotInWheel == slot
                    SlotCard(
                        slot = slot,
                        isHighlighted = isHighlighted,
                        userBet = userBets[slot] ?: 0,
                        totalBet = (slotTotalBets[slot] ?: 0) + (userBets[slot] ?: 0),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.placeBet(slot) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. BETTING CHIPS BAR & CONTROLS
            BettingChipsSection(
                selectedChip = selectedChip,
                onChipSelected = { viewModel.selectChip(it) },
                totalBet = totalPlacedBet,
                isBettingActive = gamePhase == GamePhase.BETTING,
                onClearClick = { viewModel.clearBets() },
                onDoubleClick = { viewModel.doubleBets() }
            )
        }

        // WIN CELEBRATION POPUP
        AnimatedVisibility(
            visible = gamePhase == GamePhase.RESULT && lastRoundWin > 0,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            WinCelebrationDialog(
                winningSlot = winningSlot,
                winAmount = lastRoundWin,
                grossWin = lastRoundGrossWin,
                feeAmount = lastRoundWinFee,
                onDismiss = {}
            )
        }
    }
}

@Composable
private fun TopPlayerHeader(
    userName: String,
    phone: String,
    balance: Double,
    isAdmin: Boolean,
    onBackToLobby: () -> Unit,
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // User info with Back to Lobby
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToLobby,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Lobby",
                            tint = CasinoGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(CasinoGold, CasinoGoldDark))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            fontWeight = FontWeight.Black,
                            color = CasinoPurpleDark,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = userName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (phone.length >= 7) phone.take(4) + "***" + phone.takeLast(3) else phone,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Balance Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = CasinoPurpleSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CasinoGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = CasinoGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rs. ${balance.toInt()}",
                            color = CasinoGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(CasinoGold)
                                .clickable { onDepositClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Funds",
                                tint = CasinoPurpleDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onDepositClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CasinoGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("deposit_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Deposit", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deposit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onWithdrawClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("withdraw_button")
                ) {
                    Icon(Icons.Default.Payments, contentDescription = "Withdraw", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Withdraw", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onHistoryClick,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("history_button")
                ) {
                    Icon(Icons.Default.History, contentDescription = "History", tint = CasinoCyan, modifier = Modifier.size(16.dp))
                }

                if (isAdmin) {
                    Button(
                        onClick = onAdminClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CasinoGoldDark),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("admin_panel_button")
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Logout", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun LiveTickerBar(tickers: List<com.example.model.LiveTickerItem>) {
    val displayTicker = tickers.firstOrNull() ?: return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF230D42))
            .padding(horizontal = 14.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Live",
            tint = CasinoGreen,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "LIVE: ${displayTicker.phoneMasked} • Rs. ${displayTicker.amount.toInt()} ${displayTicker.note}",
            color = CasinoGoldLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RouletteStatusCard(
    phase: GamePhase,
    countdown: Int,
    winningSlot: RouletteSlot?,
    lastRoundWin: Double,
    pulseScale: Float
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (phase) {
                GamePhase.BETTING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(CasinoGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BETTING OPEN",
                                color = CasinoGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        // Countdown Timer badge
                        Box(
                            modifier = Modifier
                                .scale(pulseScale)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CasinoGold)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${countdown}s",
                                color = CasinoPurpleDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { countdown.toFloat() / 15f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = CasinoGold,
                        trackColor = CasinoPurpleSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Place your chips on any Animal / Car slot below!",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                GamePhase.SPINNING -> {
                    Text(
                        text = "🌀 ROULETTE WHEEL SPINNING...",
                        color = CasinoCyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Good Luck! Watch the golden highlight pointer...",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                GamePhase.RESULT -> {
                    if (winningSlot == RouletteSlot.ZERO) {
                        Text(
                            text = "0️⃣ ZERO HIT: HOUSE WIPEOUT!",
                            color = CasinoRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "All animal & car bets lost to the house this round",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    } else if (winningSlot != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = winningSlot.iconEmoji,
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "WINNER: ${winningSlot.title.uppercase()}",
                                    color = CasinoGold,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Multiplier: ${winningSlot.multiplier}X",
                                    color = CasinoGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZeroSlotCard(
    slot: RouletteSlot,
    isHighlighted: Boolean,
    userBet: Int,
    totalBets: Int,
    onBetClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isHighlighted) CasinoGold else Color(0xFF4A148C),
        label = "zeroBorder"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) Color(0xFF311B92) else Color(0xFF1E0C3E)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(if (isHighlighted) 2.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable { onBetClick() }
            .testTag("slot_zero")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6200EA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("0", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ZERO (0) - HOUSE WIPEOUT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "If pointer lands on 0, house collects all bets",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            if (isHighlighted) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CasinoGold)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("ACTIVE", color = CasinoPurpleDark, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun RowHeader(title: String) {
    Text(
        text = title,
        color = CasinoGoldLight,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
        letterSpacing = 1.sp
    )
}

@Composable
private fun SlotCard(
    slot: RouletteSlot,
    isHighlighted: Boolean,
    userBet: Int,
    totalBet: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val animatedBorder by animateColorAsState(
        targetValue = if (isHighlighted) CasinoGold else Color(slot.badgeColorHex).copy(alpha = 0.4f),
        label = "slotBorder"
    )
    val animatedBg by animateColorAsState(
        targetValue = if (isHighlighted) Color(0xFF451978) else CasinoPurpleCard,
        label = "slotBg"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = animatedBg),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(if (isHighlighted) 2.5.dp else 1.dp, animatedBorder),
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() }
            .testTag("slot_${slot.id.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Multiplier Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(slot.badgeColorHex))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${slot.multiplier}X",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
            }

            // Realistic Medallion Emblem
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(slot.badgeColorHex).copy(alpha = 0.45f),
                                Color(0xFF1E0C36)
                            )
                        )
                    )
                    .border(1.5.dp, Color(slot.badgeColorHex), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = slot.iconEmoji,
                    fontSize = 25.sp
                )
            }

            // Title
            Text(
                text = slot.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Total / User Bet Tag
            if (userBet > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CasinoGold)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "Rs. $userBet",
                        color = CasinoPurpleDark,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            } else {
                Text(
                    text = if (totalBet > 0) "Rs. $totalBet" else "Tap to bet",
                    color = if (totalBet > 0) CasinoGoldLight else TextSecondary.copy(alpha = 0.6f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun BettingChipsSection(
    selectedChip: Int,
    onChipSelected: (Int) -> Unit,
    totalBet: Int,
    isBettingActive: Boolean,
    onClearClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT CHIP AMOUNT",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "My Round Bet: Rs. $totalBet",
                    color = if (totalBet > 0) CasinoGold else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chips Row
            val chips = listOf(10, 50, 100, 500, 1000, 5000)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(chips) { chip ->
                    val isSelected = selectedChip == chip
                    ChipButton(
                        amount = chip,
                        isSelected = isSelected,
                        onClick = { onChipSelected(chip) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Controls: Clear Bets, Double Bets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClearClick,
                    enabled = isBettingActive && totalBet > 0,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (totalBet > 0) CasinoRed else CasinoPurpleCardBorder
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = CasinoRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear", color = CasinoRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onDoubleClick,
                    enabled = isBettingActive && totalBet > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = CasinoGoldDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.KeyboardDoubleArrowUp, contentDescription = "Double", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Double 2X", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ChipButton(
    amount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val chipColor = when (amount) {
        10 -> Color(0xFF1E88E5)
        50 -> Color(0xFF43A047)
        100 -> Color(0xFFE53935)
        500 -> Color(0xFF8E24AA)
        1000 -> Color(0xFFFFB300)
        else -> Color(0xFF00ACC1)
    }

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(chipColor, chipColor.copy(alpha = 0.7f))
                )
            )
            .border(
                width = if (isSelected) 3.dp else 1.5.dp,
                color = if (isSelected) CasinoGold else Color.White.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable { onClick() }
            .testTag("chip_$amount"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (amount >= 1000) "${amount / 1000}K" else "$amount",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun WinCelebrationDialog(
    winningSlot: RouletteSlot?,
    winAmount: Double,
    grossWin: Double,
    feeAmount: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CasinoPurpleCard,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 WINNING PAYOUT! 🎉",
                    color = CasinoGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = winningSlot?.iconEmoji ?: "🏆",
                    fontSize = 54.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = winningSlot?.title ?: "",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "${winningSlot?.multiplier}X MULTIPLIER",
                    color = CasinoGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = CasinoPurpleSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gross Win:", color = TextSecondary, fontSize = 12.sp)
                            Text("Rs. ${grossWin.toInt()}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("System Fee (10%):", color = CasinoRed, fontSize = 12.sp)
                            Text("-Rs. ${feeAmount.toInt()}", color = CasinoRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Net Added to Balance:", color = CasinoGold, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            Text("+Rs. ${winAmount.toInt()}", color = CasinoGold, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CasinoGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("COLLECT WINNINGS", color = CasinoPurpleDark, fontWeight = FontWeight.Black)
            }
        }
    )
}
