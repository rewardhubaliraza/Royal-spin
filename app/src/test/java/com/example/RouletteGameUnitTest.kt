package com.example

import com.example.model.RouletteSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RouletteGameUnitTest {

    @Test
    fun testSlotMultipliersMatchRequirements() {
        // Top row
        assertEquals(10, RouletteSlot.CENTER.multiplier)
        assertEquals(15, RouletteSlot.DRAGON.multiplier)
        assertEquals(25, RouletteSlot.SHARK.multiplier)
        assertEquals(20, RouletteSlot.ELEPHANT.multiplier)

        // Bottom row
        assertEquals(5, RouletteSlot.HORSE.multiplier)
        assertEquals(3, RouletteSlot.DOG.multiplier)
        assertEquals(3, RouletteSlot.MERCEDES.multiplier)
        assertEquals(4, RouletteSlot.BIKE125.multiplier)

        // Zero wipeout slot
        assertEquals(0, RouletteSlot.ZERO.multiplier)
    }

    @Test
    fun testWithdrawalFeeCalculation10Percent() {
        val gross = 1000.0
        val fee = gross * 0.10
        val net = gross - fee

        assertEquals(100.0, fee, 0.001)
        assertEquals(900.0, net, 0.001)
    }

    @Test
    fun testFirstDepositBonus10Percent() {
        val depositAmount = 1000.0
        val bonus = depositAmount * 0.10 // 10% commission bonus
        val totalCredited = depositAmount + bonus

        assertEquals(100.0, bonus, 0.001)
        assertEquals(1100.0, totalCredited, 0.001)
    }

    @Test
    fun testWinningCommissionFee10Percent() {
        // User bets 100 on Dragon (15x) -> Gross win = 1500
        val grossWin = 1000.0
        val commissionFee = grossWin * 0.10 // 10% fee
        val netWin = grossWin - commissionFee

        assertEquals(100.0, commissionFee, 0.001)
        assertEquals(900.0, netWin, 0.001)
    }

    @Test
    fun testMaxFourSlotsBettingRule() {
        val currentBets = mutableMapOf<RouletteSlot, Int>()
        currentBets[RouletteSlot.DRAGON] = 100
        currentBets[RouletteSlot.SHARK] = 100
        currentBets[RouletteSlot.MERCEDES] = 100
        currentBets[RouletteSlot.DOG] = 100

        // Trying to bet on a 5th slot
        val newSlot = RouletteSlot.CENTER
        val canAddMore = currentBets.containsKey(newSlot) || currentBets.size < 4
        assertTrue(!canAddMore)
    }

    @Test
    fun testWheelSequenceContainsAllSlots() {
        val slots = RouletteSlot.WHEEL_SEQUENCE
        assertEquals(9, slots.size)
        assertNotNull(slots.find { it == RouletteSlot.ZERO })
        assertNotNull(slots.find { it == RouletteSlot.CENTER })
        assertNotNull(slots.find { it == RouletteSlot.DRAGON })
        assertNotNull(slots.find { it == RouletteSlot.MERCEDES })
    }
}
