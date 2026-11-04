package com.imawo.sevenseconds.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.imawo.sevenseconds.R
import java.lang.ref.WeakReference

/**
 * Echivalentul Android al GameCenterManager.swift, pe Play Games Services **v2**
 * (SDK-ul v1 — GoogleSignIn + Games.getLeaderboardsClient — este depreciat/eliminat).
 *
 * Cât timp ID-urile din games-ids.xml sunt „REPLACE_ME", toate apelurile sunt ignorate în siguranță.
 */
object PlayGamesBridge {
    var signedIn by mutableStateOf(false)
        private set

    private var appContext: Context? = null
    private var activityRef: WeakReference<Activity>? = null
    private var launcher: ActivityResultLauncher<Intent>? = null

    private fun idOrNull(res: Int): String? {
        val s = appContext?.getString(res) ?: return null
        return if (s.isBlank() || s.startsWith("REPLACE")) null else s
    }

    val isConfigured: Boolean
        get() = idOrNull(R.string.game_services_project_id) != null &&
            idOrNull(R.string.leaderboard_main) != null

    fun init(app: Context) {
        appContext = app.applicationContext
        if (isConfigured) {
            try {
                PlayGamesSdk.initialize(app)
            } catch (ignored: Throwable) {
            }
        }
    }

    fun attach(activity: Activity, launcher: ActivityResultLauncher<Intent>) {
        activityRef = WeakReference(activity)
        this.launcher = launcher
        refreshAuth()
    }

    fun detach(activity: Activity) {
        if (activityRef?.get() === activity) {
            activityRef = null
            launcher = null
        }
    }

    /** Verifică dacă jucătorul e deja autentificat (v2 face sign-in automat la lansare). */
    fun refreshAuth() {
        val a = activityRef?.get() ?: return
        if (!isConfigured) return
        try {
            PlayGames.getGamesSignInClient(a).isAuthenticated().addOnCompleteListener { task ->
                signedIn = task.isSuccessful && task.result.isAuthenticated
            }
        } catch (ignored: Throwable) {
        }
    }

    private fun signIn(onDone: (Boolean) -> Unit) {
        val a = activityRef?.get() ?: return onDone(false)
        try {
            PlayGames.getGamesSignInClient(a).signIn().addOnCompleteListener { task ->
                signedIn = task.isSuccessful && task.result.isAuthenticated
                onDone(signedIn)
            }
        } catch (ignored: Throwable) {
            onDone(false)
        }
    }

    fun submitScore(score: Int) {
        val a = activityRef?.get() ?: return
        val id = idOrNull(R.string.leaderboard_main) ?: return
        if (!signedIn) return
        try {
            PlayGames.getLeaderboardsClient(a).submitScore(id, score.toLong())
        } catch (ignored: Throwable) {
        }
    }

    fun unlock(achievementRes: Int) {
        val a = activityRef?.get() ?: return
        val id = idOrNull(achievementRes) ?: return
        if (!signedIn) return
        try {
            PlayGames.getAchievementsClient(a).unlock(id)
        } catch (ignored: Throwable) {
        }
    }

    fun showLeaderboard() = whenSignedIn { a, l ->
        val id = idOrNull(R.string.leaderboard_main) ?: return@whenSignedIn
        PlayGames.getLeaderboardsClient(a).getLeaderboardIntent(id).addOnSuccessListener { l.launch(it) }
    }

    fun showAchievements() = whenSignedIn { a, l ->
        PlayGames.getAchievementsClient(a).getAchievementsIntent().addOnSuccessListener { l.launch(it) }
    }

    private fun whenSignedIn(action: (Activity, ActivityResultLauncher<Intent>) -> Unit) {
        val a = activityRef?.get() ?: return
        val l = launcher ?: return
        if (!isConfigured) {
            Toast.makeText(a, R.string.games_not_configured, Toast.LENGTH_LONG).show()
            return
        }
        val run = {
            try {
                action(a, l)
            } catch (ignored: Throwable) {
                Toast.makeText(a, R.string.games_unavailable_message, Toast.LENGTH_LONG).show()
            }
        }
        if (signedIn) {
            run()
        } else {
            signIn { ok ->
                if (ok) run() else Toast.makeText(a, R.string.games_unavailable_message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
