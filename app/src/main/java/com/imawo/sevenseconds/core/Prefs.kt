package com.imawo.sevenseconds.core

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("seven_seconds", Context.MODE_PRIVATE)

    /** Cel mai bun scor (pe iOS: UserDefaults "highScore"). */
    var highScore: Int
        get() = sp.getInt("highScore", 0)
        set(value) = sp.edit().putInt("highScore", value).apply()

    var soundOn: Boolean
        get() = sp.getBoolean("soundOn", true)
        set(value) = sp.edit().putBoolean("soundOn", value).apply()

    var hapticsOn: Boolean
        get() = sp.getBoolean("hapticsOn", true)
        set(value) = sp.edit().putBoolean("hapticsOn", value).apply()

    /** null = urmează setarea sistemului (animații dezactivate ⇒ efecte reduse). */
    var reduceFx: Boolean?
        get() = if (sp.contains("reduceFx")) sp.getBoolean("reduceFx", false) else null
        set(value) {
            val e = sp.edit()
            if (value == null) e.remove("reduceFx") else e.putBoolean("reduceFx", value)
            e.apply()
        }
}
