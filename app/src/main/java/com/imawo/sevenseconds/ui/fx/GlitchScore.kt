package com.imawo.sevenseconds.ui.fx

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imawo.sevenseconds.ui.theme.GameFont
import com.imawo.sevenseconds.ui.theme.NeonBlue
import com.imawo.sevenseconds.ui.theme.NeonCyan
import com.imawo.sevenseconds.ui.theme.NeonRed
import kotlin.math.sin

/**
 * Scorul cu glitch — portul lui GlitchScoreView din iOS.
 * Diferențe: benzile de glitch sunt tăieturi REALE ale textului (clip) decalate pe orizontală, în roșu/cyan;
 * „pop"-ul la fiecare tap e un resort; intensitatea crește cu „heat".
 */
@Composable
fun GlitchScore(score: Int, fx: FxEngine, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val style = remember {
        TextStyle(fontFamily = GameFont, fontWeight = FontWeight.Black, fontSize = 96.sp)
    }
    val layout = remember(score) { measurer.measure(score.toString(), style) }

    Canvas(modifier.size(width = 260.dp, height = 128.dp)) {
        fx.subscribe()
        val u = density
        val cx = size.width / 2f
        val cy = size.height / 2f
        val tw = layout.size.width.toFloat()
        val th = layout.size.height.toFloat()
        val base = Offset(cx - tw / 2f, cy - th / 2f)
        val g = if (fx.reduce) 0f else fx.glitch
        val running = fx.running
        val jitterX = if (running && !fx.reduce) sin(fx.timeSec * 61f) * 1.2f * u else 0f

        scale(1f + 0.16f * fx.scorePop, Offset(cx, cy)) {
            // aură
            drawText(
                layout, color = NeonCyan, topLeft = base, alpha = if (running) 0.30f else 0.12f,
                shadow = Shadow(NeonCyan, Offset.Zero, 30f * u),
            )
            // copie cyan-albastru decalată (aberație cromatică)
            if (running && !fx.reduce) {
                drawText(
                    layout, color = NeonBlue, topLeft = base + Offset(fx.glitchOffset * 0.3f * u, 0f), alpha = 0.4f,
                )
            }
            // benzi de glitch
            if (g > 0.02f) {
                for (b in 0 until 3) {
                    val by = cy + fx.gbY[b] * th
                    val bh = fx.gbH[b] * u
                    val col = if (b % 2 == 0) NeonRed else NeonCyan
                    clipRect(left = 0f, top = by - bh / 2f, right = size.width, bottom = by + bh / 2f) {
                        drawText(layout, color = col, topLeft = base + Offset(fx.gbDx[b] * u, 0f), alpha = g)
                    }
                }
            }
            // textul principal
            drawText(
                layout, color = Color.White, topLeft = base + Offset(jitterX, 0f),
                shadow = Shadow(NeonCyan.copy(alpha = 0.8f), Offset.Zero, 10f * u),
            )
            // linie de scanare
            if (g > 0.4f) {
                val sy = cy + fx.gbY[0] * th * 1.3f
                drawRect(
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.7f), NeonCyan, Color.Transparent)),
                    topLeft = Offset(cx - 100f * u, sy), size = Size(200f * u, 2f * u),
                )
            }
        }
    }
}
