package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DepositRequestEntity
import com.example.data.local.GameHistoryEntity
import com.example.data.local.GameItemEntity
import com.example.data.local.UserEntity
import com.example.data.local.WithdrawalRequestEntity
import com.example.data.repository.GameRepository
import com.example.model.GamePhase
import com.example.model.LiveTickerItem
import com.example.model.RouletteSlot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    private val vibrator: Vibrator?

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GameRepository(db.appDao())

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    // Current logged in user phone
    private val _currentPhone = MutableStateFlow<String?>(null)
    val currentPhone = _currentPhone.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    // Screen navigation state: "AUTH", "LOBBY", "LOADING", "GAME", "DEPOSIT", "WITHDRAWAL", "HISTORY", "ADMIN"
    private val _currentScreen = MutableStateFlow("AUTH")
    val currentScreen = _currentScreen.asStateFlow()

    // Loading progress and game title for game launcher
    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress = _loadingProgress.asStateFlow()

    private val _loadingGameTitle = MutableStateFlow("ZOO & CAR ROULETTE")
    val loadingGameTitle = _loadingGameTitle.asStateFlow()

    private val _loadingGameEmoji = MutableStateFlow("🏎️")
    val loadingGameEmoji = _loadingGameEmoji.asStateFlow()

    // Game loop states
    private val _gamePhase = MutableStateFlow(GamePhase.BETTING)
    val gamePhase = _gamePhase.asStateFlow()

    private val _countdown = MutableStateFlow(15)
    val countdown = _countdown.asStateFlow()

    private val _highlightIndex = MutableStateFlow(0)
    val highlightIndex = _highlightIndex.asStateFlow()

    private val _selectedChip = MutableStateFlow(100)
    val selectedChip = _selectedChip.asStateFlow()

    // Map of Slot -> Bet Amount placed by user in this round
    private val _userBets = MutableStateFlow<Map<RouletteSlot, Int>>(emptyMap())
    val userBets = _userBets.asStateFlow()

    // Total bot bets on each slot (for realistic multiplayer crowd vibe)
    private val _slotTotalBets = MutableStateFlow<Map<RouletteSlot, Int>>(emptyMap())
    val slotTotalBets = _slotTotalBets.asStateFlow()

    private val _winningSlot = MutableStateFlow<RouletteSlot?>(null)
    val winningSlot = _winningSlot.asStateFlow()

    private val _lastRoundWin = MutableStateFlow(0.0)
    val lastRoundWin = _lastRoundWin.asStateFlow()

    private val _lastRoundGrossWin = MutableStateFlow(0.0)
    val lastRoundGrossWin = _lastRoundGrossWin.asStateFlow()

    private val _lastRoundWinFee = MutableStateFlow(0.0)
    val lastRoundWinFee = _lastRoundWinFee.asStateFlow()

    // Live Tickers
    private val _liveTickers = MutableStateFlow<List<LiveTickerItem>>(emptyList())
    val liveTickers = _liveTickers.asStateFlow()

    // JazzCash settings for deposits
    private val _jazzCashNumber = MutableStateFlow("03227422095")
    val jazzCashNumber = _jazzCashNumber.asStateFlow()

    private val _jazzCashTitle = MutableStateFlow("JazzCash Official Cashier")
    val jazzCashTitle = _jazzCashTitle.asStateFlow()

    // WhatsApp setting for customer support & verification code
    private val _adminWhatsApp = MutableStateFlow("+639525381307")
    val adminWhatsApp = _adminWhatsApp.asStateFlow()

    // Admin Secret Passcode for Vault and Audit Link
    private val _adminSecretPasscode = MutableStateFlow("451040600")
    val adminSecretPasscode = _adminSecretPasscode.asStateFlow()

    // Audit Portal targeted record state
    private val _auditRecordId = MutableStateFlow<Long?>(null)
    val auditRecordId = _auditRecordId.asStateFlow()

    private val _auditRecordType = MutableStateFlow("DEPOSIT") // DEPOSIT or WITHDRAWAL
    val auditRecordType = _auditRecordType.asStateFlow()

    // Active pending transactions awaiting verification code
    private val _activePendingDeposit = MutableStateFlow<DepositRequestEntity?>(null)
    val activePendingDeposit = _activePendingDeposit.asStateFlow()

    private val _activePendingWithdrawal = MutableStateFlow<WithdrawalRequestEntity?>(null)
    val activePendingWithdrawal = _activePendingWithdrawal.asStateFlow()

    // Admin Forced Next Slot
    private val _forcedWinner = MutableStateFlow("AUTO")
    val forcedWinner = _forcedWinner.asStateFlow()

    // Low winning mode setting
    private val _lowWinningMode = MutableStateFlow(true)
    val lowWinningMode = _lowWinningMode.asStateFlow()

    // UI Snackbars / Alerts
    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    // Lists for Admin & User
    val allDeposits: StateFlow<List<DepositRequestEntity>> = repository.getAllDeposits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalRequestEntity>> = repository.getAllWithdrawals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gameHistory: StateFlow<List<GameHistoryEntity>> = repository.getRecentHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGames: StateFlow<List<GameItemEntity>> = repository.getAllGames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var gameLoopJob: Job? = null
    private var botBettingJob: Job? = null

    init {
        // Load initial settings
        viewModelScope.launch {
            repository.getSetting("jazzcash_number").collect { entity ->
                entity?.configValue?.let { _jazzCashNumber.value = it }
            }
        }
        viewModelScope.launch {
            repository.getSetting("jazzcash_title").collect { entity ->
                entity?.configValue?.let { _jazzCashTitle.value = it }
            }
        }
        viewModelScope.launch {
            repository.getSetting("admin_whatsapp").collect { entity ->
                entity?.configValue?.let { _adminWhatsApp.value = it }
            }
        }
        viewModelScope.launch {
            repository.getSetting("admin_secret_pass").collect { entity ->
                entity?.configValue?.let { _adminSecretPasscode.value = it }
            }
        }
        viewModelScope.launch {
            repository.getSetting("force_next_result").collect { entity ->
                entity?.configValue?.let { _forcedWinner.value = it }
            }
        }
        viewModelScope.launch {
            repository.getSetting("low_winning_rate").collect { entity ->
                entity?.configValue?.let { _lowWinningMode.value = it.toBoolean() }
            }
        }

        // Initialize sample live tickers
        generateInitialTickers()

        // Start game loop
        startGameLoop()
    }

    private fun generateInitialTickers() {
        val initial = listOf(
            LiveTickerItem("1", "0301***912", "WITHDRAWAL", 4500.0, "JazzCash Approved"),
            LiveTickerItem("2", "0345***438", "DEPOSIT", 2000.0, "JazzCash (+10% Bonus credited)"),
            LiveTickerItem("3", "0312***881", "BIG_WIN", 13500.0, "Won 15x Dragon (-10% Fee)!"),
            LiveTickerItem("4", "0321***104", "WITHDRAWAL", 9000.0, "JazzCash Approved"),
            LiveTickerItem("5", "0333***720", "BIG_WIN", 22500.0, "Won 25x Shark (-10% Fee)!")
        )
        _liveTickers.value = initial
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // Launch any game with animated loading screen and custom title
    fun launchGame(targetScreen: String, gameTitle: String, gameEmoji: String) {
        viewModelScope.launch {
            _loadingGameTitle.value = gameTitle
            _loadingGameEmoji.value = gameEmoji
            _currentScreen.value = "LOADING"
            _loadingProgress.value = 0f
            for (step in 1..20) {
                delay(60)
                _loadingProgress.value = step / 20f
            }
            delay(150)
            _currentScreen.value = targetScreen
        }
    }

    fun launchRouletteGame() {
        launchGame("GAME", "ZOO & CAR ROULETTE", "🏎️")
    }

    // Direct instant deduction for any mini-game (Dragon Tiger, 7 Up Down, Mines)
    fun deductUserBalance(amount: Double): Boolean {
        val user = _currentUser.value ?: return false
        if (user.balance < amount) return false
        val updated = user.copy(
            balance = (user.balance - amount).coerceAtLeast(0.0),
            totalBets = user.totalBets + amount
        )
        _currentUser.value = updated
        viewModelScope.launch {
            repository.updateUser(updated)
        }
        return true
    }

    // Direct instant winning credit with 10% fee already accounted for
    fun creditUserWinning(netWin: Double, grossWin: Double, gameName: String) {
        val user = _currentUser.value ?: return
        val updated = user.copy(
            balance = user.balance + netWin,
            totalWins = user.totalWins + netWin
        )
        _currentUser.value = updated
        viewModelScope.launch {
            repository.updateUser(updated)
            if (netWin >= 300) {
                val masked = if (user.phone.length >= 7) user.phone.take(4) + "***" + user.phone.takeLast(3) else user.phone
                val ticker = LiveTickerItem(
                    id = System.currentTimeMillis().toString(),
                    phoneMasked = masked,
                    actionType = "BIG_WIN",
                    amount = netWin,
                    note = "Won on $gameName (-10% fee deducted)!"
                )
                _liveTickers.value = (listOf(ticker) + _liveTickers.value).take(10)
            }
        }
    }

    fun selectChip(amount: Int) {
        _selectedChip.value = amount
        vibrate(30)
    }

    // Auth actions
    fun login(phone: String, pass: String) {
        viewModelScope.launch {
            val result = repository.loginUser(phone, pass)
            result.onSuccess { user ->
                _currentPhone.value = user.phone
                _currentUser.value = user
                _currentScreen.value = "LOBBY"
                _uiMessage.emit("Welcome, ${user.name}!")
                listenToUser(user.phone)
            }.onFailure { err ->
                _uiMessage.emit(err.message ?: "Login failed")
            }
        }
    }

    fun register(phone: String, name: String, pass: String) {
        viewModelScope.launch {
            val deviceId = getDeviceId()
            val result = repository.registerUser(phone, name, pass, deviceId)
            result.onSuccess { user ->
                _currentPhone.value = user.phone
                _currentUser.value = user
                _currentScreen.value = "LOBBY"
                _uiMessage.emit("Registration successful! 10% Extra Bonus on your First Deposit!")
                listenToUser(user.phone)
            }.onFailure { err ->
                _uiMessage.emit(err.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        _currentPhone.value = null
        _currentUser.value = null
        _userBets.value = emptyMap()
        _currentScreen.value = "AUTH"
    }

    private fun listenToUser(phone: String) {
        viewModelScope.launch {
            repository.getUser(phone).collect { user ->
                _currentUser.value = user
            }
        }
        viewModelScope.launch {
            repository.getPendingDepositByUser(phone).collect { pendingDep ->
                _activePendingDeposit.value = pendingDep
            }
        }
        viewModelScope.launch {
            repository.getPendingWithdrawalByUser(phone).collect { pendingWth ->
                _activePendingWithdrawal.value = pendingWth
            }
        }
    }

    private fun getDeviceId(): String {
        val prefs = getApplication<Application>().getSharedPreferences("app_device", Context.MODE_PRIVATE)
        var id = prefs.getString("device_unique_id", null)
        if (id == null) {
            id = "DEV_" + System.currentTimeMillis().toString().takeLast(6) + "_" + Random.nextInt(100, 999)
            prefs.edit().putString("device_unique_id", id).apply()
        }
        return id
    }

    // Betting actions:
    // 1. Max 4 slots allowed per round
    // 2. Instant money deduction from balance
    fun placeBet(slot: RouletteSlot) {
        if (_gamePhase.value != GamePhase.BETTING) {
            viewModelScope.launch { _uiMessage.emit("Betting closed! Please wait for next round.") }
            return
        }

        val user = _currentUser.value ?: return
        val chip = _selectedChip.value

        // Check max 4 slots rule:
        val currentBetSlots = _userBets.value.keys
        if (!currentBetSlots.contains(slot) && currentBetSlots.size >= 4) {
            viewModelScope.launch {
                _uiMessage.emit("⚠️ Maximum 4 items allowed per round! You cannot bet on more than 4 items.")
            }
            vibrate(100)
            return
        }

        // Check balance
        if (user.balance < chip) {
            viewModelScope.launch { _uiMessage.emit("Insufficient balance! Please deposit to continue.") }
            return
        }

        vibrate(40)

        // Instant money deduction right away!
        viewModelScope.launch {
            val updatedUser = user.copy(
                balance = (user.balance - chip).coerceAtLeast(0.0),
                totalBets = user.totalBets + chip
            )
            repository.updateUser(updatedUser)
        }

        val currentSlotBet = _userBets.value[slot] ?: 0
        _userBets.value = _userBets.value + (slot to (currentSlotBet + chip))

        // Update overall total bets on that slot for display
        val currentSlotTotal = _slotTotalBets.value[slot] ?: 0
        _slotTotalBets.value = _slotTotalBets.value + (slot to (currentSlotTotal + chip))
    }

    fun clearBets() {
        if (_gamePhase.value != GamePhase.BETTING) return
        val user = _currentUser.value ?: return
        val currentPlacedTotal = _userBets.value.values.sum()

        if (currentPlacedTotal > 0) {
            // Refund the instantly deducted money back to user balance!
            viewModelScope.launch {
                val updatedUser = user.copy(
                    balance = user.balance + currentPlacedTotal,
                    totalBets = (user.totalBets - currentPlacedTotal).coerceAtLeast(0.0)
                )
                repository.updateUser(updatedUser)
            }
        }

        _userBets.value = emptyMap()
        vibrate(30)
    }

    fun doubleBets() {
        if (_gamePhase.value != GamePhase.BETTING) return
        val user = _currentUser.value ?: return
        val currentMap = _userBets.value
        if (currentMap.isEmpty()) return

        val totalCurrentBets = currentMap.values.sum()
        if (user.balance < totalCurrentBets) {
            viewModelScope.launch { _uiMessage.emit("Insufficient balance to double bets!") }
            return
        }

        vibrate(50)

        // Instant deduction for doubling
        viewModelScope.launch {
            val updatedUser = user.copy(
                balance = (user.balance - totalCurrentBets).coerceAtLeast(0.0),
                totalBets = user.totalBets + totalCurrentBets
            )
            repository.updateUser(updatedUser)
        }

        _userBets.value = currentMap.mapValues { it.value * 2 }
    }

    // Deposit submission
    fun submitDeposit(amount: Double, senderPhone: String, tid: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.submitDeposit(user.phone, amount, senderPhone, tid)
            res.onSuccess { req ->
                _activePendingDeposit.value = req
                _uiMessage.emit("ڈپازٹ کی درخواست درج ہو گئی! ایڈمن سے واٹس ایپ پر ویریفائی کوڈ حاصل کریں۔")
            }.onFailure {
                _uiMessage.emit(it.message ?: "ڈپازٹ جمع کرانے میں ناکامی")
            }
        }
    }

    // Verify Deposit with 6-digit code
    fun verifyDeposit(code: String) {
        val pending = _activePendingDeposit.value ?: return
        viewModelScope.launch {
            val res = repository.verifyDepositCode(pending.id, code)
            res.onSuccess {
                _activePendingDeposit.value = null
                _uiMessage.emit("ڈپازٹ کامیابی سے منظور ہو گیا! بیلنس اور 10% بونس اکاؤنٹ میں شامل کر دیا گیا۔")
            }.onFailure {
                _uiMessage.emit(it.message ?: "غلط کوڈ! براہ کرم ایڈمن سے درست کوڈ لیں۔")
            }
        }
    }

    fun dismissPendingDeposit() {
        _activePendingDeposit.value = null
    }

    // Withdrawal submission (10% fee strictly calculated)
    fun submitWithdrawal(accountNumber: String, accountTitle: String, grossAmount: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.submitWithdrawal(user.phone, accountNumber, accountTitle, grossAmount)
            res.onSuccess { req ->
                _activePendingWithdrawal.value = req
                val fee = grossAmount * 0.10
                val net = grossAmount - fee
                _uiMessage.emit("وڈراول درخواست درج! (10% فیس: Rs. ${fee.toInt()}, قابل ادا: Rs. ${net.toInt()}) - ایڈمن سے کوڈ لیں۔")
            }.onFailure {
                _uiMessage.emit(it.message ?: "Withdrawal failed")
            }
        }
    }

    // Verify Withdrawal with 6-digit code
    fun verifyWithdrawal(code: String) {
        val pending = _activePendingWithdrawal.value ?: return
        viewModelScope.launch {
            val res = repository.verifyWithdrawalCode(pending.id, code)
            res.onSuccess {
                _activePendingWithdrawal.value = null
                _uiMessage.emit("وڈراول کامیابی سے تصدیق اور منظور ہو گیا!")
            }.onFailure {
                _uiMessage.emit(it.message ?: "غلط کوڈ! براہ کرم ایڈمن سے درست کوڈ لیں۔")
            }
        }
    }

    fun dismissPendingWithdrawal() {
        _activePendingWithdrawal.value = null
    }

    fun markDepositWhatsAppContacted(depositId: Long) {
        viewModelScope.launch {
            repository.markDepositWhatsAppContacted(depositId)
            // Refresh active pending deposit state
            _activePendingDeposit.value = _activePendingDeposit.value?.copy(hasContactedWhatsApp = true)
        }
    }

    fun markWithdrawalWhatsAppContacted(withdrawalId: Long) {
        viewModelScope.launch {
            repository.markWithdrawalWhatsAppContacted(withdrawalId)
            _activePendingWithdrawal.value = _activePendingWithdrawal.value?.copy(hasContactedWhatsApp = true)
        }
    }

    fun openAuditPortal(recordId: Long, recordType: String = "DEPOSIT") {
        _auditRecordId.value = recordId
        _auditRecordType.value = recordType
        _currentScreen.value = "AUDIT_PORTAL"
    }

    suspend fun fetchDepositSync(id: Long): DepositRequestEntity? {
        return repository.getDepositById(id)
    }

    suspend fun fetchWithdrawalSync(id: Long): WithdrawalRequestEntity? {
        return repository.getWithdrawalById(id)
    }

    // Admin Panel Dedicated Code Generating System
    fun generateBoundDepositCode(depositId: Long) {
        viewModelScope.launch {
            repository.generateBoundDepositCode(depositId).onSuccess { newCode ->
                _uiMessage.emit("⚡ یوزر اور رقم کے ساتھ منسلک مخصوص ویریفائی کوڈ جنریٹ ہو گیا: $newCode")
            }.onFailure {
                _uiMessage.emit(it.message ?: "کوڈ جنریشن ناکام ہو گئی")
            }
        }
    }

    fun generateBoundWithdrawalCode(withdrawalId: Long) {
        viewModelScope.launch {
            repository.generateBoundWithdrawalCode(withdrawalId).onSuccess { newCode ->
                _uiMessage.emit("⚡ وڈراول کے لیے مخصوص ویریفائی کوڈ جنریٹ ہو گیا: $newCode")
            }.onFailure {
                _uiMessage.emit(it.message ?: "کوڈ جنریشن ناکام ہو گئی")
            }
        }
    }

    // Admin Panel Actions
    fun regenerateDepositCode(depositId: Long) {
        generateBoundDepositCode(depositId)
    }

    fun regenerateWithdrawalCode(withdrawalId: Long) {
        generateBoundWithdrawalCode(withdrawalId)
    }

    fun approveDeposit(depositId: Long) {
        viewModelScope.launch {
            repository.approveDeposit(depositId).onSuccess {
                _uiMessage.emit("Deposit approved and credited (with 10% First Deposit Bonus if applicable)!")
            }.onFailure {
                _uiMessage.emit(it.message ?: "Action failed")
            }
        }
    }

    fun rejectDeposit(depositId: Long) {
        viewModelScope.launch {
            repository.rejectDeposit(depositId).onSuccess {
                _uiMessage.emit("Deposit request rejected.")
            }
        }
    }

    fun approveWithdrawal(withdrawalId: Long) {
        viewModelScope.launch {
            repository.approveWithdrawal(withdrawalId).onSuccess {
                _uiMessage.emit("Withdrawal marked as approved and transferred!")
            }.onFailure {
                _uiMessage.emit(it.message ?: "Action failed")
            }
        }
    }

    fun rejectWithdrawal(withdrawalId: Long) {
        viewModelScope.launch {
            repository.rejectWithdrawal(withdrawalId).onSuccess {
                _uiMessage.emit("Withdrawal rejected and refunded to player balance.")
            }
        }
    }

    fun updateJazzCashDetails(number: String, title: String) {
        viewModelScope.launch {
            repository.updateSetting("jazzcash_number", number)
            repository.updateSetting("jazzcash_title", title)
            _jazzCashNumber.value = number
            _jazzCashTitle.value = title
            _uiMessage.emit("JazzCash account details updated across all users!")
        }
    }

    fun updateAdminWhatsApp(number: String) {
        viewModelScope.launch {
            repository.updateSetting("admin_whatsapp", number)
            _adminWhatsApp.value = number
            _uiMessage.emit("Admin WhatsApp number updated: $number")
        }
    }

    fun updateAdminSecretPasscode(passcode: String) {
        viewModelScope.launch {
            repository.updateSetting("admin_secret_pass", passcode)
            _adminSecretPasscode.value = passcode
            _uiMessage.emit("Admin Secret Vault passcode updated!")
        }
    }

    fun setForcedNextWinner(slotId: String) {
        viewModelScope.launch {
            repository.updateSetting("force_next_result", slotId)
            _forcedWinner.value = slotId
            _uiMessage.emit("Next spin forced to: $slotId")
        }
    }

    fun toggleLowWinningMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSetting("low_winning_rate", enabled.toString())
            _lowWinningMode.value = enabled
            _uiMessage.emit("House Winning Control set to: ${if (enabled) "Strict Low Payout" else "Standard"}")
        }
    }

    fun addNewGame(title: String, subtitle: String, iconEmoji: String, category: String, minBet: Int) {
        viewModelScope.launch {
            repository.addNewGame(title, subtitle, iconEmoji, category, minBet)
            _uiMessage.emit("New game uploaded successfully!")
        }
    }

    // Game Running Engine (Roulette Spin Loop)
    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (true) {
                // 1. BETTING PHASE (15 seconds)
                _gamePhase.value = GamePhase.BETTING
                _winningSlot.value = null
                _lastRoundWin.value = 0.0
                _lastRoundGrossWin.value = 0.0
                _lastRoundWinFee.value = 0.0
                _slotTotalBets.value = emptyMap()

                // Spawn bots placing realistic live bets
                startBotBetting()

                for (sec in 15 downTo 1) {
                    _countdown.value = sec
                    delay(1000)
                }
                _countdown.value = 0
                botBettingJob?.cancel()

                // 2. SPINNING PHASE (Wheel Highlighting Animation)
                _gamePhase.value = GamePhase.SPINNING
                val winnerSlot = determineWinnerSlot()
                val targetIndex = RouletteSlot.WHEEL_SEQUENCE.indexOf(winnerSlot)

                val fullCycles = 3
                val totalSteps = (fullCycles * RouletteSlot.WHEEL_SEQUENCE.size) + targetIndex - _highlightIndex.value + RouletteSlot.WHEEL_SEQUENCE.size

                var currentIndex = _highlightIndex.value
                for (step in 0..totalSteps) {
                    currentIndex = (currentIndex + 1) % RouletteSlot.WHEEL_SEQUENCE.size
                    _highlightIndex.value = currentIndex
                    vibrate(15)

                    // Deceleration curve for dramatic casino suspense
                    val progress = step.toFloat() / totalSteps.toFloat()
                    val sleepMs = when {
                        progress < 0.6f -> 50L
                        progress < 0.8f -> 90L
                        progress < 0.9f -> 160L
                        progress < 0.96f -> 260L
                        else -> 420L
                    }
                    delay(sleepMs)
                }

                // Ensure exact landing on target
                _highlightIndex.value = targetIndex
                _winningSlot.value = winnerSlot
                vibrate(200)

                // 3. RESULT PHASE (Payout and Celebration)
                _gamePhase.value = GamePhase.RESULT
                val userBetOnWinner = _userBets.value[winnerSlot] ?: 0
                var roundGrossWin = 0.0
                var roundWinFee = 0.0
                var roundNetWin = 0.0

                if (winnerSlot == RouletteSlot.ZERO) {
                    // ZERO: House wipeout! All animal/car bets lose.
                    roundGrossWin = 0.0
                    roundNetWin = 0.0
                } else if (userBetOnWinner > 0) {
                    // 10% Winning Commission Fee strictly deducted as requested!
                    roundGrossWin = userBetOnWinner.toDouble() * winnerSlot.multiplier
                    roundWinFee = roundGrossWin * 0.10 // 10% fee
                    roundNetWin = roundGrossWin - roundWinFee
                }

                _lastRoundGrossWin.value = roundGrossWin
                _lastRoundWinFee.value = roundWinFee
                _lastRoundWin.value = roundNetWin

                if (roundNetWin > 0 && _currentUser.value != null) {
                    val user = _currentUser.value!!
                    val updatedUser = user.copy(
                        balance = user.balance + roundNetWin,
                        totalWins = user.totalWins + roundNetWin
                    )
                    repository.updateUser(updatedUser)
                    vibratePattern()

                    // Add to live ticker if win
                    if (roundNetWin >= 500) {
                        val masked = user.phone.take(4) + "***" + user.phone.takeLast(3)
                        val ticker = LiveTickerItem(
                            id = System.currentTimeMillis().toString(),
                            phoneMasked = masked,
                            actionType = "BIG_WIN",
                            amount = roundNetWin,
                            note = "Won ${winnerSlot.multiplier}x on ${winnerSlot.title}!"
                        )
                        _liveTickers.value = (listOf(ticker) + _liveTickers.value).take(10)
                    }
                }

                // Record history
                val totalPlacedBet = _userBets.value.values.sum().toDouble()
                repository.recordGameRound(
                    winner = winnerSlot,
                    totalBets = totalPlacedBet,
                    totalPayouts = roundNetWin,
                    wasZero = winnerSlot == RouletteSlot.ZERO
                )

                delay(4500)
                _userBets.value = emptyMap()
            }
        }
    }

    private suspend fun determineWinnerSlot(): RouletteSlot {
        val forced = repository.getSettingSync("force_next_result")
        if (forced != null && forced != "AUTO") {
            // Reset to AUTO once consumed
            repository.updateSetting("force_next_result", "AUTO")
            _forcedWinner.value = "AUTO"
            return RouletteSlot.fromId(forced)
        }

        // Controlled House-Edge / Low Payout algorithm:
        // As requested by user: keep total winning low, favour 0 (house wipeout) or low multipliers (3x Dog, 3x Mercedes)
        val roll = Random.nextInt(100)
        return if (_lowWinningMode.value) {
            when {
                roll < 18 -> RouletteSlot.ZERO        // 18% Zero wipeout
                roll < 36 -> RouletteSlot.DOG         // 18% Dog 3x
                roll < 54 -> RouletteSlot.MERCEDES    // 18% Mercedes 3x
                roll < 70 -> RouletteSlot.BIKE125     // 16% Bike 4x
                roll < 84 -> RouletteSlot.HORSE       // 14% Horse 5x
                roll < 91 -> RouletteSlot.CENTER      // 7% Center Panther 10x
                roll < 96 -> RouletteSlot.DRAGON      // 5% Dragon 15x
                roll < 98 -> RouletteSlot.ELEPHANT    // 2% Elephant 20x
                else -> RouletteSlot.SHARK           // 2% Shark 25x
            }
        } else {
            when {
                roll < 10 -> RouletteSlot.ZERO
                roll < 22 -> RouletteSlot.DOG
                roll < 34 -> RouletteSlot.MERCEDES
                roll < 46 -> RouletteSlot.BIKE125
                roll < 58 -> RouletteSlot.HORSE
                roll < 70 -> RouletteSlot.CENTER
                roll < 82 -> RouletteSlot.DRAGON
                roll < 92 -> RouletteSlot.ELEPHANT
                else -> RouletteSlot.SHARK
            }
        }
    }

    private fun startBotBetting() {
        botBettingJob?.cancel()
        botBettingJob = viewModelScope.launch {
            val chips = listOf(50, 100, 200, 300, 500)
            while (_gamePhase.value == GamePhase.BETTING) {
                delay(Random.nextLong(600, 1500))
                val randomSlot = RouletteSlot.entries.random()
                if (randomSlot != RouletteSlot.ZERO) {
                    val addChip = chips.random()
                    val current = _slotTotalBets.value[randomSlot] ?: 0
                    _slotTotalBets.value = _slotTotalBets.value + (randomSlot to (current + addChip))
                }
            }
        }
    }

    private fun vibrate(ms: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(ms)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 80, 150, 80, 250), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300)
            }
        } catch (_: Exception) {}
    }
}
