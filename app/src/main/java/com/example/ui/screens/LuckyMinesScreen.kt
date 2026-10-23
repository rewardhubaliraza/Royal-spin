package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlin.random.Random

data class TileState(
    val index: Int,
    val isMine: Boolean,
    val isRevealed: Boolean = false
)

@Composable
fun LuckyMinesScreen(viewModel: GameViewModel) {
    BackHandler { viewModel.navigateTo("LOBBY") }

    val currentUser by viewModel.currentUser.collectAsState()
    val lowWinningMode by viewModel.lowWinningMode.collectAsState()

    var selectedBet by remember { mutableIntStateOf(100) }
    var isGameActive by remember { mutableStateOf(false) }
    var tiles by remember { mutableStateOf(List(25) { TileState(it, isMine = false) }) }
    var revealedCount by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var hitMine by remember { mutableStateOf(false) }

    var lastWinGross by remember { mutableStateOf(0.0) }
    var lastWinNet by remember { mutableStateOf(0.0) }
    var lastWinFee by remember { mutableStateOf(0.0) }
    var showWinDialog by remember { mutableStateOf(false) }

    val multipliers = listOf(
        1.15, 1.40, 1.75, 2.25, 2.95, 4.00, 5.60, 8.20, 12.50, 20.00,
        35.00, 65.00, 130.00, 300.00, 750.00, 2000.00, 5000.00
    )
    val currentMultiplier = if (revealedCount > 0 && revealedCount <= multipliers.size) multipliers[revealedCount - 1] else 1.0
    val potentialGross = selectedBet * currentMultiplier
    val potentialFee = potentialGross * 0.10
    val potentialNet = potentialGross - potentialFee

    val infiniteTransition = rememberInfiniteTransition(label = "cashout_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cashout_pulse"
    )

    fun startNewGame() {
        val userBalance = currentUser?.balance ?: 0.0
        if (userBalance < selectedBet) {
            return
        }

        val success = viewModel.deductUserBalance(selectedBet.toDouble())
        if (!success) return

        // Place 3 mines
        val mineIndices = mutableSetOf<Int>()
        while (mineIndices.size < 3) {
            mineIndices.add(Random.nextInt(25))
        }

        tiles = List(25) { idx ->
            TileState(index = idx, isMine = mineIndices.contains(idx), isRevealed = false)
        }
        revealedCount = 0
        isGameActive = true
        isGameOver = false
        hitMine = false
    }

    fun revealTile(index: Int) {
        if (!isGameActive || isGameOver) return
        val tile = tiles[index]
        if (tile.isRevealed) return

        // Controlled House Edge Check
        var actualIsMine = tile.isMine
        if (lowWinningMode && revealedCount >= 3 && !actualIsMine) {
            // 65% chance house turns this tile into mine if player reaches high multiplier
            if (Random.nextInt(100) < 65) {
                actualIsMine = true
            }
        }

        if (actualIsMine) {
            // Hit a Mine! Explosion!
            hitMine = true
            isGameOver = true
            isGameActive = false
            tiles = tiles.map { t ->
                if (t.index == index || t.isMine) t.copy(isMine = true, isRevealed = true) else t
            }
        } else {
            // Safe Diamond!
            revealedCount += 1
            tiles = tiles.map { t ->
                if (t.index == index) t.copy(isMine = false, isRevealed = true) else t
            }
        }
    }

    fun cashOut() {
        if (!isGameActive || revealedCount == 0) return
        isGameActive = false
        isGameOver = true

        val gross = potentialGross
        val fee = potentialFee
        val net = potentialNet

        lastWinGross = gross
        lastWinFee = fee
        lastWinNet = net
        showWinDialog = true

        // Credit winnings to user
        viewModel.creditUserWinning(net, gross, "Lucky Mines")

        // Reveal remaining tiles
        tiles = tiles.map { it.copy(isRevealed = true) }
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
                .padding(bottom = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo("LOBBY") },
                        modifier = Modifier
                            .size(38.dp)
                            .background(CasinoPurpleCard, CircleShape)
                            .testTag("mines_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CasinoGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "LUCKY MINES",
                            color = CasinoGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "3 Mines • Safe Diamonds Multiply Cash",
                            color = CasinoCyan,
                            fontSize = 10.sp
                        )
                    }
                }

                // Balance Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = CasinoPurpleSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = CasinoGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Rs. ${(currentUser?.balance ?: 0.0).toInt()}",
                            color = CasinoGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Hero Graphic Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .padding(horizontal = 14.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_lucky_mines),
                        contentDescription = "Lucky Mines Banner",
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
                                text = "💎 22 DIAMONDS | 💣 3 MINES",
                                color = CasinoGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Reveal diamonds & Cash out before hitting a mine!",
                                color = TextPrimary,
                                fontSize = 10.sp
                            )
                        }

                        if (isGameActive && revealedCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CasinoGreen)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = String.format("%.2fX", currentMultiplier),
                                    color = CasinoPurpleDark,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5x5 Mines Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CasinoPurpleCardBorder),
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        contentPadding = PaddingValues(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tiles.size) { idx ->
                            val tile = tiles[idx]
                            MinesTileView(
                                tile = tile,
                                isGameActive = isGameActive,
                                onClick = { revealTile(idx) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Control Area (Chips / Cash Out / Start Game)
            Card(
                colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!isGameActive) {
                        // Chips Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SELECT BET AMOUNT",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Selected: Rs. $selectedBet",
                                color = CasinoGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val chipOptions = listOf(50, 100, 200, 500, 1000, 2000)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(chipOptions) { chip ->
                                val isSelected = selectedBet == chip
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) CasinoGold else CasinoPurpleSurface)
                                        .border(
                                            1.dp,
                                            if (isSelected) CasinoGoldLight else CasinoPurpleCardBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedBet = chip }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = "Rs. $chip",
                                        color = if (isSelected) CasinoPurpleDark else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { startNewGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = CasinoGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("mines_start_button")
                        ) {
                            Text(
                                text = if (hitMine) "💥 REPLAY MINES (Rs. $selectedBet)" else "START GAME (Rs. $selectedBet)",
                                color = CasinoPurpleDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // Game in Progress: Cash Out or Revealed Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Diamonds: $revealedCount / 22",
                                    color = CasinoCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Gross: Rs. ${potentialGross.toInt()} (10% fee: Rs. ${potentialFee.toInt()})",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            Text(
                                text = "Net: Rs. ${potentialNet.toInt()}",
                                color = CasinoGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { cashOut() },
                            enabled = revealedCount > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = CasinoGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .scale(if (revealedCount > 0) pulseScale else 1f)
                                .testTag("mines_cashout_button")
                        ) {
                            Text(
                                text = if (revealedCount == 0) "TAP ANY TILE TO PLAY" else "CASH OUT RS. ${potentialNet.toInt()}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Win Celebration Dialog (10% Fee strictly explained)
    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { showWinDialog = false },
            title = {
                Text(
                    text = "🎉 CASHED OUT SUCCESSFULLY!",
                    color = CasinoGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Congratulations! You safely avoided the mines!",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gross Win: Rs. ${lastWinGross.toInt()}",
                        color = CasinoCyan,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "House Fee (10%): Rs. ${lastWinFee.toInt()}",
                        color = CasinoRed,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Net Credited to Balance: Rs. ${lastWinNet.toInt()}",
                        color = CasinoGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showWinDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CasinoGold)
                ) {
                    Text("PLAY AGAIN", color = CasinoPurpleDark, fontWeight = FontWeight.Black)
                }
            },
            containerColor = CasinoPurpleCard
        )
    }
}

@Composable
private fun MinesTileView(
    tile: TileState,
    isGameActive: Boolean,
    onClick: () -> Unit
) {
    val bgAnim by animateColorAsState(
        targetValue = when {
            !tile.isRevealed -> Color(0xFF261047)
            tile.isMine -> Color(0xFFB71C1C)
            else -> Color(0xFF00695C)
        },
        label = "tile_bg"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = bgAnim),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (!tile.isRevealed) CasinoPurpleCardBorder else if (tile.isMine) CasinoRed else CasinoCyan
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(enabled = isGameActive && !tile.isRevealed) { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!tile.isRevealed) {
                Text(
                    text = "❓",
                    fontSize = 18.sp,
                    color = CasinoGold.copy(alpha = 0.5f)
                )
            } else if (tile.isMine) {
                Text(
                    text = "💣",
                    fontSize = 24.sp
                )
            } else {
                Text(
                    text = "💎",
                    fontSize = 24.sp
                )
            }
        }
    }
}
