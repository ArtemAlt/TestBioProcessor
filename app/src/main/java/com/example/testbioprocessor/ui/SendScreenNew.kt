package com.example.testbioprocessor.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Blue20, White)
                    )
                )
        ) {
            // Заголовок
            Text(
                text = when (screenType) {
                    SendScreenType.REGISTRATION -> "Регистрация биовектора"
                    SendScreenType.RECOGNITION -> "Распознавание личности"
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = AppFonts.customFontFamily,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Blue80,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            onUploadClick = { showUploadDialog = true },
                            onBackClick = {
                                // Сбрасываем состояние фотографий при возврате
                                model.clearImagesState()
                                navController.popBackStack()
                            }
                        )
                    }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Превью фотографий",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = AppFonts.customFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Blue80
                )
                Text(
                    text = "${captureState.size}/${
                        when (captureState.size) {
                            5 -> "5"
                            else -> "1"
                        }
                    }",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = AppFonts.customFontFamily
                    ),
                    color = Blue60
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
    onUploadClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val isEnabled = when (screenType) {
        SendScreenType.REGISTRATION -> captureState.size == 1 || captureState.size == 5
        SendScreenType.RECOGNITION -> captureState.size == 1
    } && !isLoading

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Основная кнопка действия
        AppButton(
            onClick = onUploadClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = isEnabled,
            isLoading = isLoading,
            text = when {
                isLoading -> "Отправка..."
                screenType == SendScreenType.REGISTRATION -> "Зарегистрировать"
                else -> "Распознать"
            },
            icon = if (isLoading) Icons.Default.Refresh else Icons.Default.PlayArrow,
            buttonType = AppButtonType.PRIMARY
        )

        // Кнопка возврата
        AppButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            isLoading = false,
            text = "Вернуться назад",
            icon = Icons.Default.ArrowBack,
            buttonType = AppButtonType.SECONDARY
        )
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
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = when (screenType) {
                    SendScreenType.REGISTRATION -> "Подтверждение регистрации"
                    SendScreenType.RECOGNITION -> "Подтверждение распознавания"
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = AppFonts.customFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = Blue80
            )
        },
        text = {
            Text(
                text = when (screenType) {
                    SendScreenType.REGISTRATION -> "Вы уверены, что хотите зарегистрировать биовектор?"
                    SendScreenType.RECOGNITION -> "Вы уверены, что хотите отправить фото для распознавания личности?"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = AppFonts.customFontFamily
                ),
                color = Blue80
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = when (screenType) {
                        SendScreenType.REGISTRATION -> "Зарегистрировать"
                        SendScreenType.RECOGNITION -> "Распознать"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = AppFonts.customFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Blue60
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(
                    "Отмена",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = AppFonts.customFontFamily
                    ),
                    color = Blue80
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
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Blue60,
                    strokeWidth = 3.dp
                )
                Text(
                    text = "Отправка на сервер",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AppFonts.customFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Blue80
                )
                Text(
                    text = "Пожалуйста, подождите...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = AppFonts.customFontFamily
                    ),
                    color = Blue60
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
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = when (resultState) {
                    is ApiUiState.Success -> "Успешно!"
                    is ApiUiState.RecognitionSuccess -> "Распознавание завершено"
                    is ApiUiState.RegistrationSuccess -> "Регистрация завершена"
                    is ApiUiState.Error -> "Ошибка"
                    else -> "Неизвестная ошибка"
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = AppFonts.customFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = when (resultState) {
                    is ApiUiState.Error -> Color.Red
                    else -> Blue80
                }
            )
        },
        text = {
            Column {
                when (resultState) {
                    is ApiUiState.Success -> Text(
                        resultState.message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AppFonts.customFontFamily
                        ),
                        color = Blue80
                    )

                    is ApiUiState.RecognitionSuccess -> {
                        Text(
                            "Имя: ${resultState.name}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = AppFonts.customFontFamily
                            ),
                            color = Blue80
                        )
                        Text(
                            "Сходство: ${resultState.similarity}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = AppFonts.customFontFamily
                            ),
                            color = Blue80
                        )
                    }

                    is ApiUiState.RegistrationSuccess -> {
                        Text(
                            "Пользователь: ${resultState.name}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = AppFonts.customFontFamily
                            ),
                            color = Blue80
                        )
                    }

                    is ApiUiState.Error -> Text(
                        resultState.message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AppFonts.customFontFamily
                        ),
                        color = Color.Red
                    )

                    else -> Text(
                        "Неизвестный результат",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AppFonts.customFontFamily
                        ),
                        color = Blue80
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    "Понятно",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = AppFonts.customFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Blue60
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
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        gridItems.forEachIndexed { index, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { capturedImage ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(if (index == 2) 1f else 3f / 4f),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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