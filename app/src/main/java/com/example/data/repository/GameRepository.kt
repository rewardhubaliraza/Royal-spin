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
        if (existingDeviceCount >= 2 && cleanPhone != "03073715192") {
            return Result.failure(Exception("Maximum limit of 2 accounts reached for this device."))
        }

        val isAdmin = cleanPhone == "03073715192"
        val newUser = UserEntity(
            phone = cleanPhone,
            name = name.ifBlank { if (isAdmin) "Master Admin" else "Player_${cleanPhone.takeLast(4)}" },
            password = pass,
            balance = if (isAdmin) 50000.0 else 0.0, // Rs. 0 initial balance as requested
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
    fun getAllDeposits(): Flow<List<DepositRequestEntity>> = dao.getAllDeposits()
    fun getUserDeposits(phone: String): Flow<List<DepositRequestEntity>> = dao.getDepositsByUser(phone)

    suspend fun submitDeposit(
        userPhone: String,
        amount: Double,
        senderPhone: String,
        trxId: String
    ): Result<Long> {
        if (amount < 100) {
            return Result.failure(Exception("Minimum deposit is Rs. 100"))
        }
        if (trxId.isBlank()) {
            return Result.failure(Exception("Please provide a valid JazzCash Transaction ID (TID)"))
        }

        val user = dao.getUserSync(userPhone)
        val isFirst = user?.isFirstDeposit ?: true
        val bonus = if (isFirst) amount * 0.10 else 0.0 // 10% bonus on first deposit!
        val totalToCredit = amount + bonus

        val req = DepositRequestEntity(
            userPhone = userPhone,
            amount = amount,
            bonusAmount = bonus,
            totalCredited = totalToCredit,
            senderPhone = senderPhone.ifBlank { userPhone },
            transactionId = trxId.trim()
        )
        val id = dao.insertDeposit(req)
        return Result.success(id)
    }

    suspend fun approveDeposit(depositId: Long): Result<Unit> {
        val deposit = dao.getDepositById(depositId) ?: return Result.failure(Exception("Deposit not found"))
        if (deposit.status != "PENDING") {
            return Result.failure(Exception("Request already processed"))
        }

        dao.updateDeposit(deposit.copy(status = "APPROVED"))

        // Credit user balance including 10% first deposit bonus if applicable
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

    suspend fun submitWithdrawal(
        userPhone: String,
        accountNumber: String,
        accountTitle: String,
        grossAmount: Double
    ): Result<Long> {
        if (grossAmount < 500) {
            return Result.failure(Exception("Minimum withdrawal is Rs. 500"))
        }
        val user = dao.getUserSync(userPhone) ?: return Result.failure(Exception("User not found"))
        if (user.balance < grossAmount) {
            return Result.failure(Exception("Insufficient balance. Your balance is Rs. ${user.balance}"))
        }

        val fee = grossAmount * 0.10 // 10% fee
        val net = grossAmount - fee

        // Deduct full grossAmount from player balance
        dao.updateUser(user.copy(balance = user.balance - grossAmount))

        val req = WithdrawalRequestEntity(
            userPhone = userPhone,
            accountNumber = accountNumber,
            accountTitle = accountTitle,
            grossAmount = grossAmount,
            feeAmount = fee,
            netAmount = net,
            userTotalBets = user.totalBets,
            userTotalWins = user.totalWins,
            userTotalDeposits = user.totalDeposits
        )
        val id = dao.insertWithdrawal(req)
        return Result.success(id)
    }

    suspend fun approveWithdrawal(withdrawalId: Long): Result<Unit> {
        val req = dao.getWithdrawalById(withdrawalId) ?: return Result.failure(Exception("Request not found"))
        if (req.status != "PENDING") {
            return Result.failure(Exception("Request already processed"))
        }

        dao.updateWithdrawal(req.copy(status = "APPROVED"))

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
