package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val configKey: String,
    val configValue: String
)

@Entity(tableName = "game_history")
data class GameHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val winnerSlotId: String,
    val winnerTitle: String,
    val multiplier: Int,
    val totalRoundBets: Double = 0.0,
    val totalRoundPayouts: Double = 0.0,
    val wasForcedZero: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
