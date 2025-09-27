package com.example.testbioprocessor.login

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.testbioprocessor.ui.theme.TestBioProcessorTheme
import com.example.testbioprocessor.viewModel.BioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onContinue: (String) -> Unit,
    viewModel: BioViewModel,
    navController: NavHostController,
) {
    val uiState by viewModel.uiLoginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Показ сообщений через Snackbar
    LaunchedEffect(uiState.showSuccessMessage) {
        if (uiState.showSuccessMessage) {
            snackbarHostState.showSnackbar("Логин сохранен!")
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.showResetMessage) {
        if (uiState.showResetMessage) {
            snackbarHostState.showSnackbar("Логин сброшен!")
            viewModel.clearMessages()
        }
    }

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
                    navController.navigate("previewScreen")
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

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun GreetingPreview8() {
    TestBioProcessorTheme {
        LoginScreen({},
            navController = rememberNavController(),
            viewModel = BioViewModel()
        )
    }
}