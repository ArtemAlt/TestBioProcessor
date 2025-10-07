package com.example.testbioprocessor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.testbioprocessor.ui.custom.AppScaffold
import com.example.testbioprocessor.viewModel.ApiUiState
import com.example.testbioprocessor.viewModel.BioViewModelNew

@Composable
fun DeleteScreen(
    navController: NavHostController,
    viewModel: BioViewModelNew,
) {
    val apiState by viewModel.uiApiState.collectAsStateWithLifecycle()
    var showConfirmationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(apiState) {
        when (apiState) {
            is ApiUiState.Success -> {
                navController.navigate("main") {
                    popUpTo("deleteScreen") { inclusive = true }
                }
                viewModel.resetApiState()
            }
            is ApiUiState.Error -> {
                viewModel.resetApiState()
            }
            else -> {}
        }
    }

    AppScaffold(model = viewModel, showBottomBar = true) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Blue20, White)
                    )
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Сброс данных",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = AppFonts.customFontFamily,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Blue80,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            )

            // Основной контент
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Иконка и заголовок
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = Blue40.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удаление",
                            tint = Blue80,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "Сброс данных пользователя",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = AppFonts.customFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Blue80,
                        textAlign = TextAlign.Center
                    )
                }

                // Описание
                Text(
                    text = "Будет удалена вся информация о текущем пользователе:\n\n• Логин\n• Биовектор\n• Все связанные данные",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = AppFonts.customFontFamily
                    ),
                    color = Blue80.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                // Кнопки
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppButton(
                        onClick = { showConfirmationDialog = true },
                        text = "Сбросить данные",
                        buttonType = AppButtonType.PRIMARY,
                        icon = Icons.Default.Delete,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppButton(
                        onClick = { navController.popBackStack() },
                        text = "Вернуться назад",
                        buttonType = AppButtonType.SECONDARY,
                        icon = Icons.Default.ArrowBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Подтвердите сброс",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AppFonts.customFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Blue80
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите сбросить все данные пользователя? Это действие нельзя отменить.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = AppFonts.customFontFamily
                    ),
                    color = Blue80
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showConfirmationDialog = false }
                    ) {
                        Text(
                            "Отмена",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = AppFonts.customFontFamily
                            ),
                            color = Blue80
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    AppButton(
                        onClick = {
                            viewModel.resetLoginAnVector()
                            showConfirmationDialog = false
                        },
                        text = "Сбросить",
                        buttonType = AppButtonType.PRIMARY,
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        )
    }
}