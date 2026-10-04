package com.example.beetles

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _timeLeft = MutableLiveData(30)
    val timeLeft: LiveData<Int> = _timeLeft

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _bugs = MutableLiveData<List<Bug>>(emptyList())

    var gameSpeed: Int = 1
    var maxBeetles: Int = 5
    var roundDuration: Int = 30
    var bonusInterval: Int = 15

    private val _yuanRate = MutableLiveData(0f)

    fun onScoreChanged(newScore: Int) {
        _score.value = newScore
    }

    fun onTimeChanged(time: Int) {
        _timeLeft.value = time
    }

    fun onGameStarted() {
        _isRunning.value = true
    }

    fun onGameStopped() {
        _isRunning.value = false
    }

    fun setYuanRate(rate: Float) {
        _yuanRate.value = rate
    }

    fun getCachedYuanRate(): Float = _yuanRate.value ?: 0f

    fun saveState(score: Int, hits: Int, misses: Int, timeLeft: Int, bugs: List<Bug>, isRunning: Boolean) {
        _score.value = score
        _timeLeft.value = timeLeft
        _bugs.value = bugs
        _isRunning.value = isRunning
    }

    fun restoreState(): GameState? {
        val bugsList = _bugs.value
        if (bugsList.isNullOrEmpty() && (_score.value ?: 0) == 0) return null
        return GameState(
            _score.value ?: 0,
            0,
            0,
            _timeLeft.value ?: 30,
            bugsList ?: emptyList(),
            _isRunning.value ?: false
        )
    }
}