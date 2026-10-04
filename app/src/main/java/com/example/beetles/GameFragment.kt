package com.example.beetles

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.beetles.data.CurrentPlayer
import com.example.beetles.data.GameRepository
import com.example.beetles.data.ScoreEntity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameFragment : Fragment() {

    private lateinit var gameView: GameView
    private lateinit var textScore: TextView
    private lateinit var textTime: TextView
    private lateinit var btnStart: Button

    private val viewModel: GameViewModel by viewModel()
    private val repository: GameRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.game_form, container, false)

        gameView = view.findViewById(R.id.gameView)
        textScore = view.findViewById(R.id.textScore)
        textTime = view.findViewById(R.id.textTime)
        btnStart = view.findViewById(R.id.btnStart)

        viewModel.restoreState()?.let { state ->
            gameView.restoreState(state)
        }

        viewModel.score.observe(viewLifecycleOwner) { score ->
            textScore.text = getString(R.string.score_format, score)
        }
        viewModel.timeLeft.observe(viewLifecycleOwner) { time ->
            textTime.text = getString(R.string.time_format, time)
        }
        viewModel.isRunning.observe(viewLifecycleOwner) { running ->
            if (running) {
                btnStart.isEnabled = false
                btnStart.text = getString(R.string.game_in_progress)
            } else {
                btnStart.isEnabled = true
                btnStart.text = getString(R.string.start_game)
            }
        }



        gameView.onGetYuanRate = { viewModel.getCachedYuanRate() }

        gameView.onYuanRateLoaded = { rate ->
            viewModel.setYuanRate(rate)
        }
        gameView.onScoreChanged = { score ->
            viewModel.onScoreChanged(score)
        }
        gameView.onTimeChanged = { time ->
            viewModel.onTimeChanged(time)
        }
        gameView.onGameOver = { result ->
            viewModel.onGameStopped()
            saveScoreAndShowDialog(result)
        }

        btnStart.setOnClickListener {
            val playerId = CurrentPlayer.getPlayerId(requireContext())
            if (playerId <= 0L) {
                Toast.makeText(
                    requireContext(),
                    "Сначала зарегистрируйтесь на вкладке «Игрок»",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val prefs = requireContext()
                .getSharedPreferences("beetles", Context.MODE_PRIVATE)

            viewModel.gameSpeed = prefs.getInt("speed", 1).coerceAtLeast(1)
            viewModel.bonusInterval = prefs.getInt("bonusInterval", 15).coerceAtLeast(1)
            viewModel.maxBeetles = prefs.getInt("maxBeetles", 5).coerceAtLeast(1)
            viewModel.roundDuration = prefs.getInt("roundDuration", 30).coerceAtLeast(1)

            gameView.gameSpeed = viewModel.gameSpeed
            gameView.bonusIntervalSeconds = viewModel.bonusInterval
            gameView.maxBeetles = viewModel.maxBeetles
            gameView.roundDuration = viewModel.roundDuration

            viewModel.onGameStarted()
            gameView.post { gameView.startGame() }
        }

        return view
    }

    private fun saveScoreAndShowDialog(result: GameResult) {
        val playerId = CurrentPlayer.getPlayerId(requireContext())

        lifecycleScope.launch {
            if (playerId > 0L) {
                repository.saveScore(
                    ScoreEntity(
                        playerId = playerId,
                        score = result.score,
                        hits = result.hits,
                        misses = result.misses,
                        accuracy = result.accuracy,
                        difficulty = viewModel.gameSpeed,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            showResultDialog(result)
        }
    }

    private fun showResultDialog(result: GameResult) {
        val accuracyPercent = (result.accuracy * 100).toInt()

        val message = buildString {
            appendLine(getString(R.string.result_score, result.score))
            appendLine(getString(R.string.result_hits, result.hits))
            appendLine(getString(R.string.result_misses, result.misses))
            append(getString(R.string.result_accuracy, accuracyPercent))
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.result_title)
            .setMessage(message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.play_again) { _, _ ->
                btnStart.performClick()
            }
            .setCancelable(false)
            .show()
    }

    override fun onPause() {
        super.onPause()
        val state = gameView.saveState()
        viewModel.saveState(
            state.score,
            state.hits,
            state.misses,
            state.timeLeft,
            state.bugs,
            state.isRunning
        )
        gameView.stopGame()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gameView.stopGame()
        gameView.onScoreChanged = null
        gameView.onTimeChanged = null
        gameView.onGameOver = null
    }
}