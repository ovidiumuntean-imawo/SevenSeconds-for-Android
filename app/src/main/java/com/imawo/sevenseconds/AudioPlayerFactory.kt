package com.imawo.sevenseconds

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

object AudioPlayerFactory {

    /**
     * Creates and prepares a MediaPlayer for a given raw resource.
     * Usage: val player = AudioPlayerFactory.createMediaPlayer(context, R.raw.timer)
     */
    fun createMediaPlayer(context: Context, resId: Int): MediaPlayer? {
        return try {
            val mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.apply {
                setOnPreparedListener { start() }
                setOnErrorListener { mp, what, extra ->
                    Log.e("AudioPlayerFactory", "Error code: $what, extra: $extra")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerFactory", "Failed to create player: ${e.localizedMessage}")
            null
        }
    }
}