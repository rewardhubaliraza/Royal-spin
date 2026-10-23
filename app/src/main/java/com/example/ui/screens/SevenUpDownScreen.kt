package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class SevenOption(val title: String, val range: String, val multiplier: Int, val emoji: String, val color: Color) {
    DOWN("7 DOWN", "2 - 6", 2, "⬇️", Color(0xFF00E5FF)),
    LUCKY7("LUCKY 7", "Exact 7", 5, "🍀", Color(0xFFFFD700)),
    UP("7 UP", "8 - 12", 2, "⬆️", Color(0xFFFF2A55))
}

@Composable
fun SevenUpDownScreen(viewModel: GameViewModel) {
    BackHandler { viewModel.navigateTo("LOBBY") }

    val currentUser by viewModel.currentUser.collectAsState()
    val lowWinningMode by viewModel.lowWinningMode.collectAsState()

    var phase by remember { mutableStateOf("BETTING") } // BETTING, ROLLING, RESULT
    var countdown by remember { mutableIntStateOf(10) }
    var selectedChip by remember { mutableIntStateOf(100) }

    var userBets by remember { mutableStateOf<Map<SevenOption, Int>>(emptyMap()) }
    var die1 by remember { mutableIntStateOf(3) }
    var die2 by remember { mutableIntStateOf(4) }
    var winnerOption by remember { mutableStateOf<SevenOption?>(null) }

    var lastRoundWin by remember { mutableStateOf(0.0) }
    var lastRoundGross by remember { mutableStateOf(0.0) }
    var lastRoundFee by remember { mutableStateOf(0.0) }

    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Game loop
    LaunchedEffect(Unit) {
        while (true) {
            phase = "BETTING"
            winnerOption = null
            lastRoundWin = 0.0

            for (sec in 10 downTo 1) {
                countdown = sec
                delay(1000)
            }
            countdown = 0

            // ROLLING PHASE
            phase = "ROLLING"
            for (i in 1..8) {
                die1 = Random.nextInt(1, 7)
                die2 = Random.nextInt(1, 7)
                delay(150)
            }

            // Decide winner based on low winning mode
            val userBetDown = userBets[SevenOption.DOWN] ?: 0
            val userBetUp = userBets[SevenOption.UP] ?: 0
            val userBet7 = userBets[SevenOption.LUCKY7] ?: 0

            val decidedWinner = if (lowWinningMode) {
                when {
                    userBetDown > userBetUp && userBetDown > userBet7 -> SevenOption.UP
                    userBetUp > userBetDown && userBetUp > userBet7 -> SevenOption.DOWN
                    userBet7 > 0 -> if (Random.nextBoolean()) SevenOption.DOWN else SevenOption.UP
                    else -> SevenOption.LUCKY7 // When player didn't bet on 7, land on 7 so other bets lose!
                }
            } else {
                val sumRoll = Random.nextInt(2, 13)
                when {
                    sumRoll < 7 -> SevenOption.DOWN
                    sumRoll == 7 -> SevenOption.LUCKY7
                    else -> SevenOption.UP
                }
            }

            when (decidedWinner) {
                SevenOption.DOWN -> {
                    val s = Random.nextInt(2, 7) // 2 to 6
                    die1 = Random.nextInt(1, s)
                    die2 = s - die1
                }
                SevenOption.LUCKY7 -> {
                    die1 = 3
                    die2 = 4
                }
                SevenOption.UP -> {
                    val s = Random.nextInt(8, 13) // 8 to 12
                    die1 = Random.nextInt(s - 6, 7)
                    die2 = s - die1
                }
            }

            winnerOption = decidedWinner

            // RESULT & PAYOUT
            phase = "RESULT"
            val userBetOnWinner = userBets[decidedWinner] ?: 0
            if (userBetOnWinner > 0) {
                val gross = userBetOnWinner.toDouble() * decidedWinner.multiplier
                val fee = gross * 0.10 // 10% fee strictly deducted
                val net = gross - fee

                lastRoundGross = gross
                lastRoundFee = fee
                lastRoundWin = net

                viewModel.creditUserWinning(net, gross, "7 Up 7 Down")
            }

            delay(3500)
            userBets = emptyMap()
        }
    }

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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo("LOBBY") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CasinoGold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "🎲 7 UP 7 DOWN 🎲",
                            color = CasinoGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Fast Double Dice Prediction",
                            color = CasinoCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                // Balance Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = CasinoPurpleSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet", tint = CasinoGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rs. ${(currentUser?.balance ?: 0.0).toInt()}",
                            color = CasinoGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Phase Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (phase) {
                        "BETTING" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("BETTING OPEN", color = CasinoGreen, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                Box(
                                    modifier = Modifier
                                        .scale(pulseScale)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CasinoGold)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("${countdown}s", color = CasinoPurpleDark, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { countdown.toFloat() / 10f },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                                color = CasinoGold,
                                trackColor = CasinoPurpleSurface
                            )
                        }
                        "ROLLING" -> {
                            Text("ROLLING DICE...", color = CasinoCyan, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                        "RESULT" -> {
                            Text(
                                text = "🏆 RESULT: ${die1 + die2} (${winnerOption?.title ?: ""})",
                                color = CasinoGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3D Animated Dice Display
            Card(
                colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CasinoGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DieBox(value = die1)
                        Text("+", color = CasinoGold, fontWeight = FontWeight.Black, fontSize = 24.sp)
                        DieBox(value = die2)
                        Text("=", color = CasinoGold, fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CasinoGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${die1 + die2}", color = CasinoPurpleDark, fontWeight = FontWeight.Black, fontSize = 26.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3 Betting Options (Down 2-6 [2x], Lucky 7 [5x], Up 8-12 [2x])
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SevenOption.entries.forEach { opt ->
                    val isWinning = winnerOption == opt
                    val currentBet = userBets[opt] ?: 0

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isWinning) Color(0xFF381464) else CasinoPurpleCard
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isWinning) 2.5.dp else 1.dp,
                            if (isWinning) CasinoGold else CasinoPurpleCardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .clickable {
                                if (phase == "BETTING") {
                                    val user = currentUser ?: return@clickable
                                    if (user.balance >= selectedChip) {
                                        val deducted = viewModel.deductUserBalance(selectedChip.toDouble())
                                        if (deducted) {
                                            userBets = userBets + (opt to (currentBet + selectedChip))
                                        }
                                    }
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(opt.color.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${opt.multiplier}X", color = opt.color, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }

                            Text(opt.emoji, fontSize = 22.sp)
                            Text(opt.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(opt.range, color = CasinoGoldLight, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)

                            if (currentBet > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CasinoGold)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Rs. $currentBet", color = CasinoPurpleDark, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                }
                            } else {
                                Text("Tap to Bet", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chips Selection
            Card(
                colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("SELECT CHIP (PKR)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        val totalB = userBets.values.sum()
                        Text("Round Bet: Rs. $totalB", color = if (totalB > 0) CasinoGold else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val chips = listOf(50, 100, 200, 300, 500, 1000)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(chips) { chip ->
                            val isSel = selectedChip == chip
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) CasinoGold else CasinoPurpleSurface)
                                    .border(1.dp, if (isSel) CasinoGoldDark else CasinoPurpleCardBorder, CircleShape)
                                    .clickable { selectedChip = chip },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (chip >= 1000) "${chip / 1000}K" else "$chip",
                                    color = if (isSel) CasinoPurpleDark else TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            if (phase == "BETTING") {
                                userBets = emptyMap()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = CasinoRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Bets", color = CasinoRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Win popup
        if (phase == "RESULT" && lastRoundWin > 0) {
            AlertDialog(
                onDismissRequest = {},
                containerColor = CasinoPurpleCard,
                title = {
                    Text("🎉 7 UP 7 DOWN WIN! 🎉", color = CasinoGold, fontWeight = FontWeight.Black)
                },
                text = {
                    Column {
                        Text("Outcome: ${die1 + die2} (${winnerOption?.title ?: ""})", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Gross Win: Rs. ${lastRoundGross.toInt()}", color = TextSecondary, fontSize = 13.sp)
                        Text("10% System Commission: -Rs. ${lastRoundFee.toInt()}", color = CasinoRed, fontSize = 13.sp)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = CasinoPurpleCardBorder)
                        Text("Net Credited: +Rs. ${lastRoundWin.toInt()}", color = CasinoGold, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { lastRoundWin = 0.0 },
                        colors = ButtonDefaults.buttonColors(containerColor = CasinoGold)
                    ) {
                        Text("OK", color = CasinoPurpleDark, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
private fun DieBox(value: Int) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(2.dp, CasinoGold, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        val dots = when (value) {
            1 -> "⚀"
            2 -> "⚁"
            3 -> "⚂"
            4 -> "⚃"
            5 -> "⚄"
            else -> "⚅"
        }
        Text(text = dots, fontSize = 38.sp, color = Color.Black)
    }
}
