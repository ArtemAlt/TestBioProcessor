package com.example.testbioprocessor.camera

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.testbioprocessor.model.camera.CapturedImage
import com.example.testbioprocessor.model.camera.SingleImageCaptureState
import com.example.testbioprocessor.viewModel.BioViewModelNew
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.util.Objects

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SingleImagePicker(
    model: BioViewModelNew,
    navigation: NavHostController,
    onRecognitionComplete: (Boolean) -> Unit = { _ -> } // Колбэк после загрузки
) {
    val context = LocalContext.current

    // Состояние съемки
    var captureState by remember {
        mutableStateOf(SingleImageCaptureState())
    }

    LaunchedEffect(captureState.capturedImage) {
        model.clearImagesState()
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
                val newImage = CapturedImage(
                    uri = currentPhotoUri,
                    file = currentPhotoFile,
                    index = 1
                )
                captureState = captureState.copy(
                    capturedImage = newImage,
                    isLoaded = true
                )
            }
        }
    )

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
        if (captureState.capturedImage != null) {
            Text(
                text = "Ваше фото:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AsyncImage(
                model = captureState.capturedImage!!.uri,
                contentDescription = "Фото для проверки",
                modifier = Modifier
                    .size(200.dp)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка отправки на сервер
            Button(
                onClick = {
                    if (captureState.capturedImage != null) {
                        model.setImages(capturedImages = listOf(captureState.capturedImage!!))
                        navigation.navigate("sendScreen")
                    }
                },
                enabled = captureState.isLoaded
            ) {
                Text("Отправить на распознавание")
            }
        } else {
            // Кнопка сделать фото
            Button(
                onClick = {
                    cameraPermissionState.launchPermissionRequest()
                }
            ) {
                Text("Сделать фото")
            }
        }

    }
}
