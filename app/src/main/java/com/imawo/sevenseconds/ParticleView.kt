package com.imawo.sevenseconds

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun ParticleView(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    particleCount: Int = 40
) {
    if (!isActive) return

    // Obtain screen width and height in pixels
    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

    // Initialize particles
    val particles = remember {
        List(particleCount) {
            ParticleState.random(screenWidthPx, screenHeightPx)
        }
    }

    // Start animations
    particles.forEach { particle ->
        LaunchedEffect(particle) {
            while (isActive) {
                particle.animate(screenWidthPx, screenHeightPx)
            }
        }
    }

    // Draw particles on the Canvas
    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawCircle(
                color = particle.color,
                radius = particle.radius,
                center = Offset(particle.x.value, particle.y.value)
            )
        }
    }
}

data class ParticleState(
    val x: Animatable<Float, AnimationVector1D>,
    val y: Animatable<Float, AnimationVector1D>,
    val vx: Float,
    val vy: Float,
    val radius: Float,
    val color: Color
) {
    suspend fun animate(screenWidth: Float, screenHeight: Float) {
        x.animateTo(
            targetValue = x.value + vx,
            animationSpec = tween(durationMillis = 16)
        )
        y.animateTo(
            targetValue = y.value + vy,
            animationSpec = tween(durationMillis = 16)
        )

        if (isOffScreen(screenWidth, screenHeight)) {
            reset(screenWidth, screenHeight)
        }
    }

    fun isOffScreen(screenWidth: Float, screenHeight: Float): Boolean {
        return x.value < -radius || x.value > screenWidth + radius || y.value > screenHeight + radius
    }

    suspend fun reset(screenWidth: Float, screenHeight: Float) {
        val random = Random.Default
        x.snapTo(random.nextFloat() * screenWidth)
        y.snapTo(-random.nextFloat() * 50f)
    }

    companion object {
        fun random(screenWidth: Float, screenHeight: Float): ParticleState {
            val random = Random.Default
            val startX = random.nextFloat() * screenWidth
            val startY = -random.nextFloat() * 50f
            val velocityX = (random.nextFloat() - 0.5f) * 5f // Reduced for smoother animations
            val velocityY = random.nextFloat() * 10f + 5f
            val radius = random.nextFloat() * 8f + 2f
            val color = Color.White.copy(alpha = random.nextFloat() * 0.5f + 0.5f)

            return ParticleState(
                x = Animatable(startX),
                y = Animatable(startY),
                vx = velocityX,
                vy = velocityY,
                radius = radius,
                color = color
            )
        }
    }
}