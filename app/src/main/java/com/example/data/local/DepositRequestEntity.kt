package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deposit_requests")
data class DepositRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userPhone: String,
    val userName: String = "",
    val amount: Double,
    val bonusAmount: Double = 0.0, // 10% first deposit bonus
    val totalCredited: Double = amount,
    val senderPhone: String,
    val transactionId: String,
    val paymentMethod: String = "JazzCash",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val verificationCode: String = "", // 6-digit verification code from Admin
    val isVerified: Boolean = false,
    val isFirstDeposit: Boolean = false,
    val hasContactedWhatsApp: Boolean = false,
    val adminNote: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
