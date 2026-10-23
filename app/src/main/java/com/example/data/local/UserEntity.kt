package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val phone: String,
    val name: String,
    val password: String,
    val balance: Double = 0.0, // Rs. 0 initial balance as requested
    val totalDeposits: Double = 0.0,
    val totalWithdrawals: Double = 0.0,
    val totalBets: Double = 0.0,
    val totalWins: Double = 0.0,
    val isFirstDeposit: Boolean = true,
    val deviceId: String = "",
    val isAdmin: Boolean = false,
    val isBanned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
