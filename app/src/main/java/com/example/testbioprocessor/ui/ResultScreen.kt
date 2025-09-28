package com.example.testbioprocessor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.testbioprocessor.ui.theme.TestBioProcessorTheme

@Composable
fun ResultScreen(navController: NavHostController) {
    Scaffold (modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "CheckScreen")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview9() {
    TestBioProcessorTheme {
        ResultScreen(navController = rememberNavController())
    }
}