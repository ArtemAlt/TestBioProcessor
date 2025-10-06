package com.example.testbioprocessor.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.testbioprocessor.model.SendScreenType
import com.example.testbioprocessor.model.camera.CapturedImage
import com.example.testbioprocessor.ui.custom.AppScaffold
import com.example.testbioprocessor.viewModel.ApiUiState
import com.example.testbioprocessor.viewModel.BioViewModelNew

@Composable
fun SendScreenNew(
    navController: NavHostController,
    model: BioViewModelNew,
    screenType: SendScreenType = SendScreenType.RECOGNITION
) {
    val captureState by model.imagesState.collectAsStateWithLifecycle()
    val apiState by model.uiApiState.collectAsStateWithLifecycle()
    var showUploadDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf<ApiUiState?>(null) }

    // Обработка состояний API для показа диалога результата
    LaunchedEffect(apiState) {
        when (apiState) {
            is ApiUiState.Success,
            is ApiUiState.RecognitionSuccess,
            is ApiUiState.RegistrationSuccess,
            is ApiUiState.Error -> {
                model.clearImagesState()
                showResultDialog = apiState
            }

            else -> {}
        }
    }
    AppScaffold(
        showBottomBar = false,
        model
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp), // Уменьшен padding на 30%
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Уменьшен spacing на 30%
        ) {
            when (apiState) {
                is ApiUiState.Loading -> {
                    UploadProgressDialog()
                }

                else -> {
                    if (captureState.isNotEmpty()) {
                        PhotosPreviewCard(captureState = captureState)
                    }

                    ActionButtons(
                        navController = navController,
                        captureState = captureState,
                        screenType = screenType,
                        isLoading = apiState is ApiUiState.Loading,
                        onUploadClick = { showUploadDialog = true }
                    )
                }
            }
        }

    }

    if (showUploadDialog && apiState !is ApiUiState.Loading) {
        UploadConfirmationDialog(
            screenType = screenType,
            onConfirm = {
                showUploadDialog = false
                when (screenType) {
                    SendScreenType.REGISTRATION -> model.registerBioVector()
                    SendScreenType.RECOGNITION -> model.recognizePerson()
                }
            },
            onCancel = { showUploadDialog = false }
        )
    }

    showResultDialog?.let { resultState ->
        ResultDialog(
            resultState = resultState,
            onConfirm = {
                showResultDialog = null
                model.resetApiState()
                navController.navigate("serviceScreen")
            }
        )
    }
}

@Composable
fun PhotosPreviewCard(captureState: List<CapturedImage>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Уменьшен elevation
    ) {
        Column(
            modifier = Modifier.padding(4.dp) // Уменьшен padding на 30%
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Превью фотографий",
                    style = MaterialTheme.typography.bodyMedium // Уменьшен размер
                )
                Text(
                    text = "${captureState.size}/${
                        when (captureState.size) {
                            5 -> "5"
                            else -> "1"
                        }
                    }",
                    style = MaterialTheme.typography.bodySmall, // Уменьшен размер
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp)) // Уменьшен spacing на 30%

            when (captureState.size) {
                1 -> SingleImagePreview(capturedImage = captureState.first())
                5 -> MultiImagePreview(capturedImages = captureState)
                else -> SingleImagePreview(capturedImage = captureState.first())
            }
        }
    }
}

@Composable
fun ActionButtons(
    navController: NavHostController,
    captureState: List<CapturedImage>,
    screenType: SendScreenType,
    isLoading: Boolean,
    onUploadClick: () -> Unit
) {
    val isEnabled = when (screenType) {
        SendScreenType.REGISTRATION -> captureState.size == 1 || captureState.size == 5
        SendScreenType.RECOGNITION -> captureState.size == 1
    } && !isLoading

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Уменьшен spacing
    ) {
        FilledTonalButton(
            onClick = onUploadClick,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp), // Уменьшенная высота
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp), // Уменьшен размер
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp) // Уменьшен размер
                )
            }
            Spacer(modifier = Modifier.width(8.dp)) // Уменьшен spacing
            Text(
                text = when {
                    isLoading -> "Отправка..."
                    screenType == SendScreenType.REGISTRATION -> "Зарегистрировать"
                    else -> "Распознать"
                },
                style = MaterialTheme.typography.bodyLarge // Уменьшен размер
            )
        }
        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(18.dp) // Уменьшен размер
            )
            Spacer(modifier = Modifier.width(6.dp)) // Уменьшен spacing
            Text(
                "Вернуться",
                style = MaterialTheme.typography.bodyMedium // Уменьшен размер
            )
        }
    }
}

@Composable
fun UploadConfirmationDialog(
    screenType: SendScreenType,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = when (screenType) {
                    SendScreenType.REGISTRATION -> "Подтверждение регистрации"
                    SendScreenType.RECOGNITION -> "Подтверждение распознавания"
                },
                style = MaterialTheme.typography.bodyLarge // Уменьшен размер
            )
        },
        text = {
            Text(
                text = when (screenType) {
                    SendScreenType.REGISTRATION -> "Вы уверены, что хотите зарегистрировать биовектор?"
                    SendScreenType.RECOGNITION -> "Вы уверены, что хотите отправить фото для распознавания личности?"
                },
                style = MaterialTheme.typography.bodyMedium // Уменьшен размер
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = when (screenType) {
                        SendScreenType.REGISTRATION -> "Зарегистрировать"
                        SendScreenType.RECOGNITION -> "Распознать"
                    },
                    style = MaterialTheme.typography.bodyMedium // Уменьшен размер
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(
                    "Отмена",
                    style = MaterialTheme.typography.bodyMedium // Уменьшен размер
                )
            }
        }
    )
}

@Composable
fun UploadProgressDialog() {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(12.dp), // Уменьшен padding
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp) // Уменьшен elevation
        ) {
            Column(
                modifier = Modifier.padding(20.dp), // Уменьшен padding
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp) // Уменьшен spacing
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp), // Уменьшен размер
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp // Уменьшенная толщина
                )
                Text(
                    text = "Отправка на сервер",
                    style = MaterialTheme.typography.bodyLarge // Уменьшен размер
                )
                Text(
                    text = "Пожалуйста, подождите...",
                    style = MaterialTheme.typography.bodyMedium, // Уменьшен размер
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ResultDialog(
    resultState: ApiUiState,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        title = {
            Text(
                text = when (resultState) {
                    is ApiUiState.Success -> "Успешно!"
                    is ApiUiState.RecognitionSuccess -> "Распознавание завершено"
                    is ApiUiState.RegistrationSuccess -> "Регистрация завершена"
                    is ApiUiState.Error -> "Ошибка"
                    else -> "Неизвестная ошбка"
                },
                style = MaterialTheme.typography.bodyLarge // Уменьшен размер
            )
        },
        text = {
            Column {
                when (resultState) {
                    is ApiUiState.Success -> Text(
                        resultState.message,
                        style = MaterialTheme.typography.bodyMedium // Уменьшен размер
                    )

                    is ApiUiState.RecognitionSuccess -> {
                        Text(
                            "Имя: ${resultState.name}",
                            style = MaterialTheme.typography.bodyMedium // Уменьшен размер
                        )
                        Text(
                            "Сходство: ${resultState.similarity}",
                            style = MaterialTheme.typography.bodyMedium // Уменьшен размер
                        )
                    }

                    is ApiUiState.RegistrationSuccess -> {
                        Text(
                            "Пользователь: ${resultState.name}",
                            style = MaterialTheme.typography.bodyMedium // Уменьшен размер
                        )
                    }

                    is ApiUiState.Error -> Text(
                        resultState.message,
                        style = MaterialTheme.typography.bodyMedium // Уменьшен размер
                    )

                    else -> Text(
                        "Неизвестный результат",
                        style = MaterialTheme.typography.bodyMedium // Уменьшен размер
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    "Понятно",
                    style = MaterialTheme.typography.bodyMedium // Уменьшен размер
                )
            }
        }
    )
}

@Composable
fun SingleImagePreview(capturedImage: CapturedImage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
        shape = MaterialTheme.shapes.small, // Уменьшен радиус скругления
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Уменьшен elevation
    ) {
        Image(
            bitmap = capturedImage.bitmap.asImageBitmap(),
            contentDescription = "Сделанное фото",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}


@Composable
fun MultiImagePreview(capturedImages: List<CapturedImage>) {
    val gridItems = capturedImages.chunked(2)

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp) // Уменьшен spacing на 30%
    ) {
        gridItems.forEachIndexed { index, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp) // Уменьшен spacing на 30%
            ) {
                rowItems.forEach { capturedImage ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(if (index == 2) 1f else 3f / 4f),
                        shape = MaterialTheme.shapes.extraSmall, // Уменьшен радиус скругления
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Уменьшен elevation
                    ) {
                        Image(
                            bitmap = capturedImage.bitmap.asImageBitmap(),
                            contentDescription = "Сделанное фото",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                if (index == 2 && rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}