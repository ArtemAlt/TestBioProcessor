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
import com.example.testbioprocessor.viewModel.BioViewModel
import com.example.testbioprocessor.viewModel.RecognitionUiState

@Composable
fun AlertRecognitionDialog(
    viewModel: BioViewModel,
    navController: NavHostController,
    onDismiss: () -> Unit
) {
    val recognitionState by viewModel.uiState.collectAsStateWithLifecycle()

    val info = when (recognitionState) {
        is RecognitionUiState.RecognitionSuccess  -> "Распознал Вас как - " +
                (recognitionState as RecognitionUiState.RecognitionSuccess).name + " вероятность - " +
            (recognitionState as RecognitionUiState.RecognitionSuccess).similarity
        is RecognitionUiState.Error  -> (recognitionState as RecognitionUiState.Error).message
        else -> "Неизвестная ошибка распознавания"
    }
    val title = when (recognitionState) {
        is RecognitionUiState.RecognitionSuccess  -> "Успех"
        is RecognitionUiState.Error  -> "Неудача"
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
