package com.example.testbioprocessor.camera

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.testbioprocessor.createImageFile
import com.example.testbioprocessor.model.camera.CapturedImage
import com.example.testbioprocessor.model.camera.ImageCaptureState
import com.example.testbioprocessor.viewModel.BioViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Objects

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MultiImagePicker(
    viewModel: BioViewModel,
    navController: NavHostController,
    onUploadComplete: (Boolean, String?) -> Unit = { _, _ -> } // Колбэк после загрузки
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Состояние съемки
    var captureState by remember {
        mutableStateOf(ImageCaptureState())
    }

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

                captureState = captureState.copy(
                    capturedImages = updatedImages,
                    currentStep = captureState.currentStep + 1,
                    isCaptureInProgress = captureState.currentStep < 5
                )

                // Если сделали 5 фото, начинаем загрузку
                if (captureState.currentStep > 5) {
                    startUploadProcess(
                        images = updatedImages,
                        onUploadComplete = onUploadComplete,
                        updateState = { newState ->
                            captureState = newState
                        },
                        coroutineScope = coroutineScope,
                        viewModel = viewModel
                    )
                }
            }
        }
    )

    // Разрешение камеры
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { granted ->
            if (granted) {
                captureState = captureState.copy(isCaptureInProgress = true)
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
        // Заголовок с прогрессом
        Text(
            text = when {
                captureState.isUploading -> "Загрузка на сервер..."
                captureState.capturedImages.size == 5 -> "Все фото сделаны!"
                else -> "Фото ${captureState.currentStep}/5"
            },
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Сообщение о загрузке
        captureState.uploadMessage?.let { message ->
            Text(
                text = message,
                color = if (message.contains("успешно")) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

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
                captureState.isUploading -> {
                    CircularProgressIndicator()
                    Text("Отправляем фото на сервер...")
                }

                captureState.capturedImages.size == 5 -> {
                    Button(
                        onClick = {
                            startUploadProcess(
                                images = captureState.capturedImages,
                                onUploadComplete = onUploadComplete,
                                updateState = { newState ->
                                    captureState = newState
                                },
                                coroutineScope = coroutineScope,
                                viewModel
                            )
                        },
                        enabled = !captureState.isUploading
                    ) {
                        Text("Отправить на сервер")
                    }

                    Button(
                        onClick = {
                            // Сброс для новой сессии
                            captureState = ImageCaptureState()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Сделать новые фото")
                    }
                }

                else -> {
                    Button(
                        onClick = {
                            if (captureState.currentStep <= 5) {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        },
                        enabled = !captureState.isUploading && captureState.currentStep <= 5
                    ) {
                        Text(
                            when {
                                captureState.capturedImages.isEmpty() -> "Сделать первое фото"
                                else -> "Сделать фото ${captureState.currentStep}/5"
                            }
                        )
                    }
                }
            }
        }

        // Информация о пользователе
        Text(
            text = "Пользователь: $viewModel.uiLoginState.value.login",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

// Функция для запуска процесса загрузки
private fun startUploadProcess(
    images: List<CapturedImage>,
    onUploadComplete: (Boolean, String?) -> Unit,
    updateState: (ImageCaptureState) -> Unit,
    coroutineScope: CoroutineScope,
    viewModel: BioViewModel
) {
    updateState(
        ImageCaptureState(
            capturedImages = images,
            currentStep = 6,
            isUploading = true,
            uploadMessage = "Подготовка к отправке..."
        )
    )

    coroutineScope.launch {
        try {
            val currentUser = viewModel.uiLoginState.value.login
            val success = viewModel.registerPerson(currentUser,
                images.map { it.toBase64() }
            )

            val message = if (success) {
                "Фото $currentUser успешно отправлены на сервер!"
            } else {
                "Ошибка при отправке фото $currentUser"
            }

            updateState(
                ImageCaptureState(
                    capturedImages = images,
                    currentStep = 6,
                    isUploading = false,
                    uploadMessage = message
                )
            )

            onUploadComplete(success, message)

        } catch (e: Exception) {
            val errorMessage = "Ошибка: ${e.message}"
            updateState(
                ImageCaptureState(
                    capturedImages = images,
                    currentStep = 6,
                    isUploading = false,
                    uploadMessage = errorMessage
                )
            )
            onUploadComplete(false, errorMessage)
        }
    }
}
