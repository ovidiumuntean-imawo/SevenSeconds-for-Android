package com.imawo.sevenseconds

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Analog to the Swift GameManager. Holds game state, timers, and orchestrates audio.
 */
class GameViewModel : ViewModel() {

    // Observables
    var timeLeft by mutableStateOf(7)
    var currentScore by mutableStateOf(0)
    var previousScore by mutableStateOf(0)
    var isGameRunning by mutableStateOf(false)
    var isGameOver by mutableStateOf(false)
    var achievementMessage by mutableStateOf<String?>(null)

    // Audio
    private var timerBeep: MediaPlayer? = null
    private var explodeBeep: MediaPlayer? = null
    private var buttonBeep: MediaPlayer? = null

    private var timerJob: Job? = null

    fun initAudio(context: Context) {
        // Preload all required sounds
        timerBeep = AudioPlayerFactory.createMediaPlayer(context, R.raw.timer)
        explodeBeep = AudioPlayerFactory.createMediaPlayer(context, R.raw.explode)
        buttonBeep = AudioPlayerFactory.createMediaPlayer(context, R.raw.button)
    }

    fun startGame(context: Context, leaderboardId: String) {
        isGameRunning = true
        isGameOver = false
        currentScore = 0
        timeLeft = 7

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
                // Play short beep each second
                timerBeep?.start()
            }
            endGame(context, leaderboardId)
        }
    }

    private fun endGame(context: Context, leaderboardId: String) {
        isGameRunning = false
        isGameOver = true
        explodeBeep?.start()

        // Submit score if < 125 (mimicking Swift logic)
        if (currentScore < 125) {
            GooglePlayGamesManager.submitScore(context, leaderboardId, currentScore)
        }

        val (message, unlockId) = AchievementManager.handleAchievementsPair(currentScore)
        achievementMessage = message

        // Unlock achievement if needed
        unlockId?.let { id ->
            GooglePlayGamesManager.unlockAchievement(context, id)
        }
    }

    fun buttonPressed() {
        buttonBeep?.start()
    }

    fun incrementScore() {
        currentScore++
    }

    fun resetGame() {
        isGameRunning = false
        isGameOver = false
        timeLeft = 7
        currentScore = 0
    }
}