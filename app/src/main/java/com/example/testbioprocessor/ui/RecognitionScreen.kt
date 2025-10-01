package com.example.testbioprocessor.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.testbioprocessor.camera.ImagePicker
import com.example.testbioprocessor.camera.SingleImagePicker
import com.example.testbioprocessor.ui.theme.TestBioProcessorTheme
import com.example.testbioprocessor.viewModel.BioViewModel

@Composable
fun RecognitionScreen(
    navController: NavHostController,
    viewModel: BioViewModel,
) {
    Scaffold (modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Распознование")
            SingleImagePicker(navController =navController, viewModel = viewModel)

        }

    }
    val state by viewModel.uiLoginState.collectAsStateWithLifecycle()
    CurrentUserLogin(state.login)
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun GreetingPreview222() {
    TestBioProcessorTheme {
        RecognitionScreen(
            navController = rememberNavController(),
            viewModel = BioViewModel(),
        )
    }
}