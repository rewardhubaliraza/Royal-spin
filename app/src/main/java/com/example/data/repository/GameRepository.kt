package com.example.data.repository

import com.example.data.local.AppDao
import com.example.data.local.AppSettingsEntity
import com.example.data.local.DepositRequestEntity
import com.example.data.local.GameHistoryEntity
import com.example.data.local.GameItemEntity
import com.example.data.local.UserEntity
import com.example.data.local.WithdrawalRequestEntity
import com.example.model.RouletteSlot
import kotlinx.coroutines.flow.Flow

class GameRepository(private val dao: AppDao) {

    // User operations
    fun getUser(phone: String): Flow<UserEntity?> = dao.getUser(phone)
    suspend fun getUserSync(phone: String): UserEntity? = dao.getUserSync(phone)
    fun getAllUsers(): Flow<List<UserEntity>> = dao.getAllUsers()

    suspend fun registerUser(
        phone: String,
        name: String,
        pass: String,
        deviceId: String
    ): Result<UserEntity> {
        val cleanPhone = phone.trim()
        if (cleanPhone.length < 10) {
            return Result.failure(Exception("Please enter a valid phone number (at least 10 digits)"))
        }
        val existingPhoneCount = dao.countUserByPhone(cleanPhone)
        if (existingPhoneCount > 0) {
            return Result.failure(Exception("This phone number is already registered. Please log in!"))
        }

        val existingDeviceCount = dao.countUsersByDeviceId(deviceId)
        val isAdmin = cleanPhone == "03073715192" || cleanPhone.endsWith("9525381307") || cleanPhone == "+639525381307"
        if (existingDeviceCount >= 2 && !isAdmin) {
            return Result.failure(Exception("Maximum limit of 2 accounts reached for this device."))
        }

        val newUser = UserEntity(
            phone = cleanPhone,
            name = name.ifBlank { if (isAdmin) "Master Admin" else "Player_${cleanPhone.takeLast(4)}" },
            password = pass,
            balance = if (isAdmin) 50000.0 else 0.0,
            isFirstDeposit = true,
            deviceId = deviceId,
            isAdmin = isAdmin
        )
        dao.insertUser(newUser)
        return Result.success(newUser)
    }

    suspend fun loginUser(phone: String, pass: String): Result<UserEntity> {
        val cleanPhone = phone.trim()
        val user = dao.getUserSync(cleanPhone)
            ?: return Result.failure(Exception("No account found with this phone. Please register first."))

        if (user.isBanned) {
            return Result.failure(Exception("This account is suspended. Contact Admin."))
        }

        if (user.password != pass) {
            return Result.failure(Exception("Invalid password. Please check and try again."))
        }

        return Result.success(user)
    }

    suspend fun updateUser(user: UserEntity) {
        dao.updateUser(user)
    }

    // Deposits
    // Deposits
    fun getAllDeposits(): Flow<List<DepositRequestEntity>> = dao.getAllDeposits()
    fun getUserDeposits(phone: String): Flow<List<DepositRequestEntity>> = dao.getDepositsByUser(phone)
    fun getPendingDepositByUser(phone: String): Flow<DepositRequestEntity?> = dao.getPendingDepositByUser(phone)
    suspend fun getDepositById(id: Long): DepositRequestEntity? = dao.getDepositById(id)

    // Deterministic Bound Verification Code Generator:
    // Strictly binds the 6-digit code to user phone, exact amount, and record ID
    fun computeBoundCode(phone: String, amount: Double, saltId: Long): String {
        val cleanDigits = phone.filter { it.isDigit() }
        val phoneSeed = cleanDigits.takeLast(6).toLongOrNull() ?: 123456L
        val amtSeed = (amount * 100).toLong()
        val salt = saltId * 7919L + 451040L
        val hash = kotlin.math.abs((phoneSeed * 31L + amtSeed * 97L + salt) % 900000L) + 100000L
        return hash.toString()
    }

    suspend fun submitDeposit(
        userPhone: String,
        amount: Double,
        senderPhone: String,
        trxId: String
    ): Result<DepositRequestEntity> {
        if (amount < 100) {
            return Result.failure(Exception("کم از کم ڈپازٹ Rs. 100 ہے"))
        }
        if (trxId.isBlank()) {
            return Result.failure(Exception("براہ کرم درست JazzCash TID درج کریں"))
        }

        // ⚠️ STRICT CONSTRAINT: Only 1 deposit can be pending at a time!
        val existingPending = dao.getPendingDepositByUserSync(userPhone)
        if (existingPending != null) {
            return Result.failure(
                Exception("آپ کا ایک ڈپازٹ (ID: #${existingPending.id}) پہلے سے تصدیق کے انتظار میں ہے۔ نیا ڈپازٹ جمع کرانے سے پہلے اسے ایڈمن سے کوڈ لے کر ویریفائی کریں!")
            )
        }

        val user = dao.getUserSync(userPhone)
        val isFirst = user?.isFirstDeposit ?: true
        val bonus = if (isFirst) amount * 0.10 else 0.0 // 10% first deposit bonus
        val totalToCredit = amount + bonus

        val now = System.currentTimeMillis()
        val initialBoundCode = computeBoundCode(userPhone, amount, now)

        val req = DepositRequestEntity(
            userPhone = userPhone,
            userName = user?.name ?: "Player_${userPhone.takeLast(4)}",
            amount = amount,
            bonusAmount = bonus,
            totalCredited = totalToCredit,
            senderPhone = senderPhone.ifBlank { userPhone },
            transactionId = trxId.trim(),
            verificationCode = initialBoundCode,
            isFirstDeposit = isFirst,
            hasContactedWhatsApp = false,
            status = "PENDING",
            timestamp = now
        )
        val id = dao.insertDeposit(req)
        return Result.success(req.copy(id = id))
    }

    suspend fun markDepositWhatsAppContacted(depositId: Long): Result<Unit> {
        val deposit = dao.getDepositById(depositId) ?: return Result.failure(Exception("Deposit not found"))
        dao.updateDeposit(deposit.copy(hasContactedWhatsApp = true))
        return Result.success(Unit)
    }

    // Admin Dedicated Code Generating System (bound strictly to this user and amount)
    suspend fun generateBoundDepositCode(depositId: Long): Result<String> {
        val deposit = dao.getDepositById(depositId) ?: return Result.failure(Exception("ڈپازٹ ریکارڈ نہیں ملا"))
        val boundCode = computeBoundCode(deposit.userPhone, deposit.amount, deposit.id)
        dao.updateDeposit(deposit.copy(verificationCode = boundCode))
        return Result.success(boundCode)
    }

    suspend fun verifyDepositCode(depositId: Long, enteredCode: String): Result<Unit> {
        val deposit = dao.getDepositById(depositId) ?: return Result.failure(Exception("ڈپازٹ ریکارڈ نہیں ملا"))
        if (deposit.status != "PENDING") {
            return Result.failure(Exception("یہ ڈپازٹ پہلے ہی پراسیس ہو چکا ہے"))
        }
        if (deposit.verificationCode.trim() != enteredCode.trim()) {
            return Result.failure(Exception("غلط ویریفائی کوڈ! ایڈمن سے درست کوڈ حاصل کریں جو آپ کے فون نمبر (${deposit.userPhone}) اور رقم (Rs. ${deposit.amount.toInt()}) سے منسلک ہے۔"))
        }

        // Successfully verified by user with code
        dao.updateDeposit(deposit.copy(status = "APPROVED", isVerified = true))

        val user = dao.getUserSync(deposit.userPhone)
        if (user != null) {
            dao.updateUser(
                user.copy(
                    balance = user.balance + deposit.totalCredited,
                    totalDeposits = user.totalDeposits + deposit.amount,
                    isFirstDeposit = false
                )
            )
        }
        return Result.success(Unit)
    }

    suspend fun regenerateDepositCode(depositId: Long): Result<String> {
        return generateBoundDepositCode(depositId)
    }

    suspend fun approveDeposit(depositId: Long): Result<Unit> {
        val deposit = dao.getDepositById(depositId) ?: return Result.failure(Exception("Deposit not found"))
        if (deposit.status != "PENDING") {
            return Result.failure(Exception("Request already processed"))
        }

        dao.updateDeposit(deposit.copy(status = "APPROVED", isVerified = true))

        val user = dao.getUserSync(deposit.userPhone)
        if (user != null) {
            dao.updateUser(
                user.copy(
                    balance = user.balance + deposit.totalCredited,
                    totalDeposits = user.totalDeposits + deposit.amount,
                    isFirstDeposit = false
                )
            )
        }
        return Result.success(Unit)
    }

    suspend fun rejectDeposit(depositId: Long, reason: String = "Rejected by Admin"): Result<Unit> {
        val deposit = dao.getDepositById(depositId) ?: return Result.failure(Exception("Deposit not found"))
        dao.updateDeposit(deposit.copy(status = "REJECTED", adminNote = reason))
        return Result.success(Unit)
    }

    // Withdrawals (10% fee strictly applied!)
    fun getAllWithdrawals(): Flow<List<WithdrawalRequestEntity>> = dao.getAllWithdrawals()
    fun getUserWithdrawals(phone: String): Flow<List<WithdrawalRequestEntity>> = dao.getWithdrawalsByUser(phone)
    fun getPendingWithdrawalByUser(phone: String): Flow<WithdrawalRequestEntity?> = dao.getPendingWithdrawalByUser(phone)
    suspend fun getWithdrawalById(id: Long): WithdrawalRequestEntity? = dao.getWithdrawalById(id)

    suspend fun submitWithdrawal(
        userPhone: String,
        accountNumber: String,
        accountTitle: String,
        grossAmount: Double
    ): Result<WithdrawalRequestEntity> {
        if (grossAmount < 500) {
            return Result.failure(Exception("کم از کم وڈراول Rs. 500 ہے"))
        }

        // ⚠️ STRICT CONSTRAINT: Only 1 pending withdrawal at a time
        val existingPending = dao.getPendingWithdrawalByUserSync(userPhone)
        if (existingPending != null) {
            return Result.failure(
                Exception("آپ کی ایک وڈراول درخواست (ID: #${existingPending.id}) پہلے سے تصدیق کے انتظار میں ہے۔ نیا وڈراول جمع کرانے سے پہلے اس کے مکمل ہونے کا انتظار کریں!")
            )
        }

        val user = dao.getUserSync(userPhone) ?: return Result.failure(Exception("صارف کا اکاؤنٹ نہیں ملا"))
        if (user.balance < grossAmount) {
            return Result.failure(Exception("ناکافی بیلنس! آپ کا بیلنس Rs. ${user.balance.toInt()} ہے"))
        }

        val fee = grossAmount * 0.10 // 10% fee
        val net = grossAmount - fee

        // Deduct full grossAmount from player balance
        dao.updateUser(user.copy(balance = user.balance - grossAmount))

        val now = System.currentTimeMillis()
        val initialBoundCode = computeBoundCode(userPhone, net, now)

        val req = WithdrawalRequestEntity(
            userPhone = userPhone,
            userName = user.name,
            accountNumber = accountNumber,
            accountTitle = accountTitle,
            grossAmount = grossAmount,
            feeAmount = fee,
            netAmount = net,
            verificationCode = initialBoundCode,
            status = "PENDING",
            hasContactedWhatsApp = false,
            userTotalBets = user.totalBets,
            userTotalWins = user.totalWins,
            userTotalDeposits = user.totalDeposits,
            timestamp = now
        )
        val id = dao.insertWithdrawal(req)
        return Result.success(req.copy(id = id))
    }

    suspend fun markWithdrawalWhatsAppContacted(withdrawalId: Long): Result<Unit> {
        val req = dao.getWithdrawalById(withdrawalId) ?: return Result.failure(Exception("Withdrawal not found"))
        dao.updateWithdrawal(req.copy(hasContactedWhatsApp = true))
        return Result.success(Unit)
    }

    suspend fun generateBoundWithdrawalCode(withdrawalId: Long): Result<String> {
        val req = dao.getWithdrawalById(withdrawalId) ?: return Result.failure(Exception("وڈراول ریکارڈ نہیں ملا"))
        val boundCode = computeBoundCode(req.userPhone, req.netAmount, req.id)
        dao.updateWithdrawal(req.copy(verificationCode = boundCode))
        return Result.success(boundCode)
    }

    suspend fun verifyWithdrawalCode(withdrawalId: Long, enteredCode: String): Result<Unit> {
        val req = dao.getWithdrawalById(withdrawalId) ?: return Result.failure(Exception("وڈراول ریکارڈ نہیں ملا"))
        if (req.status != "PENDING") {
            return Result.failure(Exception("یہ وڈراول پہلے ہی پراسیس ہو چکا ہے"))
        }
        if (req.verificationCode.trim() != enteredCode.trim()) {
            return Result.failure(Exception("غلط ویریفائی کوڈ! ایڈمن سے درست کوڈ حاصل کریں جو آپ کے فون نمبر (${req.userPhone}) اور رقم (Rs. ${req.netAmount.toInt()}) سے منسلک ہے۔"))
        }

        dao.updateWithdrawal(req.copy(status = "APPROVED", isVerified = true))

        val user = dao.getUserSync(req.userPhone)
        if (user != null) {
            dao.updateUser(
                user.copy(
                    totalWithdrawals = user.totalWithdrawals + req.grossAmount
                )
            )
        }
        return Result.success(Unit)
    }

    suspend fun regenerateWithdrawalCode(withdrawalId: Long): Result<String> {
        return generateBoundWithdrawalCode(withdrawalId)
    }

    suspend fun approveWithdrawal(withdrawalId: Long): Result<Unit> {
        val req = dao.getWithdrawalById(withdrawalId) ?: return Result.failure(Exception("Request not found"))
        if (req.status != "PENDING") {
            return Result.failure(Exception("Request already processed"))
        }

        dao.updateWithdrawal(req.copy(status = "APPROVED", isVerified = true))

        val user = dao.getUserSync(req.userPhone)
        if (user != null) {
            dao.updateUser(
                user.copy(
                    totalWithdrawals = user.totalWithdrawals + req.grossAmount
                )
            )
        }
        return Result.success(Unit)
    }

    suspend fun rejectWithdrawal(withdrawalId: Long, reason: String = "Rejected by Admin"): Result<Unit> {
        val req = dao.getWithdrawalById(withdrawalId) ?: return Result.failure(Exception("Request not found"))
        if (req.status != "PENDING") {
            return Result.failure(Exception("Request already processed"))
        }

        dao.updateWithdrawal(req.copy(status = "REJECTED", adminNote = reason))

        // Refund the user balance
        val user = dao.getUserSync(req.userPhone)
        if (user != null) {
            dao.updateUser(user.copy(balance = user.balance + req.grossAmount))
        }
        return Result.success(Unit)
    }

    // App Settings
    fun getSetting(key: String): Flow<AppSettingsEntity?> = dao.getSetting(key)
    suspend fun getSettingSync(key: String): String? = dao.getSettingSync(key)?.configValue

    suspend fun updateSetting(key: String, value: String) {
        dao.insertOrUpdateSetting(AppSettingsEntity(key, value))
    }

    // History
    fun getRecentHistory(): Flow<List<GameHistoryEntity>> = dao.getRecentHistory()

    suspend fun recordGameRound(
        winner: RouletteSlot,
        totalBets: Double,
        totalPayouts: Double,
        wasZero: Boolean
    ) {
        dao.insertHistory(
            GameHistoryEntity(
                winnerSlotId = winner.id,
                winnerTitle = winner.title,
                multiplier = winner.multiplier,
                totalRoundBets = totalBets,
                totalRoundPayouts = totalPayouts,
                wasForcedZero = wasZero
            )
        )
    }

    // Games Management (Lobby & Admin uploads)
    fun getAllGames(): Flow<List<GameItemEntity>> = dao.getAllGames()

    suspend fun addNewGame(title: String, subtitle: String, iconEmoji: String, category: String, minBet: Int) {
        val id = "game_" + System.currentTimeMillis()
        dao.insertGame(
            GameItemEntity(
                id = id,
                title = title,
                subtitle = subtitle,
                iconEmoji = iconEmoji.ifBlank { "🎮" },
                category = category,
                minBet = minBet,
                isEnabled = true
            )
        )
    }

    suspend fun toggleGame(id: String, isEnabled: Boolean) {
        val all = dao.getAllGames()
        // Or update directly
        // We will query in Viewmodel or Dao
    }

    suspend fun updateGame(game: GameItemEntity) {
        dao.updateGame(game)
    }
}
