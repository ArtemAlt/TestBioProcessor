package com.example.testbioprocessor.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.testbioprocessor.ui.AppButton
import com.example.testbioprocessor.ui.AppButtonType
import com.example.testbioprocessor.ui.AppFonts
import com.example.testbioprocessor.ui.custom.AppScaffold
import com.example.testbioprocessor.viewModel.BioViewModelNew

@Composable
fun LoginScreen(
    onContinue: (String) -> Unit,
    viewModel: BioViewModelNew,
    navController: NavHostController,
) {
    var localLogin by remember { mutableStateOf("") }

    // Загружаем сохраненный логин
    LaunchedEffect(Unit) {
        localLogin = viewModel.getSavedLogin()
    }

    AppScaffold(model = viewModel, showBottomBar = true) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Введите ваш логин",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = AppFonts.customFontFamily,
                    lineHeight = 24.sp),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = localLogin,
                onValueChange = { localLogin = it },
                label = { Text("Логин") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                text = "Продолжить"
            )
        }
    }
}