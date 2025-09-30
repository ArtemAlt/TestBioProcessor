package com.example.testbioprocessor.ui

import android.annotation.SuppressLint
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.testbioprocessor.ui.theme.TestBioProcessorTheme
import com.example.testbioprocessor.viewModel.BioViewModel

@Composable
fun DeleteScreen(
    navController: NavHostController,
    viewModel: BioViewModel,
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
            val state by viewModel.uiLoginState.collectAsStateWithLifecycle()
            CurrentUserLogin(state.login)
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun GreetingPreview22() {
    TestBioProcessorTheme {
        DeleteScreen(
            navController = rememberNavController(),
            viewModel = BioViewModel(),
        )
    }
}