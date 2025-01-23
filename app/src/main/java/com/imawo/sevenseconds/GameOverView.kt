package com.imawo.sevenseconds

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.games.Games

@Composable
fun GameOverView(
    viewModel: GameViewModel,
    activity: Activity,
    leaderboardId: String,
    onClose: () -> Unit
) {
    var scaleGameOver by remember { mutableStateOf(3f) }
    var scaleScore by remember { mutableStateOf(0f) }

    val animatedScaleGameOver by animateFloatAsState(
        targetValue = scaleGameOver,
        animationSpec = tween(durationMillis = 1000)
    )

    val animatedScaleScore by animateFloatAsState(
        targetValue = scaleScore,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(Unit) {
        scaleGameOver = 1f
        scaleScore = 1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .padding(16.dp)
    ) {
        // Background
        RotatingBackground(
            isAnimating = false,
            modifier = Modifier.matchParentSize()
        )

        // Particle Effect
        ParticleView(
            isActive = true,
            particleCount = 120
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "Game Over" Text
            Text(
                text = "game over",
                fontSize = 64.sp,
                color = Color.White,
                modifier = Modifier.scale(animatedScaleGameOver)
            )

            // Score Section
            Text(
                text = "YOU SCORED",
                fontSize = 28.sp,
                color = Color.White
            )
            Text(
                text = "${viewModel.currentScore}",
                fontSize = 72.sp,
                color = Color.White,
                modifier = Modifier.scale(animatedScaleScore)
            )
            Text(
                text = "HITS",
                fontSize = 28.sp,
                color = Color.White
            )

            // Play Again Button
            Button(onClick = {
                viewModel.previousScore = viewModel.currentScore
                viewModel.resetGame()
                onClose()
            }) {
                Text("Play again!")
            }

            Spacer(modifier = Modifier.weight(1f))

            // High Scores Section
            Text(
                text = "How other players are doing?",
                fontSize = 22.sp,
                color = Color.White
            )
            Button(onClick = {
                GooglePlayGamesManager.showLeaderboard(activity, leaderboardId)
            }) {
                Text("View high scores")
            }

            // Facebook Group Section
            Text(
                text = "Share your best screenshots!",
                fontSize = 18.sp,
                color = Color.White
            )
            Button(onClick = {
                val url = "https://facebook.com/groups/sevensecondschampions"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                activity.startActivity(intent)
            }) {
                Text("Visit our FB Group")
            }
        }
    }

    // Achievement Dialog
    viewModel.achievementMessage?.let { message ->
        AchievementDialog(
            message = message,
            onDismiss = {
                viewModel.achievementMessage = null
            },
            onShowAchievements = {
                viewModel.achievementMessage = null
                GooglePlayGamesManager.showAchievements(activity)
            },
            onViewHighScores = {
                viewModel.achievementMessage = null
                GooglePlayGamesManager.showLeaderboard(activity, leaderboardId)
            }
        )
    }
}

@Composable
fun AchievementDialog(
    message: String,
    onDismiss: () -> Unit,
    onShowAchievements: () -> Unit,
    onViewHighScores: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Achievement Unlocked!",
                fontSize = 28.sp,
                color = Color.White
            )
            Text(
                text = message,
                fontSize = 18.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Back to game")
                }
                Button(onClick = onShowAchievements, modifier = Modifier.weight(1f)) {
                    Text("My Achievements")
                }
            }

            Button(onClick = onViewHighScores) {
                Text("View High Scores")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameOverViewPreview() {
    val mockViewModel = remember {
        GameViewModel().apply {
            currentScore = 42
            previousScore = 35
            achievementMessage = "You unlocked a new achievement!"
        }
    }

    // Mock Activity
    val mockActivity = Activity()

    // Mock leaderboard ID
    val mockLeaderboardId = "dummy_leaderboard_id"

    // Componența GameOverView
    GameOverView(
        viewModel = mockViewModel,
        activity = mockActivity,
        leaderboardId = mockLeaderboardId,
        onClose = { /* No action needed for preview */ }
    )
}
