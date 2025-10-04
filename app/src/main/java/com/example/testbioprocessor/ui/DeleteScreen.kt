package com.example.testbioprocessor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.testbioprocessor.viewModel.BioViewModelNew

@Composable
fun DeleteScreen(
    navController: NavHostController,
    viewModel: BioViewModelNew,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Удалить вектор")
            Row {
                Button(
                    content = { Text("Сдать") },
                    onClick = { navController.navigate("registerScreen") }
                )
                Spacer(modifier = Modifier.width(20.dp))
                Button(
                    content = { Text("Проверить") },
                    onClick = { navController.navigate("checkScreen") }
                )
            }
            CurrentUserLogin(viewModel)
        }
    }
}
