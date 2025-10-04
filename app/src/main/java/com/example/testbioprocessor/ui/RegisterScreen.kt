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
import androidx.navigation.NavHostController
import com.example.testbioprocessor.camera.MultiImagePicker
import com.example.testbioprocessor.viewModel.BioViewModelNew

@Composable
fun RegisterScreen(navController: NavHostController,
                   viewModel: BioViewModelNew) {
    Scaffold (modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "RegisterScreen")
            MultiImagePicker(navController = navController, model = viewModel)
            CurrentUserLogin(model = viewModel)
        }
    }
}

