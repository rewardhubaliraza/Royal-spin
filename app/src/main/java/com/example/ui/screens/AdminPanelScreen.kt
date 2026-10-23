package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GameItemEntity
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
fun AdminPanelScreen(viewModel: GameViewModel) {
    BackHandler { viewModel.navigateTo("LOBBY") }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Pending Deposits", "Pending Withdrawals", "Update Account", "Upload Game", "House Edge / Rig", "Users", "Finance")

    val deposits by viewModel.allDeposits.collectAsState()
    val withdrawals by viewModel.allWithdrawals.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val allGames by viewModel.allGames.collectAsState()
    val jazzCashNumber by viewModel.jazzCashNumber.collectAsState()
    val jazzCashTitle by viewModel.jazzCashTitle.collectAsState()
    val forcedWinner by viewModel.forcedWinner.collectAsState()
    val lowWinningMode by viewModel.lowWinningMode.collectAsState()

    var editJazzCashNumber by remember(jazzCashNumber) { mutableStateOf(jazzCashNumber) }
    var editJazzCashTitle by remember(jazzCashTitle) { mutableStateOf(jazzCashTitle) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CasinoPurpleDark, Color(0xFF130728), Color(0xFF090214))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo("LOBBY") },
                    modifier = Modifier.testTag("admin_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CasinoGold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "Security", tint = CasinoGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MASTER ADMIN PANEL",
                            color = CasinoGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                    }
                    Text(
                        text = "Full Cloud System Control & Real-Time Sync",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Tabs Bar
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = CasinoPurpleCard,
                contentColor = CasinoGold,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CasinoGold,
                        height = 3.dp
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val pendingCount = when (index) {
                        0 -> deposits.count { it.status == "PENDING" }
                        1 -> withdrawals.count { it.status == "PENDING" }
                        else -> 0
                    }
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) CasinoGold else TextSecondary,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                                if (pendingCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(CasinoRed)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text("$pendingCount", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                when (selectedTab) {
                    0 -> DepositsTab(
                        deposits = deposits,
                        onApprove = { viewModel.approveDeposit(it) },
                        onReject = { viewModel.rejectDeposit(it) }
                    )
                    1 -> WithdrawalsTab(
                        withdrawals = withdrawals,
                        onApprove = { viewModel.approveWithdrawal(it) },
                        onReject = { viewModel.rejectWithdrawal(it) }
                    )
                    2 -> AccountUpdateTab(
                        number = editJazzCashNumber,
                        title = editJazzCashTitle,
                        onNumberChange = { editJazzCashNumber = it },
                        onTitleChange = { editJazzCashTitle = it },
                        onSave = { viewModel.updateJazzCashDetails(editJazzCashNumber, editJazzCashTitle) }
                    )
                    3 -> GameUploadTab(
                        games = allGames,
                        onUploadNew = { title, sub, icon, cat, minB ->
                            viewModel.addNewGame(title, sub, icon, cat, minB)
                        }
                    )
                    4 -> HouseRigTab(
                        currentForced = forcedWinner,
                        lowWinning = lowWinningMode,
                        onToggleLowWinning = { viewModel.toggleLowWinningMode(it) },
                        onSelectSlot = { viewModel.setForcedNextWinner(it) }
                    )
                    5 -> UsersTab(users = users)
                    6 -> FinanceTab(deposits = deposits, withdrawals = withdrawals)
                }
            }
        }
    }
}

@Composable
private fun DepositsTab(
    deposits: List<com.example.data.local.DepositRequestEntity>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    if (deposits.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No deposit requests found", color = TextSecondary)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(deposits) { dep ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Rs. ${dep.amount.toInt()}",
                                    color = CasinoGold,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                if (dep.bonusAmount > 0) {
                                    Text(
                                        text = "+Rs. ${dep.bonusAmount.toInt()} (10% First Deposit Free Bonus)",
                                        color = CasinoGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Total To Credit: Rs. ${dep.totalCredited.toInt()}",
                                        color = CasinoGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (dep.status) {
                                            "APPROVED" -> CasinoGreen.copy(alpha = 0.2f)
                                            "REJECTED" -> CasinoRed.copy(alpha = 0.2f)
                                            else -> CasinoGold.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = dep.status,
                                    color = when (dep.status) {
                                        "APPROVED" -> CasinoGreen
                                        "REJECTED" -> CasinoRed
                                        else -> CasinoGold
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Player: ${dep.userPhone}", color = TextPrimary, fontSize = 12.sp)
                        Text(text = "Sender Number: ${dep.senderPhone}", color = TextSecondary, fontSize = 12.sp)
                        Text(text = "Transaction ID (TID): ${dep.transactionId}", color = CasinoCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        if (dep.status == "PENDING") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onApprove(dep.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CasinoGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Approve", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Approve & Credit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { onReject(dep.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoRed),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Reject", tint = CasinoRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reject", color = CasinoRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WithdrawalsTab(
    withdrawals: List<com.example.data.local.WithdrawalRequestEntity>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    if (withdrawals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No withdrawal requests found", color = TextSecondary)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(withdrawals) { req ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Request: Rs. ${req.grossAmount.toInt()}",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "10% Fee: -Rs. ${req.feeAmount.toInt()} | Net Payable: Rs. ${req.netAmount.toInt()}",
                                    color = CasinoGold,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (req.status) {
                                            "APPROVED" -> CasinoGreen.copy(alpha = 0.2f)
                                            "REJECTED" -> CasinoRed.copy(alpha = 0.2f)
                                            else -> CasinoGold.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = req.status,
                                    color = when (req.status) {
                                        "APPROVED" -> CasinoGreen
                                        "REJECTED" -> CasinoRed
                                        else -> CasinoGold
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "User Phone: ${req.userPhone} (${req.accountTitle})", color = TextPrimary, fontSize = 12.sp)
                        Text(text = "Destination: ${req.accountNumber} (${req.paymentMethod})", color = CasinoCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gameplay Check: Total Bets Rs. ${req.userTotalBets.toInt()} | Total Wins Rs. ${req.userTotalWins.toInt()}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        if (req.status == "PENDING") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onApprove(req.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CasinoGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Sent", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Approve (Transfer Rs. ${req.netAmount.toInt()})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { onReject(req.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoRed),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Reject", tint = CasinoRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reject & Refund", color = CasinoRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountUpdateTab(
    number: String,
    title: String,
    onNumberChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "UPDATE LIVE JAZZCASH DEPOSIT NUMBER",
                color = CasinoGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Any change made here will immediately update for all 10,000+ players across the entire app!",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = number,
                onValueChange = onNumberChange,
                label = { Text("JazzCash Account Number") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CasinoGold,
                    unfocusedBorderColor = CasinoPurpleCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_jazzcash_number_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Account Holder Title") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CasinoGold,
                    unfocusedBorderColor = CasinoPurpleCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_jazzcash_title_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = CasinoGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("admin_save_jazzcash_button")
            ) {
                Text(
                    text = "SAVE & SYNC TO ALL PLAYERS",
                    color = CasinoPurpleDark,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun GameUploadTab(
    games: List<GameItemEntity>,
    onUploadNew: (String, String, String, String, Int) -> Unit
) {
    var newTitle by remember { mutableStateOf("") }
    var newSubtitle by remember { mutableStateOf("") }
    var newEmoji by remember { mutableStateOf("🎰") }
    var newCategory by remember { mutableStateOf("ROULETTE") }
    var newMinBet by remember { mutableStateOf("50") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Upload Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Games, contentDescription = "Games", tint = CasinoGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UPLOAD NEW GAME / ADD GAME MODE",
                        color = CasinoGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Game Name (e.g. Royal Crash, Mines 2)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CasinoGold,
                        unfocusedBorderColor = CasinoPurpleCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newSubtitle,
                    onValueChange = { newSubtitle = it },
                    label = { Text("Short Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CasinoGold,
                        unfocusedBorderColor = CasinoPurpleCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newEmoji,
                        onValueChange = { newEmoji = it },
                        label = { Text("Icon Emoji") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CasinoGold,
                            unfocusedBorderColor = CasinoPurpleCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = newMinBet,
                        onValueChange = { newMinBet = it.filter { c -> c.isDigit() } },
                        label = { Text("Min Bet (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CasinoGold,
                            unfocusedBorderColor = CasinoPurpleCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            onUploadNew(
                                newTitle,
                                newSubtitle,
                                newEmoji,
                                newCategory,
                                newMinBet.toIntOrNull() ?: 50
                            )
                            newTitle = ""
                            newSubtitle = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CasinoGold),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = CasinoPurpleDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("UPLOAD GAME TO LOBBY", color = CasinoPurpleDark, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Existing Games List
        Text(
            text = "CURRENT LOBBY GAMES",
            color = CasinoGoldLight,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        games.forEach { g ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = g.iconEmoji, fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = g.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "${g.category} • Min Rs. ${g.minBet}", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (g.isEnabled) CasinoGreen.copy(alpha = 0.2f) else CasinoRed.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (g.isEnabled) "ACTIVE" else "DISABLED",
                            color = if (g.isEnabled) CasinoGreen else CasinoRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HouseRigTab(
    currentForced: String,
    lowWinning: Boolean,
    onToggleLowWinning: (Boolean) -> Unit,
    onSelectSlot: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "HOUSE WINNINGS CONTROL & RIG ENGINE",
                    color = CasinoGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Low winning algorithm toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Low Winning House Mode",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Limits payouts, favours Zero wipeout & low multipliers (Dog/Mercedes) to protect house funds.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = lowWinning,
                        onCheckedChange = onToggleLowWinning,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CasinoGold,
                            checkedTrackColor = CasinoGoldDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = CasinoPurpleCardBorder)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Force Next Spin Outcome (Current: $currentForced):",
                    color = CasinoCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ZERO FORCE BUTTON (House Wipeout as requested!)
                Button(
                    onClick = { onSelectSlot("ZERO") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("0️⃣ FORCE ZERO (HOUSE WIPEOUT - ALL BETS LOSE)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // AUTO / FAIR RNG
                OutlinedButton(
                    onClick = { onSelectSlot("AUTO") },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎲 AUTO FAIR RNG (STANDARD WEIGHTED)", color = CasinoGreen, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Or Force Specific Animal/Car:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                val allItems = RouletteSlot.TOP_ROW + RouletteSlot.BOTTOM_ROW
                allItems.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { slot ->
                            Button(
                                onClick = { onSelectSlot(slot.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentForced == slot.id) CasinoGold else CasinoPurpleSurface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Text(
                                    text = "${slot.iconEmoji} ${slot.title} (${slot.multiplier}x)",
                                    color = if (currentForced == slot.id) CasinoPurpleDark else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun UsersTab(users: List<com.example.data.local.UserEntity>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(users) { u ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = u.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (u.isAdmin) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CasinoGold)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("ADMIN", color = CasinoPurpleDark, fontWeight = FontWeight.Black, fontSize = 9.sp)
                                }
                            }
                        }
                        Text(text = "Phone: ${u.phone}", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = "Bets: Rs. ${u.totalBets.toInt()} | Wins: Rs. ${u.totalWins.toInt()}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Rs. ${u.balance.toInt()}",
                            color = CasinoGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Dep: ${u.totalDeposits.toInt()}",
                            color = CasinoGreen,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceTab(
    deposits: List<com.example.data.local.DepositRequestEntity>,
    withdrawals: List<com.example.data.local.WithdrawalRequestEntity>
) {
    val totalApprovedDeposits = deposits.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val totalApprovedWithdrawals = withdrawals.filter { it.status == "APPROVED" }.sumOf { it.grossAmount }
    val totalFeesCollected = withdrawals.filter { it.status == "APPROVED" }.sumOf { it.feeAmount }
    val netPaidToUsers = withdrawals.filter { it.status == "APPROVED" }.sumOf { it.netAmount }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FinanceCard("Total Approved Deposits", "Rs. ${totalApprovedDeposits.toInt()}", CasinoGreen)
        FinanceCard("Total Withdrawals Processed", "Rs. ${totalApprovedWithdrawals.toInt()}", CasinoRed)
        FinanceCard("10% Withdrawal Fees Collected", "Rs. ${totalFeesCollected.toInt()}", CasinoGold)
        FinanceCard("Net Cash Paid to Users", "Rs. ${netPaidToUsers.toInt()}", CasinoCyan)
        FinanceCard("Estimated Net House Profit", "Rs. ${(totalApprovedDeposits - netPaidToUsers).toInt()}", CasinoGold)
    }
}

@Composable
private fun FinanceCard(title: String, value: String, accent: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(value, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}
