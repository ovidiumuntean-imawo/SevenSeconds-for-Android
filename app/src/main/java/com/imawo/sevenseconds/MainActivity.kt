package com.imawo.sevenseconds

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.imawo.sevenseconds.core.GameConfig
import com.imawo.sevenseconds.core.Platform
import com.imawo.sevenseconds.core.PlayGamesBridge
import com.imawo.sevenseconds.game.GameViewModel
import com.imawo.sevenseconds.ui.SevenSecondsRoot
import com.imawo.sevenseconds.ui.theme.SevenSecondsTheme

class MainActivity : ComponentActivity() {

    private val vm: GameViewModel by viewModels()

    // pentru clasamente / achievements (Play Games deschide propriile ecrane)
    private val gamesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // telefon: portret; tabletă / pliabil deschis: orice orientare (layout-ul se scalează singur)
        requestedOrientation =
            if (resources.configuration.smallestScreenWidthDp >= 600) ActivityInfo.SCREEN_ORIENTATION_FULL_USER
            else ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT

        applyImmersive()
        Platform.requestHighRefreshRate(this)
        PlayGamesBridge.attach(this, gamesLauncher)
        handleIntent(intent)

        setContent {
            SevenSecondsTheme {
                SevenSecondsRoot(vm)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        PlayGamesBridge.refreshAuth()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applyImmersive()
    }

    override fun onStop() {
        vm.onAppStopped()
        super.onStop()
    }

    override fun onDestroy() {
        PlayGamesBridge.detach(this)
        super.onDestroy()
    }

    private fun applyImmersive() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val c = WindowInsetsControllerCompat(window, window.decorView)
        c.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        c.hide(WindowInsetsCompat.Type.systemBars())
    }

    /** sevenseconds://challenge?score=NN */
    private fun handleIntent(i: Intent?) {
        val data = i?.data ?: return
        if (data.scheme == GameConfig.CHALLENGE_SCHEME && data.host == GameConfig.CHALLENGE_HOST) {
            data.getQueryParameter("score")?.toIntOrNull()?.takeIf { it in 1..999 }?.let { vm.receiveChallenge(it) }
            i.data = null // nu reprocesăm același link la recreare
        }
    }
}
