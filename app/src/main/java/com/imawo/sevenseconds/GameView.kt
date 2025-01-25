package com.imawo.sevenseconds

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameView(
    activity: Activity? = null,
    viewModel: GameViewModel,
    leaderboardId: String = "seven.seconds.leaderboard"
) {
    val context = LocalContext.current

    // States for animations and particles
    var isAnimationActive by remember { mutableStateOf(false) }
    var areParticlesActive by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        RotatingBackground(
            isAnimating = isAnimationActive,
            modifier = Modifier.matchParentSize()
        )

        // Particle Effect
        ParticleView(
            isActive = areParticlesActive,
            particleCount = 120
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Title Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "7",
                        fontSize = 96.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "seconds",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White
                    )
                }
                Text(
                    text = "CHAMPIONS",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Subtitle
            Text(
                text = "Hit that button as fast as you can!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Timer
            Text(
                text = "Time left: ${viewModel.timeLeft} seconds",
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    viewModel.timeLeft > 5 -> Color.White
                    viewModel.timeLeft > 3 -> Color.Yellow
                    viewModel.timeLeft > 2 -> Color(0xFFFFA500) // Orange
                    else -> Color.Red
                }
            )

            // Game Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Score Section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "YOUR SCORE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = "${viewModel.currentScore}",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "HITS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                // Big Button with Particles
                var isPressed by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .size(171.dp)
                        .scale(if (isPressed) 0.98f else 1.0f)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    if (!viewModel.isGameOver) {
                                        if (!viewModel.isGameRunning) {
                                            viewModel.startGame(context, leaderboardId)
                                        } else {
                                            viewModel.incrementScore()
                                            viewModel.buttonPressed()
                                        }
                                    }

                                    // Starea de apăsare activată
                                    isPressed = true
                                    tryAwaitRelease() // Așteaptă până când utilizatorul eliberează butonul
                                    // Starea de apăsare dezactivată
                                    isPressed = false
                                }
                            )
                        }
                ) {
                    // Determinarea imaginii butonului
                    val buttonImage = remember(viewModel.timeLeft, isPressed) {
                        getButtonImage(viewModel.timeLeft, isPressed)
                    }

                    Image(
                        painter = painterResource(id = buttonImage),
                        contentDescription = "Game button",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
            }

            // Last Score
            Text(
                text = "Last score: ${viewModel.previousScore} hits",
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            Spacer(modifier = Modifier.weight(1f))

            // Leaderboard Section
            AnimatedVisibility(
                visible = !viewModel.isGameRunning,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "How other players are doing?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        GooglePlayGamesManager.showLeaderboard(activity!!, leaderboardId)
                    }) {
                        Text("View high scores")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))
        }

        // Game Over Overlay
        if (viewModel.isGameOver) {
            GameOverView(
                viewModel = viewModel,
                activity = activity!!,
                leaderboardId = leaderboardId
            ) {
                isAnimationActive = false
            }
        }
    }
}

// Determină imaginea butonului în funcție de timpul rămas și dacă este apăsat
fun getButtonImage(timeLeft: Int, isPressed: Boolean): Int {
    return when {
        timeLeft > 5 -> if (isPressed) R.drawable.button_pressed else R.drawable.button_normal
        timeLeft in 3..5 -> if (isPressed) R.drawable.button_pressed_0 else R.drawable.button_normal_0
        timeLeft <= 3 -> if (isPressed) R.drawable.button_pressed_2 else R.drawable.button_normal_2
        else -> R.drawable.button_normal // Default fallback
    }
}

@Preview(showBackground = true)
@Composable
fun GameViewPreview() {
    // Mock ViewModel
    val mockViewModel = remember {
        GameViewModel().apply {
            timeLeft = 10
            currentScore = 42
            previousScore = 35
        }
    }

    GameView(
        activity = null, // Activitate nulă pentru previzualizare
        viewModel = mockViewModel,
        leaderboardId = "dummy_leaderboard_id"
    )
}