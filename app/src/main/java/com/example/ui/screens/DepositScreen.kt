package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
fun DepositScreen(viewModel: GameViewModel) {
    BackHandler { viewModel.navigateTo("LOBBY") }

    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val jazzCashNumber by viewModel.jazzCashNumber.collectAsState()
    val jazzCashTitle by viewModel.jazzCashTitle.collectAsState()
    val adminWhatsApp by viewModel.adminWhatsApp.collectAsState()
    val activePendingDeposit by viewModel.activePendingDeposit.collectAsState()
    val allDeposits by viewModel.allDeposits.collectAsState()

    val userDeposits = allDeposits.filter { it.userPhone == currentUser?.phone }

    var amountText by remember { mutableStateOf("500") }
    var senderPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var trxId by remember { mutableStateOf("") }
    var verificationCodeInput by remember { mutableStateOf("") }

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
                    modifier = Modifier.testTag("deposit_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CasinoGold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DEPOSIT (JAZZCASH)",
                    color = CasinoGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ⚠️ ACTIVE PENDING DEPOSIT VERIFICATION CARD
            activePendingDeposit?.let { pending ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF26123D)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, CasinoGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_verification_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⏳",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "AWAITING VERIFICATION CODE",
                                        color = CasinoGold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "ڈپازٹ تصدیق درکار ہے",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text(
                                text = "Rs. ${pending.totalCredited.toInt()}",
                                color = CasinoGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Urdu Notice Box as requested
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF3B1F54))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "آپ کا ڈپازٹ تب منظور (Success) ہوگا جب آپ ایڈمن پینل / واٹس ایپ سے ویریفائی کوڈ (Verification Code) لے کر یہاں درج کریں گے۔",
                                color = CasinoGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 19.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Transaction details
                        Text(
                            text = "Deposit ID: #${pending.id}  •  TID: ${pending.transactionId}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        if (pending.isFirstDeposit) {
                            Text(
                                text = "🎁 First Deposit Bonus: +10% (Rs. ${pending.bonusAmount.toInt()}) applied!",
                                color = CasinoGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Single-Use WhatsApp Action Button per deposit
                        if (!pending.hasContactedWhatsApp) {
                            Button(
                                onClick = {
                                    viewModel.markDepositWhatsAppContacted(pending.id)
                                    val cleanNum = adminWhatsApp.replace("+", "").replace(" ", "").replace("-", "")
                                    val formattedDate = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.ENGLISH)
                                        .format(pending.timestamp)
                                    val msg = """
🎰 *ROYAL SPIN CASINO - DEPOSIT VERIFICATION*
━━━━━━━━━━━━━━━━━━━━━━
👤 *User Name:* ${currentUser?.name ?: "Player"}
📱 *User Phone / ID:* ${currentUser?.phone ?: ""}
🆔 *Deposit ID:* #${pending.id}
💰 *Deposit Amount:* Rs. ${pending.amount.toInt()}
🎁 *Bonus Amount:* Rs. ${pending.bonusAmount.toInt()}
💵 *Total to Credit:* Rs. ${pending.totalCredited.toInt()}
📲 *Sender JazzCash:* ${pending.senderPhone}
🧾 *Transaction TID:* ${pending.transactionId}
⭐ *First Deposit:* ${if (pending.isFirstDeposit) "YES (+10% Bonus)" else "NO"}
⏰ *Submission Time:* $formattedDate
🔐 *Secret Audit Link:* https://royalspin.app/audit?id=${pending.id}&type=DEPOSIT
🔑 *Audit Passcode:* 451040600
━━━━━━━━━━━━━━━━━━━━━━
السلام علیکم ایڈمن! میں نے ڈپازٹ کی درخواست سبمٹ کر دی ہے۔ برائے مہربانی چیک کر کے مجھے میرا مخصوص 6 ہندسوں کا ویریفائی کوڈ (Verification Code) دیں۔
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
                                    .testTag("whatsapp_admin_deposit_button")
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
                                text = "یہ ایڈمن کا آفیشل واٹس ایپ نمبر ہے، اسی سے جا کر ویریفائی کوڈ لے کر آئیں اور یہاں پر ویریفائی کریں، اس کے بعد آپ کا ڈپازٹ سکسس ہو گا۔ (نوٹ: ایک ڈپازٹ پر لنک صرف ایک بار بھیجا جا سکتا ہے)",
                                color = CasinoGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        } else {
                            // After clicking once, button disappears and shows confirmed status
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF143022))
                                    .border(1.dp, CasinoGreen, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                                    .testTag("whatsapp_sent_confirmation_badge")
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
                                            text = "آپ اس ڈپازٹ کے لیے ایڈمن کو لنک بھیج چکے ہیں۔ اب ایڈمن سے کوڈ لے کر نیچے درج کریں۔",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // In-App Secret Audit Portal Quick Link
                        androidx.compose.material3.OutlinedButton(
                            onClick = { viewModel.openAuditPortal(pending.id, "DEPOSIT") },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("open_audit_portal_button")
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
                                .testTag("deposit_verification_code_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    if (verificationCodeInput.length == 6) {
                                        viewModel.verifyDeposit(verificationCodeInput)
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
                                    .testTag("verify_deposit_confirm_button")
                            ) {
                                Text(
                                    text = "VERIFY & COMPLETE",
                                    color = CasinoPurpleDark,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.material3.OutlinedButton(
                                onClick = { viewModel.dismissPendingDeposit() },
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

            // JazzCash Account Details Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CasinoGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OFFICIAL JAZZCASH ACCOUNT",
                            color = CasinoGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CasinoGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE", color = CasinoGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = jazzCashNumber,
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Title: $jazzCashTitle",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("JazzCash Number", jazzCashNumber)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied: $jazzCashNumber", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CasinoPurpleSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = CasinoGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Send money from your JazzCash app to this number, then enter TID below.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val hasActivePending = activePendingDeposit != null || userDeposits.any { it.status == "PENDING" }

            if (hasActivePending) {
                // ⚠️ STRICT SINGLE DEPOSIT LOCK CARD: User cannot submit a second deposit while one is pending
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF220F36)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CasinoGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_locked_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔒 نیا ڈپازٹ فارم مقفل ہے", color = CasinoGold, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "آپ کا ایک ڈپازٹ پہلے سے تصدیق (Verification) کے انتظار میں ہے۔ جب تک وہ ڈپازٹ ایڈمن سے کوڈ لے کر ویریفائی اور منظور (Success) نہیں ہوتا، آپ دوسرا ڈپازٹ جمع نہیں کر سکتے۔",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ایک وقت میں صرف ایک ڈپازٹ پراسیس ہوتا ہے۔ براہ کرم اوپر دیے گئے کارڈ میں ایڈمن سے واٹس ایپ پر رابطہ کر کے کوڈ حاصل کریں اور درج کریں۔",
                            color = CasinoGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                // Deposit Form Card (Active only when no pending deposit exists)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoPurpleCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SUBMIT PAYMENT DETAILS",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Amount Chips
                        Text(text = "Quick Select Amount (PKR):", color = TextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        val quickAmounts = listOf("100", "200", "300", "500", "1000")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            quickAmounts.forEach { amt ->
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
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
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

                        // 10% First Deposit Bonus Calculation Card
                        val depositAmt = amountText.toDoubleOrNull() ?: 0.0
                        val isFirstDeposit = currentUser?.isFirstDeposit ?: true
                        if (isFirstDeposit && depositAmt > 0) {
                            val bonusAmt = depositAmt * 0.10
                            val totalWillReceive = depositAmt + bonusAmt
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CasinoPurpleSurface),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "🎁 FIRST DEPOSIT BONUS APPLIED (+10% FREE)",
                                        color = CasinoGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Deposit Rs. ${depositAmt.toInt()} ➔ You Get: Rs. ${totalWillReceive.toInt()} (+Rs. ${bonusAmt.toInt()} Free)",
                                        color = CasinoGreen,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Amount Text Field
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                            label = { Text("Deposit Amount (Rs)") },
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
                                .testTag("deposit_amount_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sender Phone Number
                        OutlinedTextField(
                            value = senderPhone,
                            onValueChange = { senderPhone = it },
                            label = { Text("Sender Mobile Number (Your JazzCash)") },
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
                                .testTag("deposit_sender_phone_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 12-digit TID Field
                        OutlinedTextField(
                            value = trxId,
                            onValueChange = { trxId = it },
                            label = { Text("12-Digit Transaction ID (TID / Trx ID)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CasinoGold,
                                unfocusedBorderColor = CasinoPurpleCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("deposit_tid_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val amt = amountText.toDoubleOrNull() ?: 0.0
                                viewModel.submitDeposit(amt, senderPhone, trxId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CasinoGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_deposit_button")
                        ) {
                            Text(
                                text = "SUBMIT DEPOSIT REQUEST",
                                color = CasinoPurpleDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // My Deposit History
            Text(
                text = "MY RECENT DEPOSITS",
                color = CasinoGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (userDeposits.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CasinoPurpleCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No deposit requests yet.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                userDeposits.take(5).forEach { dep ->
                    DepositHistoryItem(item = dep)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DepositHistoryItem(item: com.example.data.local.DepositRequestEntity) {
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
                    text = "Rs. ${item.amount.toInt()} (TID: ${item.transactionId})",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sender: ${item.senderPhone}",
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
