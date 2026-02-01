package com.imawo.sevenseconds

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun RotatingBackground(
    isAnimating: Boolean,
    modifier: Modifier = Modifier
) {
    // 1. Animația de rotație infinită
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing), // 40 secunde - mișcare lentă, hipnotică
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_angle"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // 2. Imaginea de fundal (Galaxy)
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop, // Umple tot, dar taie marginile
            modifier = Modifier
                .fillMaxSize()
                .scale(2.8f)
                // SECRETUL 2: Blur. Face totul 'soft', ca o nebuloasă.
                // Notă: Blur merge nativ bine pe Android 12+. Pe versiuni vechi e ignorat (fallback elegant).
                .blur(radius = 15.dp)
                .rotate(if (isAnimating) angle else 0f)
        )

        // 3. Stratul de Întuneric (Overlay)
        // Asta e CRUCIAL pentru ca textul alb să fie lizibil.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)) // 50% negru peste poză
        )
    }
}