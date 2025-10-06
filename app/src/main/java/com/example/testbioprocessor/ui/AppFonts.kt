package com.example.testbioprocessor.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.testbioprocessor.R

object AppFonts {
    val customFontFamily = FontFamily(
        Font(R.font.bitter_regular, FontWeight.Normal),
        Font(R.font.bitter_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.bitter_bold, FontWeight.Bold),
        Font(R.font.bitter_bolditalic, FontWeight.Bold, FontStyle.Italic)
    )
}
