package com.example.testbioprocessor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
                navController.navigate("loginScreen") {
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


    AppScaffold (model = viewModel, showBottomBar = true) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Сбросить данные текущего пользователя\nБудут сброшены логин и биовектор",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            Row {
                AppButton(
                    onClick = { showConfirmationDialog = true },
                    text = "Сбросить",
                    buttonType = AppButtonType.PRIMARY
                )
                Spacer(modifier = Modifier.width(20.dp))
                AppButton(
                    onClick = { navController.navigate("serviceScreen") },
                    text = "Отказаться",
                    buttonType = AppButtonType.SECONDARY
                )
            }
        }
    }
    // Диалог подтверждения сброса
    if (showConfirmationDialog) {
        ResetConfirmationDialog(
            viewModel = viewModel,
            navController = navController,
            onDismiss = { showConfirmationDialog = false }
        )
    }
}

@Composable
fun ResetConfirmationDialog(
    viewModel: BioViewModelNew,
    navController: NavHostController,
    onDismiss: () -> Unit
) {
    val apiState by viewModel.uiApiState.collectAsStateWithLifecycle()

    val info = when (apiState) {
        is ApiUiState.Success -> "Данные пользователя успешно сброшены"
        is ApiUiState.Error -> (apiState as ApiUiState.Error).message
        is ApiUiState.Loading -> "Выполняется сброс данных..."
        else -> "Подтвердите сброс данных пользователя"
    }

    val title = when (apiState) {
        is ApiUiState.Success -> "Успех"
        is ApiUiState.Error -> "Ошибка"
        is ApiUiState.Loading -> "Сброс данных"
        else -> "Подтверждение сброса"
    }

    MaterialTheme {
        val openDialog = remember { mutableStateOf(true) }

        if (openDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDialog.value = false
                    onDismiss()
                },
                title = {
                    Text(title)
                },
                text = {
                    Text(info)
                },
                confirmButton = {
                    when (apiState) {
                        is ApiUiState.Success -> {
                            Button(
                                onClick = {
                                    openDialog.value = false
                                    viewModel.resetApiState()
                                    navController.navigate("serviceScreen")
                                }
                            ) {
                                Text("ОК")
                            }
                        }
                        is ApiUiState.Error -> {
                            Button(
                                onClick = {
                                    openDialog.value = false
                                    viewModel.resetApiState()
                                    onDismiss()
                                }
                            ) {
                                Text("Закрыть")
                            }
                        }
                        is ApiUiState.Loading -> {
                            Button(
                                onClick = { /* Заблокирована во время загрузки */ },
                                enabled = false
                            ) {
                                Row {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Сброс...")
                                }
                            }
                        }
                        else -> {
                            // Состояние по умолчанию - кнопки подтверждения и отмены
                            Row {
                                Button(
                                    onClick = {
                                        openDialog.value = false
                                        onDismiss()
                                    }
                                ) {
                                    Text("Отмена")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.resetLoginAnVector()
                                    }
                                ) {
                                    Text("Сбросить")
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}