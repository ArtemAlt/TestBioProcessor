package com.example.testbioprocessor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.testbioprocessor.model.camera.CapturedImage
import com.example.testbioprocessor.viewModel.BioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreenNew(navController: NavHostController, viewModel: BioViewModel) {
    val captureState by viewModel.capturedImages
    val coroutineScope = rememberCoroutineScope()
    var showUploadDialog by remember { mutableStateOf(false) }
    var uploadResult by remember { mutableStateOf<Pair<Boolean, String?>?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Подтверждение отправки",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Карточка с информацией
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp), // Уменьшили padding с 20dp до 16dp
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Уменьшили spacing с 16dp до 12dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(60.dp) // Уменьшили с 80dp до 60dp
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = "${captureState.size}",
                            style = MaterialTheme.typography.headlineMedium, // Уменьшили с displaySmall до headlineMedium
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Фото готовы к отправке",
                        style = MaterialTheme.typography.titleMedium, // Уменьшили с headlineSmall до titleMedium
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Можно отправлять на сервер",
                        style = MaterialTheme.typography.bodySmall, // Уменьшили с bodyMedium до bodySmall
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Превью фотографий
            if (captureState.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "${captureState.size}/5",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (captureState.size) {
                            1 -> SingleImagePreview(capturedImage = captureState.first())
                            5 -> MultiImagePreview(capturedImages = captureState)
                            else -> AdaptiveImagePreview(capturedImages = captureState)
                        }
                    }
                }
            }

            // Кнопки действий
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Основная кнопка отправки
                FilledTonalButton(
                    onClick = {
                        showUploadDialog = true
                        startUploadProcess(
                            images = captureState,
                            viewModel = viewModel,
                            coroutineScope = coroutineScope,
                            onComplete = { success, message ->
                                uploadResult = success to message
                                showUploadDialog = false
                            }
                        )
                    },
                    enabled = captureState.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Отправить на сервер",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Вторичная кнопка - сделать новые фото
                OutlinedButton(
                    onClick = {
                        viewModel.clearCapturedImages()
                        navController.navigate("cameraScreen")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сделать новые фото")
                }

                // Третичная кнопка - вернуться
                TextButton(
                    onClick = {
                        viewModel.clearCapturedImages()
                        navController.navigate("loginScreen")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Вернуться в начало")
                }
            }
            val state by viewModel.uiLoginState.collectAsStateWithLifecycle()
            CurrentUserLogin(state.login)
        }

        // Диалог загрузки
        if (showUploadDialog) {
            UploadProgressDialog()
        }

        // Результат загрузки
        uploadResult?.let { (success, message) ->
            LaunchedEffect(success) {
                delay(3000) // Показываем 3 секунды
                uploadResult = null
                if (success) {
                    navController.navigate("loginScreen")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Snackbar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = if (success) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    contentColor = if (success) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onError
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (success) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(message ?: if (success) "Успешно отправлено!" else "Ошибка отправки")
                    }
                }
            }
        }
    }
}

// Красивое превью для одного фото
@Composable
fun SingleImagePreview(capturedImage: CapturedImage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = capturedImage.uri,
                contentDescription = "Фото ${capturedImage.index}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Номер фото
            Text(
                text = "Фото ${capturedImage.index}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .align(Alignment.BottomStart)
            )
        }
    }
}

// Сетка для нескольких фото
@Composable
fun MultiImagePreview(capturedImages: List<CapturedImage>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(capturedImages) { image ->
            Card(
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    AsyncImage(
                        model = image.uri,
                        contentDescription = "Фото ${image.index}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Номер фото
                    Text(
                        text = "${image.index}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
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

// Диалог загрузки
@Composable
fun UploadProgressDialog() {
    Dialog(onDismissRequest = { /* Нельзя закрыть */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )

                Text(
                    text = "Отправка на сервер",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Пожалуйста, подождите...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Обновленная функция загрузки с колбэком
private fun startUploadProcess(
    images: List<CapturedImage>,
    viewModel: BioViewModel,
    coroutineScope: CoroutineScope,
    onComplete: (Boolean, String?) -> Unit
) {
    coroutineScope.launch {
        try {
            val currentUser = viewModel.uiLoginState.value.login
            val success = viewModel.registerPerson(
                currentUser,
                images.map { it.toBase64() }
            )

            val message = if (success) {
                "✅ Фото успешно отправлены на сервер!"
            } else {
                "❌ Ошибка при отправке фото"
            }

            onComplete(success, message)
        } catch (e: Exception) {
            onComplete(false, "⚠️ Ошибка: ${e.message}")
        }
    }
}

// Адаптивное превью
@Composable
fun AdaptiveImagePreview(capturedImages: List<CapturedImage>) {
    val columns = when (capturedImages.size) {
        1 -> 1
        2 -> 2
        3 -> 2
        else -> 2
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.height(150.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(capturedImages) { image ->
            Card(
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = image.uri,
                    contentDescription = "Фото ${image.index}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.aspectRatio(1f)
                )
            }
        }
    }
}