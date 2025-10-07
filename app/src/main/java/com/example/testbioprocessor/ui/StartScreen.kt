package com.example.testbioprocessor.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.testbioprocessor.R
import com.example.testbioprocessor.model.HealthRecognitionStatus
import com.example.testbioprocessor.ui.custom.AppScaffold
import com.example.testbioprocessor.ui.theme.Blue20
import com.example.testbioprocessor.ui.theme.Blue40
import com.example.testbioprocessor.ui.theme.Blue80
import com.example.testbioprocessor.ui.theme.TestBioProcessorTheme
import com.example.testbioprocessor.viewModel.BioViewModelNew

@Composable
fun StartScreen(
    navController: NavHostController,
    model: BioViewModelNew
) {
    // Запускаем проверку сервера при первом показе экрана
    LaunchedEffect(Unit) {
        model.checkHealth()
    }

    AppScaffold(
        showBottomBar = false,
        model
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // Отображение картинки из ресурсов
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Логотип приложения",
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Приложение разработано \"Студией АГ Квадрат\" исключительно для тестирования биопроцессора.\n" +
                        "Все операции носят ознакомительный характер. Биометрические данные и образцы не сохраняются " +
                        "и не передаются третьим лицам.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = AppFonts.customFontFamily,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                ),
                color = Blue80
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                AppButton(
                    text = "Принять",
                    onClick = {
                        if (model.uiLoginState.value.isLoginSaved) navController.navigate("serviceScreen")
                        else navController.navigate("loginScreen" )
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(20.dp))
                AppButton(
                    text = "Отказаться",
                    onClick = { System.exit(0) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            ServerStatusCompact(model)
        }
    }
}

@Composable
fun ServerStatusCompact(model: BioViewModelNew) {
    val uiState by model.uiHealthCheckState.collectAsStateWithLifecycle()

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Blue20,
            contentColor = Blue80
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Blue40),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка статуса
            Icon(
                imageVector = when (uiState) {
                    HealthRecognitionStatus.HEALTHY -> Icons.Default.CheckCircle
                    HealthRecognitionStatus.NO_HEALTHY -> Icons.Default.Close
                },
                contentDescription = "Статус сервера",
                tint = when (uiState) {
                    HealthRecognitionStatus.HEALTHY -> Color(0xFF4CAF50)
                    HealthRecognitionStatus.NO_HEALTHY -> Color(0xFFF44336)
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Статус сервера",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AppFonts.customFontFamily,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Blue80
                )

                Text(
                    text = when (uiState) {
                        HealthRecognitionStatus.HEALTHY -> "Доступен"
                        HealthRecognitionStatus.NO_HEALTHY -> "Недоступен"
                    },
                    color = when (uiState) {
                        HealthRecognitionStatus.HEALTHY -> Color(0xFF4CAF50)
                        HealthRecognitionStatus.NO_HEALTHY -> Color(0xFFF44336)
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AppFonts.customFontFamily,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
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
            model = BioViewModelNew()
        )
    }
}