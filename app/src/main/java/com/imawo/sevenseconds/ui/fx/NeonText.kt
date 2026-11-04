package com.imawo.sevenseconds.ui.fx

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

/** Text cu strălucire neon pe două straturi (aura largă + miezul), ca `.shadow(color:radius:)` dublu din SwiftUI. */
@Composable
fun NeonText(
    text: String,
    color: Color,
    glow: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Black,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    glowRadius: Float = 14f,
) {
    val px = LocalDensity.current.density
    Box(modifier) {
        Text(
            text = text, color = color, fontSize = fontSize, fontWeight = fontWeight,
            letterSpacing = letterSpacing, textAlign = textAlign, lineHeight = lineHeight,
            style = TextStyle(shadow = Shadow(glow.copy(alpha = 0.55f), Offset.Zero, glowRadius * 2.4f * px)),
        )
        Text(
            text = text, color = color, fontSize = fontSize, fontWeight = fontWeight,
            letterSpacing = letterSpacing, textAlign = textAlign, lineHeight = lineHeight,
            style = TextStyle(shadow = Shadow(glow.copy(alpha = 0.95f), Offset.Zero, glowRadius * px)),
        )
    }
}
