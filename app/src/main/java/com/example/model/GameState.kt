package com.example.model

enum class GamePhase {
    BETTING,    // 15 seconds to place bets
    SPINNING,   // 8 seconds wheel spinning animation
    RESULT      // 4 seconds result presentation & win payout
}

data class CurrentBet(
    val slot: RouletteSlot,
    val amount: Int
)

data class LiveTickerItem(
    val id: String,
    val phoneMasked: String,
    val actionType: String, // "DEPOSIT", "WITHDRAWAL", "BIG_WIN"
    val amount: Double,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class BotPlayerBet(
    val botName: String,
    val slot: RouletteSlot,
    val chipAmount: Int
)
