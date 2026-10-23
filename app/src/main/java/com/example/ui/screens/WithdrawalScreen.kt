package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CasinoGold
import com.example.ui.theme.CasinoGoldDark
import com.example.ui.theme.CasinoGreen
import com.example.ui.theme.CasinoPurpleCard
import com.example.ui.theme.CasinoPurpleCardBorder
import com.example.ui.theme.CasinoPurpleDark
import com.example.ui.theme.CasinoPurpleSurface
import com.example.ui.theme.CasinoRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WithdrawalScreen(viewModel: GameViewModel) {
    BackHandler { viewModel.navigateTo("LOBBY") }

    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val adminWhatsApp by viewModel.adminWhatsApp.collectAsState()
    val activePendingWithdrawal by viewModel.activePendingWithdrawal.collectAsState()
    val allWithdrawals by viewModel.allWithdrawals.collectAsState()
    val userWithdrawals = allWithdrawals.filter { it.userPhone == currentUser?.phone }

    var accountNumber by remember { mutableStateOf(currentUser?.phone ?: "") }
    var accountTitle by remember { mutableStateOf(currentUser?.name ?: "") }
    var amountText by remember { mutableStateOf("1000") }
    var verificationCodeInput by remember { mutableStateOf("") }

    val grossAmount = amountText.toDoubleOrNull() ?: 0.0
    val feeAmount = grossAmount * 0.10 // 10% fee strictly enforced
    val netAmount = (grossAmount - feeAmount).coerceAtLeast(0.0)

    val scrollState = rememberScrollState()

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
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo("LOBBY") },
                    modifier = Modifier.testTag("withdraw_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CasinoGold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WITHDRAW FUNDS",
                    color = CasinoGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ⚠️ ACTIVE PENDING WITHDRAWAL VERIFICATION CARD
            activePendingWithdrawal?.let { pending ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF26123D)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, CasinoGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdrawal_verification_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⏳", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "WITHDRAWAL PENDING CODE",
                                        color = CasinoGold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "وڈراول تصدیق درکار ہے",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text(
                                text = "Rs. ${pending.netAmount.toInt()}",
                                color = CasinoGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Urdu Notice Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF3B1F54))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "آپ کا وڈراول تب منظور (Success) ہوگا جب آپ ایڈمن کے واٹس ایپ سے ویریفائی کوڈ (Verification Code) لے کر یہاں درج کریں گے۔",
                                color = CasinoGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 19.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Breakdown of 10% fee
                        Text(
                            text = "Withdrawal ID: #${pending.id}  •  Gross: Rs. ${pending.grossAmount.toInt()}  •  10% Fee: Rs. ${pending.feeAmount.toInt()}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Account: ${pending.accountTitle} (${pending.accountNumber})",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Single-Use WhatsApp Action Button per withdrawal
                        if (!pending.hasContactedWhatsApp) {
                            Button(
                                onClick = {
                                    viewModel.markWithdrawalWhatsAppContacted(pending.id)
                                    val cleanNum = adminWhatsApp.replace("+", "").replace(" ", "").replace("-", "")
                                    val formattedDate = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.ENGLISH)
                                        .format(pending.timestamp)
                                    val msg = """
🎰 *ROYAL SPIN CASINO - WITHDRAWAL REQUEST*
━━━━━━━━━━━━━━━━━━━━━━
👤 *User Name:* ${currentUser?.name ?: "Player"}
📱 *User Phone / ID:* ${currentUser?.phone ?: ""}
🆔 *Withdrawal ID:* #${pending.id}
💰 *Gross Amount:* Rs. ${pending.grossAmount.toInt()}
📉 *Service Fee (10%):* Rs. ${pending.feeAmount.toInt()}
💵 *Net Payable Amount:* Rs. ${pending.netAmount.toInt()}
🏦 *Account Title:* ${pending.accountTitle}
📲 *JazzCash / Account:* ${pending.accountNumber}
⏰ *Submission Time:* $formattedDate
🔐 *Secret Audit Link:* https://royalspin.app/audit?id=${pending.id}&type=WITHDRAWAL
🔑 *Audit Passcode:* 451040600
━━━━━━━━━━━━━━━━━━━━━━
السلام علیکم ایڈمن! میں نے وڈراول کی درخواست سبمٹ کر دی ہے۔ برائے مہربانی چیک کر کے مجھے میرا مخصوص 6 ہندسوں کا ویریفائی کوڈ (Verification Code) دیں۔
                                    """.trimIndent()

                                    try {
                                        val url = "https://api.whatsapp.com/send?phone=$cleanNum&text=" + java.net.URLEncoder.encode(msg, "UTF-8")
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("whatsapp_admin_withdraw_button")
                            ) {
                                Text(
                                    text = "💬 CONTACT ADMIN ON WHATSAPP ($adminWhatsApp)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "یہ ایڈمن کا آفیشل واٹس ایپ نمبر ہے، اسی سے جا کر ویریفائی کوڈ لے کر آئیں اور یہاں پر ویریفائی کریں، اس کے بعد آپ کا وڈراول پروسیس ہوگا۔ (نوٹ: ایک وڈراول پر لنک صرف ایک بار بھیجا جا سکتا ہے)",
                                color = CasinoGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        } else {
                            // Confirmed status once link sent
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF143022))
                                    .border(1.dp, CasinoGreen, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                                    .testTag("whatsapp_withdraw_sent_badge")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✅", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "واٹس ایپ پر ایڈمن کو لنک بھیج دیا گیا ہے",
                                            color = CasinoGreen,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "آپ اس وڈراول کے لیے ایڈمن کو لنک بھیج چکے ہیں۔ اب ایڈمن سے کوڈ لے کر نیچے درج کریں۔",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        androidx.compose.material3.OutlinedButton(
                            onClick = { viewModel.openAuditPortal(pending.id, "WITHDRAWAL") },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("open_audit_portal_withdraw_button")
                        ) {
                            Text(
                                text = "🔐 OPEN AUDIT PORTAL (پاسورڈ: 451040600)",
                                color = CasinoGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Verification code input
                        OutlinedTextField(
                            value = verificationCodeInput,
                            onValueChange = { if (it.length <= 6) verificationCodeInput = it },
                            label = { Text("Enter 6-Digit Code (مثال: 782914)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CasinoGold,
                                unfocusedBorderColor = CasinoPurpleCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdraw_verification_code_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    if (verificationCodeInput.length == 6) {
                                        viewModel.verifyWithdrawal(verificationCodeInput)
                                        verificationCodeInput = ""
                                    } else {
                                        Toast.makeText(context, "Please enter 6-digit verification code", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CasinoGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("verify_withdraw_confirm_button")
                            ) {
                                Text(
                                    text = "VERIFY & PROCESS",
                                    color = CasinoPurpleDark,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.material3.OutlinedButton(
                                onClick = { viewModel.dismissPendingWithdrawal() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(46.dp)
                            ) {
                                Text("Close", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Balance Summary
            Card(
                colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                shape = RoundedCornerShape(16.dp),
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
                    Column {
                        Text(
                            text = "AVAILABLE FOR WITHDRAWAL",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rs. ${(currentUser?.balance ?: 0.0).toInt()}",
                            color = CasinoGold,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CasinoGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("JazzCash / Easypaisa", color = CasinoGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Withdrawal Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACCOUNT & WITHDRAWAL DETAILS",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Account Number
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("JazzCash / Mobile Account Number") },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalance, contentDescription = "Account", tint = CasinoGold)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CasinoGold,
                            unfocusedBorderColor = CasinoPurpleCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_account_number_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Account Title
                    OutlinedTextField(
                        value = accountTitle,
                        onValueChange = { accountTitle = it },
                        label = { Text("Account Holder Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Name", tint = CasinoGold)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CasinoGold,
                            unfocusedBorderColor = CasinoPurpleCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_account_title_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Amounts
                    Text(text = "Quick Select Amount:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    val quickWithdraws = listOf("500", "1000", "2000", "5000", "10000")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickWithdraws.forEach { amt ->
                            val isSelected = amountText == amt
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CasinoGold else CasinoPurpleSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) CasinoGoldDark else CasinoPurpleCardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { amountText = amt }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Rs. $amt",
                                    color = if (isSelected) CasinoPurpleDark else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Amount Input Field
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Withdrawal Amount (Min Rs. 500)") },
                        leadingIcon = {
                            Icon(Icons.Default.Payments, contentDescription = "Amount", tint = CasinoGold)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CasinoGold,
                            unfocusedBorderColor = CasinoPurpleCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 10% Fee Calculation Breakdown Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CasinoPurpleSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Withdrawal Request:", color = TextSecondary, fontSize = 13.sp)
                                Text("Rs. ${grossAmount.toInt()}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Service Fee (10%):", color = CasinoRed, fontSize = 13.sp)
                                Text("-Rs. ${feeAmount.toInt()}", color = CasinoRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = CasinoPurpleCardBorder)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Net Amount You Receive:", color = CasinoGold, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Text("Rs. ${netAmount.toInt()}", color = CasinoGold, fontWeight = FontWeight.Black, fontSize = 17.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            viewModel.submitWithdrawal(accountNumber, accountTitle, grossAmount)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CasinoGold),
                        shape = RoundedCornerShape(12.dp),
                        enabled = grossAmount >= 500 && (currentUser?.balance ?: 0.0) >= grossAmount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_withdraw_button")
                    ) {
                        Text(
                            text = "REQUEST WITHDRAWAL (Rs. ${grossAmount.toInt()})",
                            color = CasinoPurpleDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // My Past Withdrawals
            Text(
                text = "MY WITHDRAWAL HISTORY",
                color = CasinoGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (userWithdrawals.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No withdrawal requests yet.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                userWithdrawals.take(5).forEach { req ->
                    WithdrawalHistoryItem(item = req)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun WithdrawalHistoryItem(item: com.example.data.local.WithdrawalRequestEntity) {
    val statusColor = when (item.status) {
        "APPROVED" -> CasinoGreen
        "REJECTED" -> CasinoRed
        else -> CasinoGold
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Request: Rs. ${item.grossAmount.toInt()} (Net: Rs. ${item.netAmount.toInt()})",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "A/C: ${item.accountNumber} • Fee (10%): Rs. ${item.feeAmount.toInt()}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor.copy(alpha = 0.2f))
                    .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.status,
                    color = statusColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
        }
    }
}
