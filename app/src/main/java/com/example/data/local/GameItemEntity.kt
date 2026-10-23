package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_items")
data class GameItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val category: String, // "ROULETTE", "CARD", "CASINO"
    val isEnabled: Boolean = true,
    val activePlayers: Int = 120,
    val minBet: Int = 10,
    val maxBet: Int = 50000,
    val createdAt: Long = System.currentTimeMillis()
)
