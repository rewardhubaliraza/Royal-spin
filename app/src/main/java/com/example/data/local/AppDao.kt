package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Users
    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    fun getUser(phone: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserSync(phone: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users WHERE phone = :phone")
    suspend fun countUserByPhone(phone: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE deviceId = :deviceId")
    suspend fun countUsersByDeviceId(deviceId: String): Int

    // Deposits
    @Query("SELECT * FROM deposit_requests ORDER BY timestamp DESC")
    fun getAllDeposits(): Flow<List<DepositRequestEntity>>

    @Query("SELECT * FROM deposit_requests WHERE userPhone = :phone ORDER BY timestamp DESC")
    fun getDepositsByUser(phone: String): Flow<List<DepositRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositRequestEntity): Long

    @Update
    suspend fun updateDeposit(deposit: DepositRequestEntity)

    @Query("SELECT * FROM deposit_requests WHERE id = :id LIMIT 1")
    suspend fun getDepositById(id: Long): DepositRequestEntity?

    @Query("SELECT * FROM deposit_requests WHERE userPhone = :phone AND status = 'PENDING' ORDER BY timestamp DESC LIMIT 1")
    fun getPendingDepositByUser(phone: String): Flow<DepositRequestEntity?>

    @Query("SELECT * FROM deposit_requests WHERE userPhone = :phone AND status = 'PENDING' ORDER BY timestamp DESC LIMIT 1")
    suspend fun getPendingDepositByUserSync(phone: String): DepositRequestEntity?

    // Withdrawals
    @Query("SELECT * FROM withdrawal_requests ORDER BY timestamp DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalRequestEntity>>

    @Query("SELECT * FROM withdrawal_requests WHERE userPhone = :phone ORDER BY timestamp DESC")
    fun getWithdrawalsByUser(phone: String): Flow<List<WithdrawalRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalRequestEntity): Long

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalRequestEntity)

    @Query("SELECT * FROM withdrawal_requests WHERE id = :id LIMIT 1")
    suspend fun getWithdrawalById(id: Long): WithdrawalRequestEntity?

    @Query("SELECT * FROM withdrawal_requests WHERE userPhone = :phone AND status = 'PENDING' ORDER BY timestamp DESC LIMIT 1")
    fun getPendingWithdrawalByUser(phone: String): Flow<WithdrawalRequestEntity?>

    @Query("SELECT * FROM withdrawal_requests WHERE userPhone = :phone AND status = 'PENDING' ORDER BY timestamp DESC LIMIT 1")
    suspend fun getPendingWithdrawalByUserSync(phone: String): WithdrawalRequestEntity?

    // Settings
    @Query("SELECT * FROM app_settings WHERE configKey = :key LIMIT 1")
    fun getSetting(key: String): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE configKey = :key LIMIT 1")
    suspend fun getSettingSync(key: String): AppSettingsEntity?

    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSettingsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSetting(setting: AppSettingsEntity)

    // Game History
    @Query("SELECT * FROM game_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 30): Flow<List<GameHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: GameHistoryEntity): Long

    // Games Management (for lobby & admin upload)
    @Query("SELECT * FROM game_items ORDER BY createdAt ASC")
    fun getAllGames(): Flow<List<GameItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameItemEntity)

    @Update
    suspend fun updateGame(game: GameItemEntity)

    @Query("DELETE FROM game_items WHERE id = :id")
    suspend fun deleteGame(id: String)

    @Query("SELECT COUNT(*) FROM game_items")
    suspend fun countGames(): Int
}
