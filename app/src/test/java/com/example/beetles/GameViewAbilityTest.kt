package com.example.beetles

import android.os.Looper
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GameViewAbilityTest {

    private lateinit var gameView: GameView

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        gameView = GameView(context)
        gameView.layout(0, 0, 1080, 1920)
        gameView.startGame()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `activateAbility_allFourAbilitiesReturnTrue`() {
        assertTrue(gameView.activateAbility(AbilityType.FREEZE))
        assertTrue(gameView.activateAbility(AbilityType.SLOW))
        assertTrue(gameView.activateAbility(AbilityType.MULTI_HIT))
        assertTrue(gameView.activateAbility(AbilityType.BOMB))
    }
}