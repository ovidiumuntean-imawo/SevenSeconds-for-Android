package com.imawo.sevenseconds

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameOverView(
    viewModel: GameViewModel,
    activity: Activity,
    leaderboardId: String,
    onClose: () -> Unit
) {
    // Stări pentru animația de intrare (Pop-up effect)
    var scaleGameOver by remember { mutableStateOf(0.5f) }
    var scaleScore by remember { mutableStateOf(0.5f) }

    // Animație elastică pentru text
    val animatedScaleGameOver by animateFloatAsState(
        targetValue = scaleGameOver,
        animationSpec = tween(durationMillis = 800), // Puțin mai rapid
        label = "gameOverScale"
    )

    val animatedScaleScore by animateFloatAsState(
        targetValue = scaleScore,
        animationSpec = tween(durationMillis = 800),
        label = "scoreScale"
    )

    LaunchedEffect(Unit) {
        scaleGameOver = 1f
        scaleScore = 1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // NU FACEM NIMIC AICI!
                // Asta e șmecheria: prindem click-ul și îl ignorăm.
                // Astfel, nu se duce comanda "Play Again" decât dacă apeși butonul corect.
            }
    ) {
        // Background - Folosim același RotatingBackground reparat (cel cu scale 1.5)
        // Atenție: Dacă RotatingBackground e în GameView.kt, el trebuie să fie accesibil aici.
        RotatingBackground(
            isAnimating = false, // Pe Game Over stă pe loc sau se mișcă încet, cum preferi
            modifier = Modifier.matchParentSize()
        )

        // Particule de fundal
        ParticleView(
            isActive = true,
            particleCount = 100 // Mai puține ca în joc, să nu distragă
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // "GAME OVER" Text - Mare și Bold
            Text(
                text = "GAME OVER",
                fontSize = 54.sp, // Ajustat să încapă bine
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.scale(animatedScaleGameOver)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Score Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "YOU SCORED",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = "${viewModel.currentScore}",
                    fontSize = 80.sp, // Gigantic
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .scale(animatedScaleScore)
                        .padding(vertical = 8.dp)
                )
                Text(
                    text = "TAPS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Butoane de acțiune
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Play Again - Buton Verde (ca în iOS)
                Button(
                    onClick = {
                        viewModel.previousScore = viewModel.currentScore
                        viewModel.resetGame()
                        onClose() // Asta resetează particulele din GameView
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CD964)) // Verde iOS
                ) {
                    Text("Play Again", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                // View High Scores - Buton Albastru/Transparent
                Button(
                    onClick = {
                        GooglePlayGamesManager.showLeaderboard(activity, leaderboardId)
                    },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("View High Scores")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Facebook Group Section (Mic jos)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Share your best screenshots!",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                androidx.compose.foundation.text.ClickableText(
                    text = androidx.compose.ui.text.AnnotatedString("Visit our FB Group"),
                    style = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFF1877F2), // Facebook Blue
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    onClick = {
                        val url = "https://facebook.com/groups/sevensecondschampions"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        activity.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
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
        onClose = { }
    )
}

/*
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
            .padding(0.dp)
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

            Spacer(modifier = Modifier.weight(1f))

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

            Spacer(modifier = Modifier.weight(1f))

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
    */
/*viewModel.achievementMessage?.let { message ->
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
    }*//*

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
        onClose = { */
/* No action needed for preview *//*
 }
    )
}
*/
