package com.imawo.sevenseconds.core

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.imawo.sevenseconds.R

enum class Sfx { Tap, Tick, Explode, Ice }

/**
 * Un singur SoundPool pentru toate efectele (latență mică la tap-uri rapide).
 * - Tap: pitch-ul crește cu „heat" (cu cât joci mai repede, cu atât sună mai ascuțit).
 * - Ice: pornește odată cu runda și se oprește la final (ca pe iOS).
 */
class SoundEngine(context: Context) {
    @Volatile
    var enabled: Boolean = true

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids = HashMap<Sfx, Int>()
    private val ready = HashSet<Int>()

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) synchronized(ready) { ready.add(sampleId) }
        }
        ids[Sfx.Tap] = pool.load(context, R.raw.button, 1)
        ids[Sfx.Tick] = pool.load(context, R.raw.timer_tick, 1)
        ids[Sfx.Explode] = pool.load(context, R.raw.explode, 2)
        ids[Sfx.Ice] = pool.load(context, R.raw.ice_cracking, 2)
    }

    /** Întoarce streamId (0 dacă nu s-a redat nimic). */
    fun play(sfx: Sfx, volume: Float = 1f, rate: Float = 1f): Int {
        if (!enabled) return 0
        val id = ids[sfx] ?: return 0
        val loaded = synchronized(ready) { ready.contains(id) }
        if (!loaded) return 0
        return pool.play(id, volume, volume, if (sfx == Sfx.Tap) 1 else 2, 0, rate.coerceIn(0.5f, 2f))
    }

    fun playTap(heat: Float): Int =
        play(Sfx.Tap, volume = 0.5f + 0.2f * heat, rate = 0.92f + 0.55f * heat)

    fun stop(streamId: Int) {
        if (streamId != 0) pool.stop(streamId)
    }

    fun release() = pool.release()
}
