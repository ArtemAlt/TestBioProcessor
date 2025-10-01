package com.example.testbioprocessor.camera

import android.content.Context
import android.net.Uri
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.example.testbioprocessor.viewModel.BioViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImagePicker(navController: NavHostController, viewModel: BioViewModel) {
    val context = LocalContext.current
    var currentPhotoUri by remember { mutableStateOf(value = Uri.EMPTY) }
    val file = context.createImageFile()
    val authority = "${context.packageName}.fileprovider"
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context), authority, file
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) currentPhotoUri = uri
        }
    )

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { granted ->
            if (granted) {
                cameraLauncher.launch(uri)
            } else print("camera permission is denied")
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(visible = currentPhotoUri.toString().isNotEmpty()) {
            // from coil library
            coil.compose.AsyncImage(
                modifier = Modifier.padding(120.dp),
                model = currentPhotoUri,
                contentDescription = null
            )
        }
        Button(onClick = cameraPermissionState::launchPermissionRequest) {
            Text(text = "Сделать фото")
        }
    }
}

fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    return File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir /* directory */
    )
}