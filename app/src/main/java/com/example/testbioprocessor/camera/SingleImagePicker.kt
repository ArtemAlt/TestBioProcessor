package com.example.testbioprocessor.camera

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.testbioprocessor.model.camera.CapturedImage
import com.example.testbioprocessor.model.camera.ImageCaptureState
import com.example.testbioprocessor.ui.CurrentUserLogin
import com.example.testbioprocessor.viewModel.BioViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.util.Objects

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SingleImagePicker(
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

                val maxImages = 1
                val isCaptureComplete = captureState.currentStep >= maxImages

                captureState = captureState.copy(
                    capturedImages = updatedImages,
                    currentStep = captureState.currentStep + 1,
                    isCaptureInProgress = !isCaptureComplete
                )

                // Если достигли нужного количества фото, начинаем загрузку
                if (isCaptureComplete) { navController.navigate("checkScreen") }
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
            text = "Сделайте свое фото",
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

                captureState.capturedImages.size == 1 -> {
                    Button(
                        onClick = {
                          navController.navigate("checkScreen")
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
                            val maxImages = 1
                            if (captureState.currentStep <= maxImages) {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        },
                        enabled = captureState.currentStep <= 1
                    ) {
                        Text("Сделать фото")
                    }
                }
            }
        }

        val state by viewModel.uiLoginState.collectAsStateWithLifecycle()

        // Информация о пользователе
        CurrentUserLogin(login = state.login)
    }
}
