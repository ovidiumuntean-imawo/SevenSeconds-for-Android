package com.imawo.sevenseconds.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun SevenSecondsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = NeonCyan,
            onPrimary = Color.Black,
            secondary = NeonPurple,
            tertiary = NeonOrange,
            background = DeepSpace,
            onBackground = Color.White,
            surface = DeepSpace,
            onSurface = Color.White,
        )
    ) {
        ProvideTextStyle(TextStyle(fontFamily = GameFont, color = Color.White), content)
    }
}
