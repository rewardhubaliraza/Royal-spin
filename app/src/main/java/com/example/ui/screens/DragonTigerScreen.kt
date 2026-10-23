package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class DTOption(val title: String, val multiplier: Int, val emoji: String, val color: Color) {
    DRAGON("DRAGON", 2, "🐉", Color(0xFFFF2A55)),
    TIE("TIE", 8, "🤝", Color(0xFF00E676)),
    TIGER("TIGER", 2, "🐯", Color(0xFFFFB300))
}

data class CardRank(val name: String, val value: Int, val suit: String)

@Composable
fun DragonTigerScreen(viewModel: GameViewModel) {
    BackHandler { viewModel.navigateTo("LOBBY") }

    val currentUser by viewModel.currentUser.collectAsState()
    val lowWinningMode by viewModel.lowWinningMode.collectAsState()
    val scope = rememberCoroutineScope()

    var phase by remember { mutableStateOf("BETTING") } // BETTING, DEALING, RESULT
    var countdown by remember { mutableIntStateOf(10) }
    var selectedChip by remember { mutableIntStateOf(100) }

    var userBets by remember { mutableStateOf<Map<DTOption, Int>>(emptyMap()) }
    var dragonCard by remember { mutableStateOf<CardRank?>(null) }
    var tigerCard by remember { mutableStateOf<CardRank?>(null) }
    var winnerOption by remember { mutableStateOf<DTOption?>(null) }

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
            // BETTING PHASE
            phase = "BETTING"
            dragonCard = null
            tigerCard = null
            winnerOption = null
            lastRoundWin = 0.0

            for (sec in 10 downTo 1) {
                countdown = sec
                delay(1000)
            }
            countdown = 0

            // DEALING PHASE
            phase = "DEALING"
            delay(1200)

            // Generate card values (Ace = 1, King = 13)
            val suits = listOf("♠️", "♥️", "♦️", "♣️")
            val ranks = listOf(
                "A" to 1, "2" to 2, "3" to 3, "4" to 4, "5" to 5,
                "6" to 6, "7" to 7, "8" to 8, "9" to 9, "10" to 10,
                "J" to 11, "Q" to 12, "K" to 13
            )

            // Controlled Low Winning algorithm as requested by admin
            val userBetDragon = userBets[DTOption.DRAGON] ?: 0
            val userBetTiger = userBets[DTOption.TIGER] ?: 0

            val decidedWinner: DTOption = if (lowWinningMode) {
                // Bias towards the outcome player did NOT bet on!
                when {
                    userBetDragon > userBetTiger -> DTOption.TIGER
                    userBetTiger > userBetDragon -> DTOption.DRAGON
                    else -> if (Random.nextInt(100) < 30) DTOption.TIE else listOf(DTOption.DRAGON, DTOption.TIGER).random()
                }
            } else {
                val roll = Random.nextInt(100)
                when {
                    roll < 12 -> DTOption.TIE
                    roll < 56 -> DTOption.DRAGON
                    else -> DTOption.TIGER
                }
            }

            val dVal: Int
            val tVal: Int
            when (decidedWinner) {
                DTOption.DRAGON -> {
                    dVal = Random.nextInt(8, 14) // 8 to 13
                    tVal = Random.nextInt(1, dVal)
                }
                DTOption.TIGER -> {
                    tVal = Random.nextInt(8, 14) // 8 to 13
                    dVal = Random.nextInt(1, tVal)
                }
                DTOption.TIE -> {
                    val tieVal = Random.nextInt(2, 12)
                    dVal = tieVal
                    tVal = tieVal
                }
            }

            val dRank = ranks.first { it.second == dVal }
            val tRank = ranks.first { it.second == tVal }

            dragonCard = CardRank(dRank.first, dRank.second, suits.random())
            delay(800)
            tigerCard = CardRank(tRank.first, tRank.second, suits.random())
            delay(800)

            winnerOption = decidedWinner

            // RESULT & PAYOUT
            phase = "RESULT"
            val userBetOnWinner = userBets[decidedWinner] ?: 0
            if (userBetOnWinner > 0) {
                val gross = userBetOnWinner.toDouble() * decidedWinner.multiplier
                val fee = gross * 0.10 // 10% fee strictly deducted!
                val net = gross - fee

                lastRoundGross = gross
                lastRoundFee = fee
                lastRoundWin = net

                // Credit net win to user with 10% fee applied
                viewModel.creditUserWinning(net, gross, "Dragon vs Tiger")
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
                            text = "🐉 DRAGON vs TIGER 🐯",
                            color = CasinoGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Controlled Low Payout • Fast Battle",
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
                        "DEALING" -> {
                            Text("FLIPPING CARDS...", color = CasinoCyan, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                        "RESULT" -> {
                            Text(
                                text = "🏆 WINNER: ${winnerOption?.title ?: ""}!",
                                color = CasinoGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cards Arena (Dragon vs Tiger)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dragon Card Box
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (winnerOption == DTOption.DRAGON) Color(0xFF4A1020) else CasinoPurpleCard
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        if (winnerOption == DTOption.DRAGON) 2.dp else 1.dp,
                        if (winnerOption == DTOption.DRAGON) CasinoGold else CasinoPurpleCardBorder
                    ),
                    modifier = Modifier.weight(1f).height(150.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🐉 DRAGON", color = Color(0xFFFF2A55), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        if (dragonCard != null) {
                            Text(text = "${dragonCard!!.name} ${dragonCard!!.suit}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
                            Text(text = "Rank: ${dragonCard!!.value}", color = TextSecondary, fontSize = 12.sp)
                        } else {
                            Text(text = "🂠", fontSize = 48.sp, color = TextSecondary)
                            Text(text = "Waiting...", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }

                // VS Circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CasinoGoldDark)
                        .align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                    Text("VS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                // Tiger Card Box
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (winnerOption == DTOption.TIGER) Color(0xFF4A3510) else CasinoPurpleCard
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        if (winnerOption == DTOption.TIGER) 2.dp else 1.dp,
                        if (winnerOption == DTOption.TIGER) CasinoGold else CasinoPurpleCardBorder
                    ),
                    modifier = Modifier.weight(1f).height(150.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🐯 TIGER", color = Color(0xFFFFB300), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        if (tigerCard != null) {
                            Text(text = "${tigerCard!!.name} ${tigerCard!!.suit}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
                            Text(text = "Rank: ${tigerCard!!.value}", color = TextSecondary, fontSize = 12.sp)
                        } else {
                            Text(text = "🂠", fontSize = 48.sp, color = TextSecondary)
                            Text(text = "Waiting...", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3 Betting Options (Dragon 2x, Tie 8x, Tiger 2x)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DTOption.entries.forEach { opt ->
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
                            .weight(if (opt == DTOption.TIE) 0.8f else 1.1f)
                            .height(110.dp)
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

                            Text(opt.emoji, fontSize = 24.sp)
                            Text(opt.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)

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
                    Text("🎉 DRAGON TIGER WIN! 🎉", color = CasinoGold, fontWeight = FontWeight.Black)
                },
                text = {
                    Column {
                        Text("Winning: ${winnerOption?.title ?: ""}", color = TextPrimary, fontWeight = FontWeight.Bold)
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
