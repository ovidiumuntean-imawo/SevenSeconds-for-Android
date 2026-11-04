package com.imawo.sevenseconds.ui.fx

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.imawo.sevenseconds.R
import com.imawo.sevenseconds.ui.theme.DeepSpace
import com.imawo.sevenseconds.ui.theme.NeonRed
import kotlin.math.max
import kotlin.math.sin

/**
 * Fundalul viu. Pe iOS e o imagine statică + stardust. Aici, în plus:
 *  - derivă lentă (paralaxă) + „respirație" care urmărește ritmul tastării;
 *  - unde de șoc care DISTORSIONEAZĂ efectiv imaginea (AGSL, Android 13+; pe restul rămân inelele luminoase);
 *  - vignetă pentru lizibilitatea HUD-ului;
 *  - puls roșu pe margini în ultimele 3 secunde.
 */
@Composable
fun Backdrop(fx: FxEngine, modifier: Modifier = Modifier) {
    val shockProvider = remember { createShockwaveProvider() }

    Box(modifier.fillMaxSize().background(DeepSpace)) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    fx.subscribe()
                    renderEffect = if (fx.reduce) null else shockProvider?.effect(fx.shock, fx.timeSec, density)
                }
        ) {
            Image(
                painter = painterResource(R.drawable.bg_cosmic),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        fx.subscribe()
                        val s = 1.14f + 0.012f * fx.heat + 0.010f * fx.pulse
                        scaleX = s
                        scaleY = s
                        translationX = sin(fx.timeSec * 0.21f) * 24f * density
                        translationY = sin(fx.timeSec * 0.13f + 1.3f) * 30f * density
                    },
            )
        }

        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            fx.subscribe()
            drawStars(fx.stars, fx.timeSec, fx.heat)

            val rad = max(size.width, size.height) * 0.78f
            drawRect(
                Brush.radialGradient(
                    0.0f to Color.Transparent,
                    0.55f to Color.Transparent,
                    1.0f to Color.Black.copy(alpha = 0.72f),
                    center = center, radius = rad,
                )
            )
            val urg = fx.urgency
            if (urg > 0.01f) {
                val beat = 0.65f + 0.35f * sin(fx.timeSec * TAU * 1.8f)
                drawRect(
                    Brush.radialGradient(
                        0.0f to Color.Transparent,
                        0.5f to Color.Transparent,
                        1.0f to NeonRed.copy(alpha = 0.42f * urg * beat),
                        center = center, radius = rad,
                    )
                )
            }
        }
    }
}
