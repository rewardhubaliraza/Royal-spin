package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "withdrawal_requests")
data class WithdrawalRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userPhone: String,
    val userName: String = "",
    val accountNumber: String,
    val accountTitle: String,
    val paymentMethod: String = "JazzCash",
    val grossAmount: Double, // e.g., 1000.0
    val feeAmount: Double,   // 10% = 100.0
    val netAmount: Double,   // e.g., 900.0 (net payable)
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val verificationCode: String = "", // 6-digit verification code from Admin
    val isVerified: Boolean = false,
    val hasContactedWhatsApp: Boolean = false,
    val userTotalBets: Double = 0.0,
    val userTotalWins: Double = 0.0,
    val userTotalDeposits: Double = 0.0,
    val adminNote: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
