package com.example.testbioprocessor.ui.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.navigation.compose.rememberNavController
import com.example.testbioprocessor.ui.theme.Blue20
import com.example.testbioprocessor.ui.theme.White
import com.example.testbioprocessor.viewModel.BioViewModelNew

@Composable
fun AppScaffold(
    showBottomBar: Boolean = true,
    model: BioViewModelNew,
    content: @Composable (PaddingValues) -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            AppTopBar()
        },
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(model)
            }
        },
        containerColor = Blue20 // Фон приложения
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Blue20, White)
                    )
                )
        ) {
            content(paddingValues)
        }
    }
}
