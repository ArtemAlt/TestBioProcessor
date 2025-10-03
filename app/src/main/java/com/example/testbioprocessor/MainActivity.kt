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
import com.example.testbioprocessor.camera.SingleImagePicker
import com.example.testbioprocessor.login.LoginScreen
import com.example.testbioprocessor.ui.CheckScreen
import com.example.testbioprocessor.ui.DeleteScreen
import com.example.testbioprocessor.ui.RecognitionScreen
import com.example.testbioprocessor.ui.RegisterScreen
import com.example.testbioprocessor.ui.ResultScreen
//import com.example.testbioprocessor.ui.SendScreen
import com.example.testbioprocessor.ui.SendScreenNew
import com.example.testbioprocessor.ui.ServicesScreen
import com.example.testbioprocessor.ui.StartScreen
import com.example.testbioprocessor.ui.theme.TestBioProcessorTheme
import com.example.testbioprocessor.viewModel.BioViewModel

class MainActivity : ComponentActivity() {
    private val model: BioViewModel by viewModels()
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
fun NavigationApp(navController: NavHostController, model: BioViewModel) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            StartScreen(navController = navController, model)
        }
        composable("loginScreen") {
            LoginScreen(onContinue = {} , model, navController)
        }
        composable("registerScreen") {
            RegisterScreen(navController = navController, model)
        }
        composable("checkScreen") {
            CheckScreen(navController = navController)
        }
        composable("resultScreen") {
            ResultScreen(navController = navController, model)
        }
        composable("sendScreen") {
            SendScreenNew(navController = navController, model)
        }
        composable("serviceScreen") {
            ServicesScreen(navController = navController, model)
        }
        composable("deleteScreen") {
            DeleteScreen(navController = navController, model)
        }
        composable("recognitionScreen") {
            RecognitionScreen(navController = navController, model)
        }
        composable("singleImagePicker") {
            SingleImagePicker(viewModel = model)
        }
    }
}