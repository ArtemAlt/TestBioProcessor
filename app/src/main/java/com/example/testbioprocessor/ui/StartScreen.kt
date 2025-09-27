package com.example.testbioprocessor.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.testbioprocessor.ui.theme.TestBioProcessorTheme
import com.example.testbioprocessor.viewModel.BioViewModel
import com.example.testbioprocessor.viewModel.RecognitionUiState

@Composable
fun StartScreen(navController: NavHostController,
                model: BioViewModel) {
    val uiState by model.uiState.collectAsState()
    Scaffold (modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(128.dp))
            Text(text = "Приложение разработано для тестрования биопроцессора и несет только ознакомительный функционал." +
                    " Биомтерические образцы не сохраняются",
                modifier = Modifier.fillMaxWidth().padding(25.dp))

            Row {
                Button(
                    content = { Text("Принять") },
                    onClick = {navController.navigate("loginScreen")}
                )
                Spacer(modifier = Modifier.width(20.dp))
                Button(
                    content = { Text("Отказаться") },
                    onClick = {System.exit(0)}
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            ServerStatusCompact(uiState = uiState, onRetry = { model.retryConnection() })
        }
    }
}

@Composable
fun ServerStatusCompact(
    uiState: RecognitionUiState,
    onRetry: () -> Unit = {}
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .background(
                            color = when (uiState) {
                                is RecognitionUiState.HealthCheckSuccess -> Color.Green
                                is RecognitionUiState.Error -> Color.Red
                                is RecognitionUiState.Loading-> Color.Yellow
                                else -> Color.Gray
                            },
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Статус сервера",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )

                    Text(
                        text = when (uiState) {
                            is RecognitionUiState.Loading -> "Проверка..."
                            is RecognitionUiState.HealthCheckSuccess-> "Доступен"
                            is RecognitionUiState.Idle -> "Недоступен"
                            else -> "Error"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Кнопка повторной проверки (только при ошибке)
            if (uiState is RecognitionUiState.Loading) {
                IconButton(
                    onClick = onRetry,
                    modifier = Modifier.padding(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Повторить",
                        tint = Color.Blue
                    )
                }
            } else if (uiState is RecognitionUiState.Error) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestBioProcessorTheme {
        StartScreen(
            navController = rememberNavController(),
            model = BioViewModel()
        )
    }
}