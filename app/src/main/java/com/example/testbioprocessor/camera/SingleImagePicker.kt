package com.example.testbioprocessor.camera

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.testbioprocessor.model.camera.CapturedImage
import com.example.testbioprocessor.model.camera.SingleImageCaptureState
import com.example.testbioprocessor.ui.AppButton
import com.example.testbioprocessor.ui.AppButtonType
import com.example.testbioprocessor.ui.AppFonts
import com.example.testbioprocessor.ui.Blue20
import com.example.testbioprocessor.ui.Blue60
import com.example.testbioprocessor.ui.Blue80
import com.example.testbioprocessor.ui.White
import com.example.testbioprocessor.ui.custom.AppScaffold
import com.example.testbioprocessor.viewModel.BioViewModelNew
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.util.Objects

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SingleImagePicker(
    model: BioViewModelNew,
    navigation: NavHostController,
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

    AppScaffold(
        showBottomBar = true,
        model = model
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
                text = "Распознавание личности",
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
                // Заголовок с прогрессом
                Text(
                    text = "Сделайте свое фото",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AppFonts.customFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Blue80,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Сообщение о загрузке
                captureState.uploadMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AppFonts.customFontFamily
                        ),
                        color = if (message.contains("успешно")) Blue60 else Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Галерея сделанных фото
                if (captureState.capturedImage != null) {
                    Text(
                        text = "Ваше фото:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AppFonts.customFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Blue80,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(200.dp)
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = captureState.capturedImage!!.uri,
                            contentDescription = "Фото для проверки",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Кнопка отправки на сервер
                    AppButton(
                        onClick = {
                            if (captureState.capturedImage != null) {
                                model.setImages(capturedImages = listOf(captureState.capturedImage!!))
                                navigation.navigate("sendRecognitionScreen")
                            }
                        },
                        enabled = captureState.isLoaded,
                        text = "Распознать",
                        buttonType = AppButtonType.PRIMARY,
                        icon = Icons.Default.Send,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                } else {
                    // Кнопка сделать фото
                    AppButton(
                        onClick = {
                            cameraPermissionState.launchPermissionRequest()
                        },
                        text = "Сделать фото",
                        buttonType = AppButtonType.PRIMARY,
                        icon = Icons.Default.Face,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка возврата назад
                AppButton(
                    onClick = {
                        captureState = SingleImageCaptureState()
                        navigation.popBackStack()
                    },
                    text = "Вернуться назад",
                    buttonType = AppButtonType.SECONDARY,
                    icon = Icons.Default.ArrowBack,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        }
    }
}