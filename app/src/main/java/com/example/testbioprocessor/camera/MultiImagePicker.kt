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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.testbioprocessor.createImageFile
import com.example.testbioprocessor.model.camera.CapturedImage
import com.example.testbioprocessor.model.camera.ImageCaptureState
import com.example.testbioprocessor.ui.CurrentUserLogin
import com.example.testbioprocessor.ui.theme.TestBioProcessorTheme
import com.example.testbioprocessor.viewModel.BioViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.util.Objects

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MultiImagePicker(
    viewModel: BioViewModel,
    navController: NavHostController,
    onUploadComplete: (Boolean, String?) -> Unit = { _, _ -> } // Колбэк после загрузки
) {
    val context = LocalContext.current

    // Состояние съемки
    var captureState by remember {
        mutableStateOf(ImageCaptureState())
    }

    LaunchedEffect(captureState.capturedImages) {
        viewModel.updateCapturedImages(captureState.capturedImages)
    }
    // Режим съемки: true - многокадровый (5 фото), false - однокадровый (1 фото)
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

                // Если достигли нужного количества фото, начинаем загрузку
                if (isCaptureComplete) { navController.navigate("sendScreen") }
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
                // Кнопка многокадрового режима
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

                // Кнопка однокадрового режима
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
                captureState.isUploading -> "Загрузка на сервер..."
                captureState.capturedImages.size == getMaxImages(isMultiCaptureMode) -> "Все фото сделаны!"
                else -> "Фото ${captureState.currentStep}/${getMaxImages(isMultiCaptureMode)}"
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

                captureState.capturedImages.size == getMaxImages(isMultiCaptureMode) -> {
                    Button(
                        onClick = {
                          navController.navigate("sendScreen")
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
                            val maxImages = getMaxImages(isMultiCaptureMode)
                            if (captureState.currentStep <= maxImages) {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        },
                        enabled = !captureState.isUploading && captureState.currentStep <= getMaxImages(isMultiCaptureMode)
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

        val state by viewModel.uiLoginState.collectAsStateWithLifecycle()

        // Информация о пользователе
        CurrentUserLogin(login = state.login)
    }
}

// Вспомогательная функция для получения максимального количества фото
private fun getMaxImages(isMultiCaptureMode: Boolean): Int {
    return if (isMultiCaptureMode) 5 else 1
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    TestBioProcessorTheme {
        MultiImagePicker(viewModel = BioViewModel(), navController = rememberNavController())
    }
}
