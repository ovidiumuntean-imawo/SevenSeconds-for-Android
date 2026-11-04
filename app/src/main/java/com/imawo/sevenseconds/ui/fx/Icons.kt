package com.imawo.sevenseconds.ui.fx

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// Pictograme desenate (echivalent SF Symbols) — fără dependența material-icons-extended.

@Composable
fun TrophyIcon(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier.size(size)) {
        val k = this.size.minDimension / 24f
        val cup = Path().apply {
            moveTo(7f * k, 3f * k); lineTo(17f * k, 3f * k); lineTo(17f * k, 9.5f * k)
            lineTo(15.4f * k, 12.6f * k); lineTo(13f * k, 14f * k); lineTo(11f * k, 14f * k)
            lineTo(8.6f * k, 12.6f * k); lineTo(7f * k, 9.5f * k); close()
        }
        drawPath(cup, color)
        val st = Stroke(1.9f * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val left = Path().apply {
            moveTo(7f * k, 5f * k); lineTo(4.2f * k, 5f * k); lineTo(4.2f * k, 8f * k); lineTo(7.6f * k, 10.6f * k)
        }
        val right = Path().apply {
            moveTo(17f * k, 5f * k); lineTo(19.8f * k, 5f * k); lineTo(19.8f * k, 8f * k); lineTo(16.4f * k, 10.6f * k)
        }
        drawPath(left, color, style = st)
        drawPath(right, color, style = st)
        drawRect(color, topLeft = Offset(11f * k, 14f * k), size = Size(2f * k, 3.2f * k))
        drawRoundRect(color, topLeft = Offset(8f * k, 17.2f * k), size = Size(8f * k, 2.8f * k), cornerRadius = CornerRadius(1.2f * k))
    }
}

@Composable
fun ShareIcon(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier.size(size)) {
        val k = this.size.minDimension / 24f
        val a = Offset(18f * k, 5f * k)
        val b = Offset(6f * k, 12f * k)
        val c = Offset(18f * k, 19f * k)
        val st = Stroke(1.7f * k, cap = StrokeCap.Round)
        drawLine(color, b, a, strokeWidth = st.width, cap = StrokeCap.Round)
        drawLine(color, b, c, strokeWidth = st.width, cap = StrokeCap.Round)
        drawCircle(color, radius = 3.1f * k, center = a)
        drawCircle(color, radius = 3.1f * k, center = b)
        drawCircle(color, radius = 3.1f * k, center = c)
    }
}

@Composable
fun WarningIcon(color: Color, mark: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier.size(size)) {
        val k = this.size.minDimension / 24f
        val tri = Path().apply {
            moveTo(12f * k, 2.8f * k); lineTo(22.4f * k, 20.4f * k); lineTo(1.6f * k, 20.4f * k); close()
        }
        drawPath(tri, color, style = Stroke(2.2f * k, join = StrokeJoin.Round))
        drawPath(tri, color)
        drawLine(mark, Offset(12f * k, 9f * k), Offset(12f * k, 14f * k), strokeWidth = 2.2f * k, cap = StrokeCap.Round)
        drawCircle(mark, radius = 1.25f * k, center = Offset(12f * k, 17.2f * k))
    }
}

@Composable
fun GearIcon(color: Color, modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Canvas(modifier.size(size)) {
        val k = this.size.minDimension / 24f
        val c = center
        drawCircle(color, radius = 5.2f * k, center = c, style = Stroke(2.4f * k))
        for (i in 0 until 8) {
            val a = i * 0.7853982f
            drawLine(
                color,
                Offset(c.x + cos(a) * 7.8f * k, c.y + sin(a) * 7.8f * k),
                Offset(c.x + cos(a) * 10.2f * k, c.y + sin(a) * 10.2f * k),
                strokeWidth = 3.2f * k, cap = StrokeCap.Round,
            )
        }
    }
}
