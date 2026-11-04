package com.imawo.sevenseconds.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.imawo.sevenseconds.R

/**
 * Fontul jocului. Pe iOS se folosește SF Rounded (font de sistem Apple, nu poate fi folosit pe Android).
 * Poppins e inclus în aplicație ca tipografia să fie IDENTICĂ pe toate telefoanele (Samsung/Xiaomi/Pixel…).
 * Vrei look rotunjit? Înlocuiește cele două fișiere din res/font cu Nunito/Fredoka și ajustezi doar R.font.* de aici.
 */
val GameFont = FontFamily(
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_bold, FontWeight.ExtraBold),
    Font(R.font.poppins_bold, FontWeight.Black),
)
