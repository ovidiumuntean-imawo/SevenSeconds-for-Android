package com.imawo.sevenseconds.ui.fx

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as lerpColor

internal const val TAU = 6.2831855f

internal fun mix(a: Float, b: Float, t: Float): Float = a + (b - a) * t

internal fun saturate(x: Float): Float = if (x < 0f) 0f else if (x > 1f) 1f else x

internal fun smoothstep(e0: Float, e1: Float, x: Float): Float {
    val t = saturate((x - e0) / (e1 - e0))
    return t * t * (3f - 2f * t)
}

internal fun easeOutCubic(t: Float): Float {
    val u = 1f - saturate(t)
    return 1f - u * u * u
}

internal fun mixColor(a: Color, b: Color, t: Float): Color = lerpColor(a, b, saturate(t))
