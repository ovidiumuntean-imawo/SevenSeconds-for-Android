package com.imawo.sevenseconds

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.imawo.sevenseconds.ui.theme.SevenSecondsTheme

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        GooglePlayGamesManager.signIn(this) { success ->
            // handle sign in result
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }

        setContent {
            SevenSecondsTheme {
                GameView(
                    activity = this,
                    viewModel = gameViewModel,
                    leaderboardId = "YOUR_LEADERBOARD_ID"
                )
            }
        }
    }
}

@SuppressLint("ContextCastToActivity")
@Preview(showBackground = true)
@Composable
fun GamePreview() {
    SevenSecondsTheme {
        // Creează o listă de emitters statice pentru previzualizare
        val emitters = listOf(Offset(200f, 400f)) // Poziție statică a emitterului

        // Stare mock pentru ViewModel
        val mockViewModel = GameViewModel().apply {
            timeLeft = 0
            currentScore = 50
            previousScore = 45
            isGameRunning = false
            isGameOver = true
            achievementMessage = "Ai deblocat o realizare!"
        }
        
        GameView(
            activity = null,
            viewModel = mockViewModel,
            leaderboardId = "dummy_leaderboard_id"
        )
    }
}