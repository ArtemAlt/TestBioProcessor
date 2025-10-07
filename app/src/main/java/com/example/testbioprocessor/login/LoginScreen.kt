package com.example.testbioprocessor.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.testbioprocessor.ui.AppButton
import com.example.testbioprocessor.ui.AppButtonType
import com.example.testbioprocessor.ui.AppFonts
import com.example.testbioprocessor.ui.Blue20
import com.example.testbioprocessor.ui.Blue40
import com.example.testbioprocessor.ui.Blue60
import com.example.testbioprocessor.ui.Blue80
import com.example.testbioprocessor.ui.White
import com.example.testbioprocessor.ui.custom.AppScaffold
import com.example.testbioprocessor.viewModel.BioViewModelNew

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onContinue: (String) -> Unit,
    viewModel: BioViewModelNew,
    navController: NavHostController,
) {
    val uiState by viewModel.uiLoginState.collectAsStateWithLifecycle()
    val currentLogin = uiState.login

    var localLogin by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        localLogin = currentLogin
    }

    AppScaffold(model = viewModel, showBottomBar = true) { paddingValues ->
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
                text = "Авторизация",
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

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Иконка и заголовок
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    color = Blue40.copy(alpha = 0.2f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Пользователь",
                                tint = Blue80,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = if (currentLogin.isNotEmpty()) "Изменить логин" else "Введите ваш логин",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = AppFonts.customFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Blue80,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Поле ввода
                    OutlinedTextField(
                        value = localLogin,
                        onValueChange = { localLogin = it },
                        label = {
                            Text(
                                "Логин",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = AppFonts.customFontFamily
                                )
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Blue60,
                            unfocusedBorderColor = Blue40,
                            focusedLabelColor = Blue60,
                            unfocusedLabelColor = Blue40,
                            cursorColor = Blue60,
                            focusedTextColor = Blue80,
                            unfocusedTextColor = Blue80,
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Подсказка
                    Text(
                        text = "Логин должен содержать не менее 3 символов",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = AppFonts.customFontFamily
                        ),
                        color = Blue80.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    // Кнопка
                    AppButton(
                        onClick = {
                            if (localLogin.trim().length >= 3) {
                                viewModel.saveLogin(localLogin.trim())
                                onContinue(localLogin.trim())
                                navController.navigate("serviceScreen")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = localLogin.trim().length >= 3,
                        buttonType = AppButtonType.PRIMARY,
                        text = if (currentLogin.isNotEmpty()) "Обновить" else "Продолжить",
                        icon = Icons.Default.ArrowForward
                    )
                }
            }
        }
    }
}