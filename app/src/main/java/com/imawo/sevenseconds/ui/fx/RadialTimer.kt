package com.imawo.sevenseconds.ui.fx

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.imawo.sevenseconds.core.GameConfig
import com.imawo.sevenseconds.ui.theme.NeonBlue
import com.imawo.sevenseconds.ui.theme.NeonCyan
import com.imawo.sevenseconds.ui.theme.NeonOrange
import com.imawo.sevenseconds.ui.theme.NeonPurple
import com.imawo.sevenseconds.ui.theme.NeonRed
import com.imawo.sevenseconds.ui.theme.SysYellow
import kotlin.math.cos
import kotlin.math.sin

/**
 * Cronometrul radial (semi-cerc) — portul lui RadialTimerView din iOS:
 * gradient unghiular roșu→portocaliu→alb→cyan→albastru→violet, cometă la capăt, puls care se accelerează
 * în ultimele 3 secunde. În plus: bloom real, jar care se desprinde din cometă și un halou roșu de urgență.
 * Progresul e CONTINUU (ceas monoton), nu în trepte de 0.1 s.
 */
@Composable
fun RadialTimer(fx: FxEngine, modifier: Modifier = Modifier) {
    val u = LocalDensity.current.density
    Box(
        modifier
            .size(width = 340.dp, height = 196.dp)
            .onGloballyPositioned { coords ->
                val o = coords.positionInRoot()
                fx.timerCenter = Offset(o.x + coords.size.width / 2f, o.y + coords.size.height - 18f * u)
                fx.timerRadius = 150f * u
            }
    ) {
        if (Build.VERSION.SDK_INT >= 31 && !fx.reduce) {
            Canvas(Modifier.fillMaxSize().blur(14.dp, BlurredEdgeTreatment.Unbounded)) {
                fx.subscribe()
                drawTimer(fx, bloom = true)
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            fx.subscribe()
            drawTimer(fx, bloom = false)
        }
    }
}

private fun DrawScope.drawTimer(fx: FxEngine, bloom: Boolean) {
    val u = density
    val r = 150f * u
    val c = Offset(size.width / 2f, size.height - 18f * u)
    val th = 20f * u
    val p = saturate(fx.timeLeftMs / GameConfig.DURATION_MS.toFloat())
    val topLeft = Offset(c.x - r, c.y - r)
    val box = Size(2f * r, 2f * r)
    val t = fx.timeSec
    val urgent = fx.urgency

    if (!bloom) {
        drawArc(
            color = NeonOrange.copy(alpha = 0.3f), startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = topLeft, size = box, style = Stroke(th, cap = StrokeCap.Round),
        )
    }
    if (p <= 0.002f) return

    // gradientul „curge" ușor pe arc (ca gradientRotation 0→15° pe iOS)
    val shimmer = 0.04f * (0.5f - 0.5f * cos(t * 3.1f))
    val base = 0.5f + shimmer
    val span = 0.5f - shimmer
    val brush = Brush.sweepGradient(
        base to NeonRed,
        base + span * 0.2f to NeonOrange,
        base + span * 0.4f to Color.White,
        base + span * 0.6f to NeonCyan,
        base + span * 0.8f to NeonBlue,
        1.0f to NeonPurple,
        center = c,
    )
    val sweep = 180f * p

    if (bloom) {
        drawArc(
            brush = brush, startAngle = 180f, sweepAngle = sweep, useCenter = false,
            topLeft = topLeft, size = box, alpha = 0.85f, style = Stroke(th * 1.1f, cap = StrokeCap.Round),
        )
        return
    }

    // halou larg (fallback fără blur) + arcul + firul alb
    drawArc(
        brush = brush, startAngle = 180f, sweepAngle = sweep, useCenter = false,
        topLeft = topLeft, size = box, alpha = 0.32f, style = Stroke(th * 1.7f, cap = StrokeCap.Round),
    )
    drawArc(
        brush = brush, startAngle = 180f, sweepAngle = sweep, useCenter = false,
        topLeft = topLeft, size = box, style = Stroke(th, cap = StrokeCap.Round),
    )
    drawArc(
        color = Color.White.copy(alpha = 0.4f), startAngle = 180f, sweepAngle = sweep, useCenter = false,
        topLeft = topLeft, size = box, style = Stroke(3f * u, cap = StrokeCap.Round),
    )
    if (urgent > 0.01f) {
        val beat = 0.6f + 0.4f * sin(t * TAU * 2.2f)
        drawArc(
            color = NeonRed.copy(alpha = 0.45f * urgent * beat), startAngle = 180f, sweepAngle = sweep,
            useCenter = false, topLeft = topLeft, size = box,
            style = Stroke(th * 1.35f, cap = StrokeCap.Round), blendMode = BlendMode.Plus,
        )
    }

    // cometa
    val ang = Math.toRadians((180f + 180f * p).toDouble()).toFloat()
    val h = Offset(c.x + r * cos(ang), c.y + r * sin(ang))
    val period = if (urgent > 0.01f) 0.45f else 0.8f
    val beat = 0.5f + 0.5f * sin(t * TAU / period)
    drawCircle(
        Brush.radialGradient(listOf(NeonRed.copy(alpha = 0.5f + 0.3f * beat), Color.Transparent), center = h, radius = 64f * u),
        radius = 64f * u, center = h, blendMode = BlendMode.Plus,
    )
    drawCircle(
        Brush.radialGradient(listOf(NeonOrange.copy(alpha = 0.55f), Color.Transparent), center = h, radius = 48f * u),
        radius = 48f * u, center = h, blendMode = BlendMode.Plus,
    )
    //val cr = 18f * u * (1f + 0.25f * beat)
    val cr = 26f * u * (1f + 0.25f * beat)
    drawCircle(
        Brush.radialGradient(listOf(Color.White, SysYellow, NeonRed, Color.Transparent), center = h, radius = cr),
        radius = cr, center = h,
    )
}
