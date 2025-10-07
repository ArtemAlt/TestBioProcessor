package com.example.testbioprocessor.ui.custom

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.testbioprocessor.ui.AppFonts
import com.example.testbioprocessor.ui.theme.Blue80
import com.example.testbioprocessor.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Тестирование",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = AppFonts.customFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Blue80,
            titleContentColor = White
        ),
        modifier = Modifier
            .height(72.dp) // Стандартная высота для более узкого TopBar
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                clip = true
            )
    )
}