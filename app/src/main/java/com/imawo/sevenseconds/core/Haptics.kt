package com.imawo.sevenseconds.core

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Haptics în stil „arcade":
 *  - la fiecare tap un click scurt (HEAVY_CLICK când jucătorul e „în foc"), limitat ca frecvență
 *    ca motorul de vibrații să nu se transforme într-un bâzâit continuu;
 *  - start / rank-up / final au pattern-uri proprii.
 */
class Haptics(context: Context) {
    @Volatile
    var enabled: Boolean = true

    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= 31) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    private val canVibrate = vibrator?.hasVibrator() == true
    private var lastTapMs = 0L

    fun tap(heat: Float) {
        if (!enabled || !canVibrate) return
        val now = SystemClock.uptimeMillis()
        if (now - lastTapMs < 32L) return
        lastTapMs = now
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= 29) {
            val effect = if (heat > 0.62f) VibrationEffect.EFFECT_HEAVY_CLICK else VibrationEffect.EFFECT_CLICK
            v.vibrate(VibrationEffect.createPredefined(effect))
        } else if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(16L, 180))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(16L)
        }
    }

    /** Primul tap: pornește runda. */
    fun start() = wave(longArrayOf(0, 18, 45, 34), intArrayOf(0, 140, 0, 230))

    /** Ai atins un nou rank în timpul rundei. */
    fun rankUp() = wave(longArrayOf(0, 30, 60, 40), intArrayOf(0, 255, 0, 255))

    /** Explozia de la final. */
    fun finish() = wave(longArrayOf(0, 40, 25, 70, 25, 130), intArrayOf(0, 255, 0, 210, 0, 120))

    private fun wave(timings: LongArray, amplitudes: IntArray) {
        if (!enabled || !canVibrate) return
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(timings, -1)
        }
    }
}
