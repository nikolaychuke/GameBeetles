package com.example.beetles

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AbilitySelectionTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        viewModel = GameViewModel()
    }

    @Test
    fun `selectingAbilities_fillsChargesAndLimitsToTwo`() {
        // FREEZE
        viewModel.toggleAbilitySelection(AbilityType.FREEZE)
        assertTrue(viewModel.isSelected(AbilityType.FREEZE))
        assertEquals(AbilityType.FREEZE.maxCharges, viewModel.abilities.value?.get(AbilityType.FREEZE))

        // BOMB
        viewModel.toggleAbilitySelection(AbilityType.BOMB)
        assertTrue(viewModel.isSelected(AbilityType.BOMB))

        // False (SLOW)
        viewModel.toggleAbilitySelection(AbilityType.SLOW)
        assertFalse(viewModel.isSelected(AbilityType.SLOW))
        assertEquals(2, viewModel.selectedAbilities.value?.size)
    }
}