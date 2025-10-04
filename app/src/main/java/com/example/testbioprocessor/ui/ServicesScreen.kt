package com.example.testbioprocessor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.testbioprocessor.viewModel.BioViewModelNew

@Composable
fun ServicesScreen(navController: NavHostController, viewModel: BioViewModelNew) {
    var selectedService by remember { mutableStateOf<ServiceItem?>(null) }

    val services = listOf(
        ServiceItem(
            id = 1,
            title = "Зарегистрировать биовектор",
            icon = Icons.Default.Add,
            description = "Создание и регистрация биометрического вектора",
            destination = "registerScreen"
        ),
        ServiceItem(
            id = 2,
            title = "Распознавание",
            icon = Icons.Default.Face,
            description = "Идентификация по биометрическим данным",
            destination = "recognitionScreen"
        ),
        ServiceItem(
            id = 3,
            title = "Удалить биовектор",
            icon = Icons.Default.Delete,
            description = "Удаление сохраненных биометрических данных",
            destination = "deleteScreen"
        ),
        ServiceItem(
            id = 4,
            title = "Liveness",
            icon = Icons.Default.FavoriteBorder,
            description = "Проверка живого присутствия (в разработке)",
            destination = "",
            isInDevelopment = true
        ),
        ServiceItem(
            id = 5,
            title = "Информация",
            icon = Icons.Default.Search,
            description = "Инфомация по серверу (в разработке)",
            destination = "",
            isInDevelopment = true
        )
    )

    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Заголовок
            ServiceCardInfo(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Четкий заголовок
                    Text(
                        text = "Биометрические услуги",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Лаконичный подзаголовок
                    Text(
                        text = "Выберите услугу для продолжения",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Список услуг с Box и скроллом
            Box(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    services.forEach { service ->
                        ServiceCard(
                            service = service,
                            onServiceClick = {
                                if (service.destination.isNotEmpty() && !service.isInDevelopment) {
                                    navController.navigate(service.destination)
                                }
                            },
                        )
                    }
                }
            }
            CurrentUserLogin(viewModel)
        }
    }

// Диалог ВНЕ ScreenContainer
    if (selectedService != null) {
        ServiceInfoDialog(
            service = selectedService!!,
            onDismiss = { selectedService = null }
        )
    }



}

// Модель данных для услуги
data class ServiceItem(
    val id: Int,
    val title: String,
    val icon: ImageVector,
    val description: String,
    val destination: String,
    val isInDevelopment: Boolean = false
)

//// Функция для показа информации (добавьте в ServicesScreen)
//@Composable
//fun ServicesScreen(navController: NavHostController, viewModel: BioViewModel) {
//    var selectedService by remember { mutableStateOf<ServiceItem?>(null) }
//
//    // ... остальной код ServicesScreen ...
//
//    // Диалог информации
//    selectedService?.let { service ->
//        ServiceInfoDialog(
//            service = service,
//            onDismiss = { selectedService = null }
//        )
//    }
//
//    // В ServiceCard передаем:
//    onInfoClick = { selectedService = service }
//}

