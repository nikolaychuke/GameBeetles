package com.example.beetles

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AbilityUsageTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        viewModel = GameViewModel()
        // FREEZE (maxCharges = 2)
        viewModel.toggleAbilitySelection(AbilityType.FREEZE)
    }

    @Test
    fun `useAbility_decrementsChargesAndStopsAtZero`() {
        // 1
        assertTrue(viewModel.useAbility(AbilityType.FREEZE))
        assertEquals(1, viewModel.abilities.value?.get(AbilityType.FREEZE))

        // 2
        assertTrue(viewModel.useAbility(AbilityType.FREEZE))
        assertEquals(0, viewModel.abilities.value?.get(AbilityType.FREEZE))

        // false
        assertFalse(viewModel.useAbility(AbilityType.FREEZE))
        assertEquals(0, viewModel.abilities.value?.get(AbilityType.FREEZE))
    }
}