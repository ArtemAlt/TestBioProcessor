package com.example.testbioprocessor.camera

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.testbioprocessor.model.camera.CapturedImage
import com.example.testbioprocessor.model.camera.ImageCaptureState
import com.example.testbioprocessor.ui.AppButton
import com.example.testbioprocessor.ui.AppButtonType
import com.example.testbioprocessor.ui.AppFonts
import com.example.testbioprocessor.ui.Blue20
import com.example.testbioprocessor.ui.Blue40
import com.example.testbioprocessor.ui.Blue60
import com.example.testbioprocessor.ui.Blue80
import com.example.testbioprocessor.ui.White
import com.example.testbioprocessor.ui.custom.AppScaffold
import com.example.testbioprocessor.viewModel.BioViewModelNew
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects

@SuppressLint("RememberInComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RegisterScreen(
    model: BioViewModelNew,
    navController: NavHostController,
) {
    val context = LocalContext.current
    var captureState by remember {
        mutableStateOf(ImageCaptureState())
    }

    // Управление навигацией
    var shouldNavigate by remember { mutableStateOf(false) }

    // Режим съемки
    var isMultiCaptureMode by remember { mutableStateOf(true) }

    // Подготовка URI для следующего фото
    val currentPhotoFile = remember { context.createImageFile() }
    val authority = "${context.packageName}.fileprovider"
    val currentPhotoUri = remember {
        FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            authority,
            currentPhotoFile
        )
    }

    // Лаунчер для камеры
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                // Добавляем сделанное фото в список
                val newImage = CapturedImage(
                    uri = currentPhotoUri,
                    file = currentPhotoFile,
                    index = captureState.currentStep
                )

                val updatedImages = captureState.capturedImages + newImage
                val maxImages = if (isMultiCaptureMode) 5 else 1
                val isCaptureComplete = captureState.currentStep >= maxImages

                captureState = captureState.copy(
                    capturedImages = updatedImages,
                    currentStep = captureState.currentStep + 1,
                    isCaptureInProgress = !isCaptureComplete
                )

                // Если достигли нужного количества фото, сохраняем и готовим навигацию
                if (isCaptureComplete) {
                    model.setImages(captureState.capturedImages)
                    shouldNavigate = true // Устанавливаем флаг для навигации
                }
            }
        }
    )

    // Навигация при завершении съемки
    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            delay(500) // Небольшая задержка для плавности
            navController.navigate("sendRegistrationScreen") {
                // Очищаем back stack чтобы нельзя было вернуться
                popUpTo("multiImagePicker") { inclusive = true }
            }
            shouldNavigate = false
        }
    }

    // Разрешение камеры
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { granted ->
            if (granted) {
                cameraLauncher.launch(currentPhotoUri)
            }
        }
    )

    AppScaffold(
        showBottomBar = true,
        model = model,
    ) { paddingValues ->
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
                text = "Зарегистрировать свой биовектор",
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Переключатель режимов съемки
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Режим съёмки:",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = AppFonts.customFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Blue80,
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    // Контейнер переключателя
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Blue20)
                            .border(
                                width = 2.dp,
                                color = Blue40,
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Row {
                            // Кнопка 5 фото
                            Box(
                                modifier = Modifier
                                    .height(44.dp)
                                    .width(100.dp)
                                    .clickable(
                                        interactionSource = MutableInteractionSource(),
                                        indication = LocalIndication.current
                                    ) { isMultiCaptureMode = true }
                                    .background(
                                        if (isMultiCaptureMode) Blue60
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "5 фото",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = AppFonts.customFontFamily,
                                        fontWeight = if (isMultiCaptureMode) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isMultiCaptureMode) Color.White else Blue80
                                )
                            }

                            // Кнопка 1 фото
                            Box(
                                modifier = Modifier
                                    .height(44.dp)
                                    .width(100.dp)
                                    .clickable(
                                        interactionSource = MutableInteractionSource(),
                                        indication = LocalIndication.current
                                    ) { isMultiCaptureMode = false }
                                    .background(
                                        if (!isMultiCaptureMode) Blue60
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "1 фото",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = AppFonts.customFontFamily,
                                        fontWeight = if (!isMultiCaptureMode) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (!isMultiCaptureMode) Color.White else Blue80
                                )
                            }
                        }
                    }
                }

                // Заголовок с прогрессом
                Text(
                    text = when {
                        captureState.capturedImages.size == getMaxImages(isMultiCaptureMode) -> "Все фото сделаны!"
                        else -> "Фото ${captureState.currentStep}/${getMaxImages(isMultiCaptureMode)}"
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AppFonts.customFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Blue80,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Галерея сделанных фото
                if (captureState.capturedImages.isNotEmpty()) {
                    Text(
                        text = "Сделанные фото:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AppFonts.customFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Blue80,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        items(captureState.capturedImages) { image ->
                            Card(
                                elevation = CardDefaults.cardElevation(2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(4.dp)
                            ) {
                                AsyncImage(
                                    model = image.uri,
                                    contentDescription = "Фото ${image.index}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                // Кнопки управления
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when {
                        captureState.capturedImages.size == getMaxImages(isMultiCaptureMode) -> {
                            AppButton(
                                onClick = {
                                    model.setImages(captureState.capturedImages)
                                    shouldNavigate = true
                                },
                                text = "Перейти к отправке",
                                buttonType = AppButtonType.PRIMARY,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )

                            AppButton(
                                onClick = {
                                    // Сброс для новой сессии
                                    captureState = ImageCaptureState()
                                },
                                text = "Сделать новые фото",
                                buttonType = AppButtonType.SECONDARY,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )
                        }

                        else -> {
                            AppButton(
                                onClick = {
                                    val maxImages = getMaxImages(isMultiCaptureMode)
                                    if (captureState.currentStep <= maxImages) {
                                        cameraPermissionState.launchPermissionRequest()
                                    }
                                },
                                enabled = captureState.currentStep <= getMaxImages(isMultiCaptureMode),
                                text = when {
                                    captureState.capturedImages.isEmpty() -> "Сделать первое фото"
                                    else -> "Сделать фото ${captureState.currentStep}/${getMaxImages(isMultiCaptureMode)}"
                                },
                                buttonType = AppButtonType.PRIMARY,
                                icon = Icons.Default.Face,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Вспомогательная функция для получения максимального количества фото
private fun getMaxImages(isMultiCaptureMode: Boolean): Int {
    return if (isMultiCaptureMode) 5 else 1
}

fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    return File.createTempFile(
        imageFileName, /* prefix */
        ".png", /* suffix */
        externalCacheDir /* directory */
    )
}