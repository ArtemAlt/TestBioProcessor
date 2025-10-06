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
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.testbioprocessor.model.camera.CapturedImage
import com.example.testbioprocessor.model.camera.ImageCaptureState
import com.example.testbioprocessor.ui.CurrentUserLogin
import com.example.testbioprocessor.viewModel.BioViewModelNew
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MultiImagePicker(
    model: BioViewModelNew,
    navController: NavHostController,
    onUploadComplete: (Boolean, String?) -> Unit = { _, _ -> }
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
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 16.dp)
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                TextButton(
                    onClick = { isMultiCaptureMode = true },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (isMultiCaptureMode) MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        contentColor = if (isMultiCaptureMode) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("5 фото")
                }

                TextButton(
                    onClick = { isMultiCaptureMode = false },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (!isMultiCaptureMode) MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        contentColor = if (!isMultiCaptureMode) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("1 фото")
                }
            }
        }

        // Заголовок с прогрессом
        Text(
            text = when {
                captureState.capturedImages.size == getMaxImages(isMultiCaptureMode) -> "Все фото сделаны!"
                else -> "Фото ${captureState.currentStep}/${getMaxImages(isMultiCaptureMode)}"
            },
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Галерея сделанных фото
        if (captureState.capturedImages.isNotEmpty()) {
            Text(
                text = "Сделанные фото:",
                style = MaterialTheme.typography.titleMedium,
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
                    AsyncImage(
                        model = image.uri,
                        contentDescription = "Фото ${image.index}",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(4.dp)
                    )
                }
            }
        }

        // Кнопки управления
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when {
                captureState.capturedImages.size == getMaxImages(isMultiCaptureMode) -> {
                    Button(
                        onClick = {
                            model.setImages(captureState.capturedImages)
                            shouldNavigate = true
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Перейти к отправке")
                    }

                    Button(
                        onClick = {
                            // Сброс для новой сессии
                            captureState = ImageCaptureState()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Сделать новые фото")
                    }
                }

                else -> {
                    Button(
                        onClick = {
                            val maxImages = getMaxImages(isMultiCaptureMode)
                            if (captureState.currentStep <= maxImages) {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        },
                        enabled = captureState.currentStep <= getMaxImages(isMultiCaptureMode),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(
                            when {
                                captureState.capturedImages.isEmpty() -> "Сделать первое фото"
                                else -> "Сделать фото ${captureState.currentStep}/${getMaxImages(isMultiCaptureMode)}"
                            }
                        )
                    }
                }
            }
        }

        // Информация о пользователе
        Spacer(modifier = Modifier.height(32.dp))
        CurrentUserLogin(model)
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