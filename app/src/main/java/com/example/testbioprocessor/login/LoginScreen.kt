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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.testbioprocessor.viewModel.BioViewModelNew

@Composable
fun LoginScreen(
    onContinue: (String) -> Unit,
    viewModel: BioViewModelNew,
    navController: NavHostController,
) {
    val uiState by viewModel.uiLoginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

//    // Показ сообщений через Snackbar
//    LaunchedEffect(uiState) {
//        if (uiState.showSuccessMessage) {
//            snackbarHostState.showSnackbar("Логин сохранен!")
//            viewModel.clearMessages()
//        }
//    }

//    LaunchedEffect(uiState.showResetMessage) {
//        if (uiState.showResetMessage) {
//            snackbarHostState.showSnackbar("Логин сброшен!")
//            viewModel.clearMessages()
//        }
//    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Тот же контент что и выше
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Логин",
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ваш логин -" + uiState.login,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Введите ваш логин",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(value = uiState.login,
                onValueChange = { viewModel.onLoginChange(it) },
                label = { Text(text = "Логин") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.saveLogin()
                    onContinue(uiState.login.trim())
                    navController.navigate("serviceScreen")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.login.trim().length >= 3
            ) {
                Text("Продолжить")
            }

            if (uiState.isLoginSaved) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { viewModel.resetLogin() }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сбросить логин")
                }
            }
        }
    }
}