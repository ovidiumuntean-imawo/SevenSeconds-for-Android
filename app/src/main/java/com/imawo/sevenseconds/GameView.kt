package com.imawo.sevenseconds

import ButtonParticleView
import android.app.Activity
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imawo.sevenseconds.GameViewModel

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

    // Coordonatele butonului
    var buttonX by remember { mutableStateOf(0f) }
    var buttonY by remember { mutableStateOf(0f) }

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

                // Coordonatele butonului
                var buttonX by remember { mutableStateOf(0f) }
                var buttonY by remember { mutableStateOf(0f) }

                Box(
                    modifier = Modifier
                        .size(171.dp)
                        .scale(if (isPressed) 0.98f else 1.0f)
                        .onGloballyPositioned { layoutCoordinates ->
                            val position = layoutCoordinates.positionInRoot()
                            buttonX = position.x + layoutCoordinates.size.width / 2
                            buttonY = position.y + layoutCoordinates.size.height / 2
                        }
                ) {
                    // Efectul particulelor
                    ButtonParticleView(
                        startX = buttonX,
                        startY = buttonY
                    )

                    // Logica butonului
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                if (!viewModel.isGameOver) {
                                    if (!viewModel.isGameRunning) {
                                        viewModel.startGame(context, leaderboardId)
                                    } else {
                                        viewModel.incrementScore()
                                        viewModel.buttonPressed()
                                    }
                                }
                                isPressed = true
                            }
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (isPressed) R.drawable.button_pressed else R.drawable.button_normal
                            ),
                            contentDescription = "Big button",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }
            }

            // Last Score
            Text(
                text = "Last score: ${viewModel.previousScore} hits",
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            // Spacer cu dimensiune controlată (în loc de weight)
            Spacer(modifier = Modifier.height(0.dp)) // Ajustează dimensiunea după preferințe

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

    @Composable
    fun ButtonWithParticles(
        modifier: Modifier = Modifier,
        onButtonClick: () -> Unit
    ) {
        // Poziția centrului butonului
        var buttonCenterX by remember { mutableStateOf(0f) }
        var buttonCenterY by remember { mutableStateOf(0f) }

        Box(modifier = Modifier.fillMaxSize()) {
            // Efect de particule
            ButtonParticleView(
                startX = buttonCenterX,
                startY = buttonCenterY
            )

            // Butonul principal
            Box(
                modifier = modifier
                    .size(120.dp)
                    .onGloballyPositioned { layoutCoordinates ->
                        val position = layoutCoordinates.positionInRoot()
                        buttonCenterX = position.x + layoutCoordinates.size.width / 2
                        buttonCenterY = position.y + layoutCoordinates.size.height / 2
                    }
                    .align(Alignment.Center)
            ) {
                // Butonul rotund cu imagine și animație
                Button(
                    onClick = onButtonClick,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.button_normal), // Înlocuiește cu imaginea corectă
                        contentDescription = "Button",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                shadowElevation = 8f
                                shape = CircleShape
                                clip = true // Asigură-te că totul este rotund
                            }
                    )
                }
            }
        }
    }
}