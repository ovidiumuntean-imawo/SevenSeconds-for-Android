package com.imawo.sevenseconds

import android.app.Application
import com.imawo.sevenseconds.core.Haptics
import com.imawo.sevenseconds.core.PlayGamesBridge
import com.imawo.sevenseconds.core.Prefs
import com.imawo.sevenseconds.core.SoundEngine

/**
 * Obiecte care trebuie să supraviețuiască schimbărilor de configurare (rotire, pliere):
 * preferințe, motorul de sunet și haptics. Le ține Application-ul, nu Activity-ul.
 */
class SevenSecondsApp : Application() {
    lateinit var prefs: Prefs
        private set
    lateinit var sound: SoundEngine
        private set
    lateinit var haptics: Haptics
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        sound = SoundEngine(this).also { it.enabled = prefs.soundOn }
        haptics = Haptics(this).also { it.enabled = prefs.hapticsOn }
        PlayGamesBridge.init(this)
    }
}
