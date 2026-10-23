package com.example.model

enum class RouletteSlot(
    val id: String,
    val title: String,
    val multiplier: Int,
    val iconEmoji: String,
    val row: Int, // 1 for Top, 2 for Bottom, 0 for Special Zero
    val positionIndex: Int, // Clockwise position around wheel
    val badgeColorHex: Long
) {
    // Top Row (High Multipliers)
    CENTER(
        id = "CENTER",
        title = "Panther/Center",
        multiplier = 10,
        iconEmoji = "🐆",
        row = 1,
        positionIndex = 0,
        badgeColorHex = 0xFFFFD700
    ),
    DRAGON(
        id = "DRAGON",
        title = "Dragon",
        multiplier = 15,
        iconEmoji = "🐉",
        row = 1,
        positionIndex = 1,
        badgeColorHex = 0xFFFF3366
    ),
    SHARK(
        id = "SHARK",
        title = "Shark",
        multiplier = 25,
        iconEmoji = "🦈",
        row = 1,
        positionIndex = 2,
        badgeColorHex = 0xFF00E5FF
    ),
    ELEPHANT(
        id = "ELEPHANT",
        title = "Elephant",
        multiplier = 20,
        iconEmoji = "🐘",
        row = 1,
        positionIndex = 3,
        badgeColorHex = 0xFF9C27B0
    ),

    // Bottom Row
    HORSE(
        id = "HORSE",
        title = "Horse",
        multiplier = 5,
        iconEmoji = "🐎",
        row = 2,
        positionIndex = 4,
        badgeColorHex = 0xFFFF9800
    ),
    DOG(
        id = "DOG",
        title = "Dog",
        multiplier = 3,
        iconEmoji = "🐕",
        row = 2,
        positionIndex = 5,
        badgeColorHex = 0xFF4CAF50
    ),
    MERCEDES(
        id = "MERCEDES",
        title = "Mercedes Car",
        multiplier = 3,
        iconEmoji = "🏎️",
        row = 2,
        positionIndex = 6,
        badgeColorHex = 0xFF00BCD4
    ),
    BIKE125(
        id = "BIKE125",
        title = "125 Bike",
        multiplier = 4,
        iconEmoji = "🏍️",
        row = 2,
        positionIndex = 7,
        badgeColorHex = 0xFFE91E63
    ),

    // Special Zero Slot (House Wipeout)
    ZERO(
        id = "ZERO",
        title = "ZERO (0)",
        multiplier = 0,
        iconEmoji = "0️⃣",
        row = 0,
        positionIndex = 8,
        badgeColorHex = 0xFF4527A0
    );

    companion object {
        val TOP_ROW = listOf(CENTER, DRAGON, SHARK, ELEPHANT)
        val BOTTOM_ROW = listOf(HORSE, DOG, MERCEDES, BIKE125)
        val WHEEL_SEQUENCE = listOf(
            ZERO,
            CENTER,
            DRAGON,
            SHARK,
            ELEPHANT,
            HORSE,
            DOG,
            MERCEDES,
            BIKE125
        )

        fun fromId(id: String): RouletteSlot {
            return entries.firstOrNull { it.id == id } ?: ZERO
        }
    }
}
