package com.example.testbioprocessor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.testbioprocessor.viewModel.ApiUiState
import com.example.testbioprocessor.viewModel.BioViewModelNew

@Composable
fun AlertRecognitionDialog(
    viewModel: BioViewModelNew,
    navController: NavHostController,
    onDismiss: () -> Unit
) {
    val recognitionState by viewModel.uiApiState.collectAsStateWithLifecycle()

    val info = when (recognitionState) {
        is ApiUiState.Success -> "Распознал Вас как - " + (recognitionState as ApiUiState.Success).message
        is ApiUiState.Error -> (recognitionState as ApiUiState.Error).message
        else -> "Неизвестная ошибка распознавания"
    }
    val title = when (recognitionState) {
        is ApiUiState.Success -> "Успех"
        is ApiUiState.Error -> "Неудача"
        else -> "Ошибка"
    }

    MaterialTheme {
        Column {
            val openDialog = remember { mutableStateOf(true) }

            Button(onClick = {
                openDialog.value = true
            }) {
                Text("Click me")
            }

            if (openDialog.value) {

                AlertDialog(
                    onDismissRequest = {
                        // Dismiss the dialog when the user clicks outside the dialog or on the back
                        // button. If you want to disable that functionality, simply use an empty
                        // onCloseRequest.
                        openDialog.value = false
                    },
                    title = {
                        Text(title)
                    },
                    text = {
                        Text(info)
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                openDialog.value = false
                                navController.navigate("serviceScreen")
                            }) {
                            Text("Закрыть")
                        }
                    }
                )
            }
        }

    }
}
