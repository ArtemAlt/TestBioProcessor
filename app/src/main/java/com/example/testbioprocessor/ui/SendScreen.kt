package com.example.testbioprocessor.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.testbioprocessor.model.camera.CapturedImage
import com.example.testbioprocessor.ui.theme.TestBioProcessorTheme
import com.example.testbioprocessor.viewModel.BioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SendScreen(navController: NavHostController, viewModel: BioViewModel) {
    val captureState by viewModel.capturedImages
    val coroutineScope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Отправить на сервер",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Превью фотографий
            if (captureState.isNotEmpty()) {
                Text(
                    text = "Превью фотографий (${captureState.size} шт.):",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Для 1 фото - большое превью
                if (captureState.size == 1) {
                    SingleImagePreview(capturedImage = captureState.first())
                }
                // Для 5 фото - сетка
                else if (captureState.size == 5) {
                    MultiImagePreview(capturedImages = captureState)
                }
                // Для другого количества - адаптивный вариант
                else {
                    AdaptiveImagePreview(capturedImages = captureState)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "Количество фото: ${captureState.size}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Текущий пользователь: ${viewModel.uiLoginState.value.login}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        startUploadProcess(
                            images = captureState,
                            viewModel = viewModel,
                            coroutineScope = coroutineScope
                        )
                    },
                    enabled = captureState.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Отправить на сервер")
                }

                Button(
                    onClick = {
                        // Очищаем изображения перед возвратом
                        viewModel.clearCapturedImages()
                        navController.navigate("loginScreen")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Вернуться")
                }
            }
        }
    }
}

// Компонент для отображения одного фото (большой размер)
@Composable
fun SingleImagePreview(capturedImage: CapturedImage) {
    Card(
        modifier = Modifier
            .size(250.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        AsyncImage(
            model = capturedImage.uri,
            contentDescription = "Фото ${capturedImage.index}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// Компонент для отображения 5 фото в сетке
@Composable
fun MultiImagePreview(capturedImages: List<CapturedImage>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(300.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(capturedImages) { image ->
            Card(
                modifier = Modifier
                    .aspectRatio(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = image.uri,
                        contentDescription = "Фото ${image.index}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Номер фото в углу
                    Text(
                        text = "${image.index}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(4.dp)
                            .align(Alignment.TopStart)
                    )
                }
            }
        }
    }
}

// Адаптивный компонент для любого количества фото
@Composable
fun AdaptiveImagePreview(capturedImages: List<CapturedImage>) {
    val columns = when (capturedImages.size) {
        1 -> 1
        2 -> 2
        3 -> 2
        4 -> 2
        else -> 3
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(capturedImages) { image ->
            Card(
                modifier = Modifier
                    .aspectRatio(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = image.uri,
                    contentDescription = "Фото ${image.index}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun startUploadProcess(
    images: List<CapturedImage>,
    coroutineScope: CoroutineScope,
    viewModel: BioViewModel
) {

    coroutineScope.launch {
        try {
            val currentUser = viewModel.uiLoginState.value.login
            val success = sendToServer(viewModel, images, currentUser)
            val message = if (success) {
                "Фото $currentUser успешно отправлены на сервер!"
            } else {
                "Ошибка при отправке фото $currentUser"
            }
           onUploadComplete(success, message)

        } catch (e: Exception) {
            val errorMessage = "Ошибка: ${e.message}"
           onUploadComplete(false, errorMessage)
        }
    }
}

private fun onUploadComplete(success: Boolean, message: String) {

}

private fun sendToServer(
    viewModel: BioViewModel,
    images: List<CapturedImage>,
    currentUser: String
): Boolean {
    val success = viewModel.registerPerson(currentUser, images.map { it.toBase64() })
    return success
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun GreetingPreview12() {
    TestBioProcessorTheme {
        SendScreen(navController = rememberNavController(), viewModel = BioViewModel())
    }
}