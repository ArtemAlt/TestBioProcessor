package com.example.testbioprocessor.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.testbioprocessor.camera.SingleImagePicker
import com.example.testbioprocessor.ui.theme.TestBioProcessorTheme
import com.example.testbioprocessor.viewModel.BioViewModelNew

@Composable
fun RecognitionScreen(
    navController: NavHostController,
    viewModel: BioViewModelNew,
) {
    val state by viewModel.uiLoginState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    // Следим за состоянием распознавания из ViewModel
    val recognitionState by viewModel.uiApiState.collectAsStateWithLifecycle()

//    // Показываем диалог когда распознавание завершено
//    LaunchedEffect(recognitionState) {
//        if (recognitionState is RecognitionUiState.RecognitionSuccess
//            || recognitionState is RecognitionUiState.Error
//            || recognitionState is RecognitionUiState.Idle){
//            showDialog = true
//        }
//    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Распознавание",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SingleImagePicker(
                model = viewModel,
                onRecognitionComplete = {
                    // Эта функция теперь вызывается из SingleImagePicker
                    showDialog = true
                }
            )
        }
    }

    // Диалог распознавания
    if (showDialog) {
        AlertRecognitionDialog(
            navController = navController,
            viewModel = viewModel,
            onDismiss = { showDialog = false }
        )
    }

    // Информация о пользователе
    CurrentUserLogin(viewModel)
}
