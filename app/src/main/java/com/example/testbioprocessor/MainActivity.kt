package com.example.testbioprocessor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testbioprocessor.camera.RegisterScreen
import com.example.testbioprocessor.camera.SingleImagePicker
import com.example.testbioprocessor.login.LoginScreen
import com.example.testbioprocessor.model.SendScreenType
import com.example.testbioprocessor.ui.DeleteScreen
import com.example.testbioprocessor.ui.SendScreenNew
import com.example.testbioprocessor.ui.ServicesScreen
import com.example.testbioprocessor.ui.StartScreen
import com.example.testbioprocessor.ui.theme.TestBioProcessorTheme
import com.example.testbioprocessor.viewModel.BioViewModelNew

class MainActivity : ComponentActivity() {
    private val model: BioViewModelNew by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            TestBioProcessorTheme {
                NavigationApp(navController, model)
            }
        }
    }
}

@Composable
fun NavigationApp(navController: NavHostController, model: BioViewModelNew) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            StartScreen(navController = navController, model)
        }
        composable("loginScreen") {
            LoginScreen(onContinue = {} , model, navController)
        }
        composable("registerScreen") {
            RegisterScreen(navController = navController, model = model)
        }
        composable("sendRegistrationScreen") {
            SendScreenNew(navController = navController, model, SendScreenType.REGISTRATION)
        }
        composable("sendRecognitionScreen") {
            SendScreenNew(navController = navController, model, SendScreenType.RECOGNITION)
        }
        composable("serviceScreen") {
            ServicesScreen(navController = navController, model)
        }
        composable("deleteScreen") {
            DeleteScreen(navController = navController, model)
        }
        composable("recognitionScreen") {
            SingleImagePicker(model, navController)
        }
    }
}