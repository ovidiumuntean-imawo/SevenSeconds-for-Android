package com.imawo.sevenseconds.ui.fx

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.imawo.sevenseconds.ui.theme.GameFont
import com.imawo.sevenseconds.ui.theme.NeonCyan
import com.imawo.sevenseconds.ui.theme.SysYellow
import kotlin.math.exp

/** Stratul de deasupra HUD-ului: scântei, cioburi, „+1", bannere de rank și flash-ul final. */
@Composable
fun FxOverlay(fx: FxEngine, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val plusStyle = remember {
        TextStyle(fontFamily = GameFont, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp)
    }
    val bannerStyle = remember {
        TextStyle(fontFamily = GameFont, fontWeight = FontWeight.Black, fontSize = 30.sp, letterSpacing = 3.sp)
    }
    val cache = remember { HashMap<String, TextLayoutResult>() }

    Canvas(modifier.fillMaxSize()) {
        fx.subscribe()
        val u = density

        // Ripple-ul real = distorsiunea fundalului (Backdrop, AGSL) — fără cercuri de contur.
        // Doar pe dispozitive fără AGSL (Android 12-) desenăm o undă moale, fără contur, ca ripple-ul să existe totuși.
        if (!fx.reduce && !fx.rippleViaShader()) {
            for (w in 0 until 4) {
                val age = fx.timeSec - fx.shock.t0[w]
                if (age < 0f || age > 1.1f) continue
                val r = age * 620f * u
                val band = 46f * u
                val outer = r + band
                val a = (exp(-age * 2.6f) * fx.shock.power[w] * 0.18f).coerceIn(0f, 1f)
                val origin = Offset(fx.shock.ox[w], fx.shock.oy[w])
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to Color.Transparent,
                        ((r - band) / outer).coerceAtLeast(0f) to Color.Transparent,
                        r / outer to NeonCyan.copy(alpha = a),
                        1f to Color.Transparent,
                        center = origin, radius = outer,
                    ),
                    radius = outer, center = origin, blendMode = BlendMode.Plus,
                )
            }
        }

        drawParticles(fx.sparks)

        val fl = fx.floaters
        for (i in 0 until fl.count) {
            val str = fl.text[i] ?: continue
            val q = fl.age[i]
            if (fl.kind[i] == F_PLUS) {
                val layout = cache.getOrPut("p:$str") { measurer.measure(str, plusStyle) }
                val e = easeOutCubic(q)
                val cx = fl.x[i]
                val cy = mix(fl.y0[i], fl.y1[i], e)
                val s = mix(0.5f, 1.5f, e)
                val h = fl.heat[i]
                val col = if (h < 0.5f) mixColor(Color.White, NeonCyan, h / 0.5f)
                else mixColor(NeonCyan, SysYellow, (h - 0.5f) / 0.5f)
                scale(s, Offset(cx, cy)) {
                    drawText(
                        layout, color = col,
                        topLeft = Offset(cx - layout.size.width / 2f, cy - layout.size.height / 2f),
                        alpha = 1f - smoothstep(0.35f, 1f, q),
                        shadow = Shadow(Color.Black.copy(alpha = 0.6f), Offset(0f, 2f * u), 4f * u),
                    )
                }
            } else {
                val layout = cache.getOrPut("b:$str") { measurer.measure(str, bannerStyle) }
                val pop = when {
                    q < 0.12f -> mix(0.5f, 1.18f, q / 0.12f)
                    q < 0.26f -> mix(1.18f, 1f, (q - 0.12f) / 0.14f)
                    else -> 1f
                }
                val alpha = if (q < 0.7f) 1f else 1f - (q - 0.7f) / 0.3f
                val cx = fl.x[i]
                val cy = fl.y0[i]
                scale(pop, Offset(cx, cy)) {
                    drawText(
                        layout, color = SysYellow,
                        topLeft = Offset(cx - layout.size.width / 2f, cy - layout.size.height / 2f),
                        alpha = alpha,
                        shadow = Shadow(SysYellow.copy(alpha = 0.9f), Offset.Zero, 24f * u),
                    )
                }
            }
        }

        if (fx.flash > 0.01f) {
            drawRect(Color.White.copy(alpha = (fx.flash * 0.85f).coerceIn(0f, 1f)))
        }
    }
}