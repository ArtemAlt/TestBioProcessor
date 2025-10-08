package com.example.testbioprocessor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.testbioprocessor.ui.custom.AppScaffold
import com.example.testbioprocessor.viewModel.BioViewModelNew

@Composable
fun ServicesScreen(navController: NavHostController, model: BioViewModelNew) {
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
            title = "Сбросить данные",
            icon = Icons.Default.Delete,
            description = "Удаление сохраненных данных текущего пользователя",
            destination = "deleteScreen"
        ),
        ServiceItem(
            id = 4,
            title = "Liveness",
            icon = Icons.Default.Favorite,
            description = "Проверка живого присутствия",
            destination = "",
            isInDevelopment = true
        ),
        ServiceItem(
            id = 5,
            title = "Информация",
            icon = Icons.Default.Info,
            description = "Информация по серверу",
            destination = "",
            isInDevelopment = true
        )
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
                )
        ) {
            // Заголовок
            Text(
                text = "Доступные функции",
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

            // Список услуг
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(services.size) { index ->
                    ServiceCard(
                        service = services[index],
                        onServiceClick = {
                            if (services[index].destination.isNotEmpty() && !services[index].isInDevelopment) {
                                navController.navigate(services[index].destination)
                            }
                        }
                    )
                }
            }

            // Кнопка возврата назад
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                AppButton(
                    onClick = { navController.navigate("main") },
                    text = "Вернуться на главную",
                    buttonType = AppButtonType.SECONDARY,
                    icon = Icons.Default.ArrowBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
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

