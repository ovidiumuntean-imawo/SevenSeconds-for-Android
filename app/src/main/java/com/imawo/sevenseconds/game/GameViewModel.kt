package com.imawo.sevenseconds.game

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imawo.sevenseconds.SevenSecondsApp
import com.imawo.sevenseconds.core.GameConfig
import com.imawo.sevenseconds.core.PlayGamesBridge
import com.imawo.sevenseconds.core.Sfx
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Phase { Idle, Running, Over }
enum class TapResult { Started, Counted, Ignored }
enum class ChallengeOutcome { Win, Loss }

/**
 * Logica jocului (echivalentul GameManager + AppState de pe iOS).
 *
 * Diferențe față de iOS, intenționate:
 *  - cronometrul se bazează pe ceasul monoton (SystemClock.elapsedRealtime), nu pe un Timer de 0.1 s ⇒ fără drift;
 *  - dacă aplicația ajunge în fundal în timpul rundei, runda se anulează (fără scor), ca să nu se trimită scoruri „în gol".
 */
class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val sevenApp get() = getApplication<SevenSecondsApp>()
    private val prefs get() = sevenApp.prefs
    private val sound get() = sevenApp.sound

    var phase by mutableStateOf(Phase.Idle)
        private set
    var score by mutableIntStateOf(0)
        private set
    var best by mutableIntStateOf(prefs.highScore)
        private set
    var startElapsed by mutableLongStateOf(0L)
        private set
    var isNewBest by mutableStateOf(false)
        private set
    var rank by mutableStateOf<Rank?>(null)
        private set

    /** Ținta primită printr-un link de provocare (apare ca „TARGET: n" în HUD). */
    var pendingChallenge by mutableStateOf<Int?>(null)
        private set

    /** Ținta rundei curente (blocată la start, ca pe iOS). */
    var runChallengeTarget by mutableStateOf<Int?>(null)
        private set
    var outcome by mutableStateOf<ChallengeOutcome?>(null)
        private set

    private var runJob: Job? = null
    private var iceStream = 0

    fun onTap(): TapResult = when (phase) {
        Phase.Idle -> {
            start()
            TapResult.Started
        }
        Phase.Running -> {
            score += 1
            TapResult.Counted
        }
        Phase.Over -> TapResult.Ignored
    }

    private fun start() {
        score = 0
        isNewBest = false
        outcome = null
        rank = null
        runChallengeTarget = pendingChallenge
        startElapsed = SystemClock.elapsedRealtime()
        phase = Phase.Running

        sound.stop(iceStream)
        iceStream = sound.play(Sfx.Ice)

        val t0 = startElapsed
        runJob?.cancel()
        runJob = viewModelScope.launch {
            // beep la 1..6 secunde (ca pe iOS: când timeLeft e întreg și > 0)
            for (sec in 1..6) {
                delay((t0 + sec * 1000L - SystemClock.elapsedRealtime()).coerceAtLeast(0L))
                if (phase != Phase.Running) return@launch
                sound.play(Sfx.Tick)
            }
            delay((t0 + GameConfig.DURATION_MS - SystemClock.elapsedRealtime()).coerceAtLeast(0L))
            finish()
        }
    }

    private fun finish() {
        if (phase != Phase.Running) return
        sound.stop(iceStream)
        sound.play(Sfx.Explode)

        val finalScore = score
        outcome = runChallengeTarget?.let { if (finalScore > it) ChallengeOutcome.Win else ChallengeOutcome.Loss }

        if (finalScore > prefs.highScore) {
            prefs.highScore = finalScore
            best = finalScore
            isNewBest = true
        }

        val r = Ranks.forScore(finalScore)
        rank = r
        if (finalScore < GameConfig.LEADERBOARD_MAX_SCORE) PlayGamesBridge.submitScore(finalScore)
        r.achievement?.let { PlayGamesBridge.unlock(it) }

        // ca pe iOS: ținta primită se consumă când apare ecranul Game Over
        pendingChallenge = null
        phase = Phase.Over
    }

    /** „RETRY MISSION" / „RETURN TO BASE" / Back pe ecranul de rezultat. */
    fun dismissOver() {
        if (phase != Phase.Over) return
        score = 0
        outcome = null
        isNewBest = false
        rank = null
        runChallengeTarget = null
        phase = Phase.Idle
    }

    /** Link de provocare primit (sevenseconds://challenge?score=NN). */
    fun receiveChallenge(target: Int) {
        pendingChallenge = target
        if (phase == Phase.Over) dismissOver()
    }

    /** Activity.onStop — anulăm runda în curs. */
    fun onAppStopped() {
        if (phase != Phase.Running) return
        runJob?.cancel()
        sound.stop(iceStream)
        score = 0
        runChallengeTarget = null
        phase = Phase.Idle
    }

    override fun onCleared() {
        runJob?.cancel()
        sound.stop(iceStream)
        super.onCleared()
    }
}
