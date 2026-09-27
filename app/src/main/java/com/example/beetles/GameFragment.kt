package com.example.beetles

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class GameFragment : Fragment() {

    private lateinit var gameView: GameView
    private lateinit var textScore: TextView
    private lateinit var textTime: TextView
    private lateinit var btnStart: Button

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

        gameView.onScoreChanged = { score ->
            textScore.text = getString(R.string.score_format, score)
        }
        gameView.onTimeChanged = { t ->
            textTime.text = getString(R.string.time_format, t)
        }
        gameView.onGameOver = { score ->
            btnStart.isEnabled = true
            btnStart.text = getString(R.string.play_again)
            Toast.makeText(
                requireContext(),
                getString(R.string.game_over_format, score),
                Toast.LENGTH_LONG
            ).show()
        }

        btnStart.setOnClickListener {
            val prefs = requireContext()
                .getSharedPreferences("beetles", Context.MODE_PRIVATE)

            gameView.gameSpeed = prefs.getInt("speed", 1).coerceAtLeast(1)
            gameView.maxBeetles = prefs.getInt("maxBeetles", 5).coerceAtLeast(1)
            gameView.roundDuration = prefs.getInt("roundDuration", 30).coerceAtLeast(1)

            btnStart.isEnabled = false
            btnStart.text = getString(R.string.game_in_progress)
            gameView.post { gameView.startGame() }
        }

        return view
    }

    override fun onPause() {
        super.onPause()
        gameView.stopGame()
        btnStart.isEnabled = true
        btnStart.text = getString(R.string.start_game)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gameView.stopGame()
        gameView.onScoreChanged = null
        gameView.onTimeChanged = null
        gameView.onGameOver = null
    }
}