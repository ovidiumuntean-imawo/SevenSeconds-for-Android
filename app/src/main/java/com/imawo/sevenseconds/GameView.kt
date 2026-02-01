package com.imawo.sevenseconds

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresPermission
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// Data class pentru punctele care zboară (+1)
data class FloatingPoint(
    val id: Long,
    val startX: Float = 0f, // Relativ la centrul butonului
    val startY: Float = 0f
)

// Data class pentru particule (Explozie)
data class BurstParticle(
    val id: Long,
    val angle: Double,
    val speed: Float,
    val color: Color,
    val startX: Float,
    val startY: Float,
    val size: Float,
    val createdAt: Long = System.currentTimeMillis()
)

@Composable
fun GameView(
    activity: Activity? = null,
    viewModel: GameViewModel,
    leaderboardId: String = "seven.seconds.leaderboard"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- State-uri pentru efecte ---
    var isAnimationActive by remember { mutableStateOf(false) }
    var areParticlesActive by remember { mutableStateOf(true) }

    // Screen Shake State
    val shakeOffsetX = remember { Animatable(0f) }
    val shakeOffsetY = remember { Animatable(0f) }

    // Floating Points (+1) List
    val floatingPoints = remember { mutableStateListOf<FloatingPoint>() }

    // Burst Particles List
    val burstParticles = remember { mutableStateListOf<BurstParticle>() }

    // Funcție pentru Screen Shake (Tremurat)
    fun triggerShake() {
        scope.launch {
            // Tremură rapid de câteva ori
            repeat(3) {
                val x = Random.nextFloat() * 15f - 7.5f
                val y = Random.nextFloat() * 15f - 7.5f
                launch { shakeOffsetX.animateTo(x, animationSpec = tween(50)) }
                launch { shakeOffsetY.animateTo(y, animationSpec = tween(50)) }
                delay(50)
            }
            // Revenire la 0
            launch { shakeOffsetX.animateTo(0f, tween(100)) }
            launch { shakeOffsetY.animateTo(0f, tween(100)) }
        }
    }

    // Funcție pentru vibrație haptică
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun triggerHaptic() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Rotativ
        RotatingBackground(
            isAnimating = isAnimationActive,
            modifier = Modifier.matchParentSize()
        )

        // Particule Fundal (Generic)
        ParticleView(
            isActive = areParticlesActive,
            particleCount = 120
        )

        // Containerul Principal cu efect de Shake aplicat pe TOT conținutul
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = shakeOffsetX.value.roundToInt(),
                        y = shakeOffsetY.value.roundToInt()
                    )
                }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp), // Păstrăm spacing fix
            horizontalAlignment = Alignment.Start
        ) {
            // --- Title Section ---
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
                    color = Color.White,
                    modifier = Modifier.offset(y = (-10).dp) // Ajustare fină vizuală
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // --- Subtitle ---
            Text(
                text = "Hit that button as fast as you can!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // --- Timer ---
            // Folosim un Box cu lățime fixă sau widthIn ca să nu sară textul când se schimbă secundele
            Box(modifier = Modifier.fillMaxWidth()) {
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
            }

            // --- Game Section (Score + Button) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score Section (Left)
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.width(120.dp) // Lățime fixă să nu împingă butonul
                ) {
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
                        color = Color.White,
                        lineHeight = 64.sp // Fix line height
                    )
                    Text(
                        text = "TAPS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                // --- The Big Button (Right) ---
                var isPressed by remember { mutableStateOf(false) }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(190.dp) // Container fix, puțin mai mare pentru particule
                ) {
                    // 1. Stratul de particule (Explozie) - sub buton
                    BurstParticlesEffect(particles = burstParticles)

                    // 2. Butonul efectiv
                    Box(
                        modifier = Modifier
                            .size(170.dp) // Dimensiunea butonului
                            .scale(if (isPressed) 0.95f else 1.0f) // Scale la apăsare
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        // Start Press Logic
                                        isPressed = true

                                        if (!viewModel.isGameRunning) {
                                            viewModel.startGame(context, leaderboardId)
                                            isAnimationActive = true
                                        } else {
                                            viewModel.incrementScore()
                                            viewModel.buttonPressed()

                                            // Trigger Effects
                                            triggerShake()
                                            triggerHaptic()

                                            // Add Floating +1
                                            floatingPoints.add(FloatingPoint(id = System.nanoTime()))

                                            // --- PARTICULE MASTER ANDROID ---
                                            // Generăm 30 de particule per tap (înainte erau 8)
                                            repeat(30) {
                                                val angle = Random.nextDouble() * 360.0

                                                // Viteză variabilă: unele explodează tare, altele rămân aproape
                                                val speed = Random.nextFloat() * 15f + 5f

                                                // Culori "nebune": Galben, Alb, Cyan, și un pic de Portocaliu
                                                val color = listOf(
                                                    Color(0xFFFFD700), // Gold
                                                    Color.White,
                                                    Color.Cyan,
                                                    Color(0xFFFF4500)  // Orange Red
                                                ).random()

                                                // Poziție de start ușor aleatoare sub buton (nu toate fix din centru)
                                                val startOffsetX = Random.nextFloat() * 40f - 20f
                                                val startOffsetY = Random.nextFloat() * 40f - 20f

                                                burstParticles.add(
                                                    BurstParticle(
                                                        id = System.nanoTime() + it,
                                                        angle = angle,
                                                        speed = speed,
                                                        color = color,
                                                        startX = startOffsetX,
                                                        startY = startOffsetY,
                                                        // Adăugăm și o dimensiune random (unele mari, unele mici)
                                                        size = Random.nextFloat() * 8f + 4f
                                                    )
                                                )
                                            }
                                        }

                                        /*if (!viewModel.isGameOver) {
                                            if (!viewModel.isGameRunning) {
                                                viewModel.startGame(context, leaderboardId)
                                                isAnimationActive = true
                                            } else {
                                                viewModel.incrementScore()
                                                viewModel.buttonPressed()

                                                // Trigger Effects
                                                triggerShake()
                                                triggerHaptic()

                                                // Add Floating +1
                                                floatingPoints.add(FloatingPoint(id = System.nanoTime()))

                                                // Add Burst Particles
                                                repeat(8) {
                                                    val angle = Random.nextDouble() * 360.0
                                                    val speed = Random.nextFloat() * 10f + 5f
                                                    val color = listOf(Color.Yellow, Color.White, Color.Cyan).random()
                                                    burstParticles.add(
                                                        BurstParticle(
                                                            id = System.nanoTime() + it,
                                                            angle = angle,
                                                            speed = speed,
                                                            color = color,
                                                            startX = 0f,
                                                            startY = 0f
                                                        )
                                                    )
                                                }
                                            }
                                        }*/

                                        tryAwaitRelease()
                                        // End Press Logic
                                        isPressed = false
                                    }
                                )
                            }
                    ) {
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

                    // 3. Floating Points (+1) - deasupra butonului
                    // Renderizăm fiecare "+1" din listă
                    floatingPoints.forEach { point ->
                        key(point.id) {
                            FloatingOneEffect(
                                onAnimationEnd = {
                                    floatingPoints.remove(point)
                                }
                            )
                        }
                    }
                }
            }

            // --- Last Score ---
            Text(
                text = "Last score: ${viewModel.previousScore} taps",
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- Leaderboard Section ---
            // Aici e FIX-UL pentru layout shift: folosim Alpha, nu AnimatedVisibility
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (!viewModel.isGameRunning) 1f else 0f) // Doar invizibil, ocupă spațiu
            ) {
                Text(
                    text = "How other players are doing?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        // Prevenim click-ul dacă e invizibil (jocul merge)
                        if (!viewModel.isGameRunning) {
                            GooglePlayGamesManager.showLeaderboard(activity!!, leaderboardId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("View high scores", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))
        }

        // --- Game Over Overlay ---
        if (viewModel.isGameOver) {
            GameOverView(
                viewModel = viewModel,
                activity = activity!!,
                leaderboardId = leaderboardId
            ) {
                isAnimationActive = false
                floatingPoints.clear() // Curățăm ecranul
                burstParticles.clear()
            }
        }
    }
}

// --- Composable: Efectul vizual pentru "+1" ---
@Composable
fun FloatingOneEffect(onAnimationEnd: () -> Unit) {
    // Animăm offset-ul pe Y (să urce)
    val yOffset = remember { Animatable(0f) }
    // Animăm opacitatea (să dispară)
    val opacity = remember { Animatable(1f) }
    // Animăm scala (să se mărească)
    val scale = remember { Animatable(0.5f) }

    // Poziție random mică pe X ca să pară organic
    val randomX = remember { Random.nextFloat() * 40f - 20f }

    LaunchedEffect(Unit) {
        launch {
            yOffset.animateTo(-250f, animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing))
        }
        launch {
            scale.animateTo(1.5f, animationSpec = tween(durationMillis = 800))
        }
        launch {
            delay(400) // Stă vizibil puțin, apoi dispare
            opacity.animateTo(0f, animationSpec = tween(durationMillis = 400))
        }
        delay(800)
        onAnimationEnd()
    }

    Text(
        text = "+1",
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        modifier = Modifier
            .offset(x = randomX.dp, y = yOffset.value.dp)
            .scale(scale.value)
            .alpha(opacity.value)
    )
}

// --- Composable: Efectul de Particule Burst (Explozie) ---
@Composable
fun BurstParticlesEffect(particles: MutableList<BurstParticle>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()

            // Timpul de viață
            val age = System.currentTimeMillis() - particle.createdAt
            val maxAge = 800L // Le lăsăm puțin mai mult (800ms)

            if (age > maxAge) {
                iterator.remove()
                continue
            }

            val progress = age / maxAge.toFloat()
            // Alpha: fade out spre final
            val alpha = (1f - progress).coerceIn(0f, 1f)

            // Fizica: Explodează repede, apoi încetinesc puțin (drag effect)
            val moveProgress = 1f - Math.pow((1.0 - progress), 3.0).toFloat() // EaseOutCubic
            val distance = particle.speed * 25f * progress // Distanța totală

            val currentX = particle.startX + (cos(Math.toRadians(particle.angle)) * distance).toFloat()
            val currentY = particle.startY + (sin(Math.toRadians(particle.angle)) * distance).toFloat()

            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size * (1f - progress), // Se micșorează în timp
                center = center + Offset(currentX, currentY)
            )
        }
    }

    if (particles.isNotEmpty()) {
        LaunchedEffect(Unit) {
            while(particles.isNotEmpty()) {
                withFrameNanos { }
            }
        }
    }
}

/*@Composable
fun BurstParticlesEffect(particles: MutableList<BurstParticle>) {
    // Un Canvas care desenează toate particulele active
    Canvas(modifier = Modifier.fillMaxSize()) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()

            // Calculăm noua poziție bazată pe timp
            val age = System.currentTimeMillis() - particle.createdAt
            val maxAge = 600L // Particula trăiește 600ms

            if (age > maxAge) {
                iterator.remove()
                continue
            }

            // Progresul animației (0.0 -> 1.0)
            val progress = age / maxAge.toFloat()
            val alpha = 1f - progress // Dispare spre final

            // Calcul fizic simplu
            val distance = particle.speed * age / 10f // Distanța parcursă
            val currentX = (cos(Math.toRadians(particle.angle)) * distance).toFloat()
            val currentY = (sin(Math.toRadians(particle.angle)) * distance).toFloat()

            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = 6f * (1f - progress), // Se micșorează în timp
                center = center + Offset(currentX, currentY)
            )
        }
    }

    // Forțăm redesenarea continuă cât timp sunt particule
    if (particles.isNotEmpty()) {
        LaunchedEffect(Unit) {
            while(particles.isNotEmpty()) {
                withFrameNanos { }
            }
        }
    }
}*/

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