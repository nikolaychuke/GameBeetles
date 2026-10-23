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

    private val _selectedAbilities = MutableLiveData<Set<AbilityType>>(emptySet())
    val selectedAbilities: LiveData<Set<AbilityType>> = _selectedAbilities
    private val _abilities = MutableLiveData<Map<AbilityType, Int>>(emptyMap())
    val abilities: LiveData<Map<AbilityType, Int>> = _abilities

    fun onScoreChanged(newScore: Int) {
        _score.value = newScore
    }

    fun onTimeChanged(time: Int) {
        _timeLeft.value = time
    }

    fun onGameStarted() {
        refillSelectedAbilities()
        _isRunning.value = true
    }

    fun onGameStopped() {
        _isRunning.value = false
        refillSelectedAbilities()
    }

    fun setYuanRate(rate: Float) {
        _yuanRate.value = rate
    }

    fun getCachedYuanRate(): Float = _yuanRate.value ?: 0f

    fun saveState(
        score: Int,
        hits: Int,
        misses: Int,
        timeLeft: Int,
        bugs: List<Bug>,
        isRunning: Boolean
    ) {
        _score.value = score
        _timeLeft.value = timeLeft
        _bugs.value = bugs
        _isRunning.value = isRunning
    }


    fun toggleAbilitySelection(type: AbilityType) {
        val current = _selectedAbilities.value ?: emptySet()

        val newSelection = if (type in current) {
            current - type
        } else {
            if (current.size >= 2) {
                return
            }
            current + type
        }

        _selectedAbilities.value = newSelection

        val newAbilities = newSelection.associateWith { type ->
            _abilities.value?.get(type) ?: AbilityType.valueOf(type.name).maxCharges
        }
        _abilities.value = newAbilities
    }

    fun refillSelectedAbilities() {
        val selected = _selectedAbilities.value ?: emptySet()
        _abilities.value = selected.associateWith { it.maxCharges }
    }

    fun useAbility(type: AbilityType): Boolean {
        val current = _abilities.value ?: return false
        val charges = current[type] ?: 0
        if (charges <= 0) return false

        _abilities.value = current + (type to (charges - 1))
        return true
    }

    fun isSelected(type: AbilityType): Boolean {
        return _selectedAbilities.value?.contains(type) ?: false
    }

    fun canSelectMore(): Boolean {
        return (_selectedAbilities.value?.size ?: 0) < 2
    }
}