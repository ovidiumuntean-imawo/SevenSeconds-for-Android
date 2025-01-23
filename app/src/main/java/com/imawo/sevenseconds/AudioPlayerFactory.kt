package com.imawo.sevenseconds

import android.content.Context
import android.media.SoundPool

import android.util.Log

object AudioPlayerFactory {

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>()

    /**
     * Initializes SoundPool and preloads audio files.
     */
    fun initSoundPool(context: Context, vararg soundResIds: Int) {
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .build()

        soundResIds.forEach { resId ->
            val soundId = soundPool?.load(context, resId, 1) ?: 0
            soundMap[resId] = soundId
            Log.d("AudioPlayerFactory", "Loaded sound: $resId with ID: $soundId")
        }
    }

    /**
     * Plays a sound by its resource ID.
     */
    fun playSound(resId: Int) {
        val soundId = soundMap[resId]
        if (soundId == null) {
            Log.e("AudioPlayerFactory", "Sound not found for resource ID: $resId")
        } else {
            val result = soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
            if (result == 0) {
                Log.e("AudioPlayerFactory", "Failed to play sound for resource ID: $resId")
            }
        }
    }

    /**
     * Releases resources when no longer needed.
     */
    fun releaseSoundPool() {
        soundPool?.release()
        soundPool = null
        Log.d("AudioPlayerFactory", "SoundPool released")
    }
}
