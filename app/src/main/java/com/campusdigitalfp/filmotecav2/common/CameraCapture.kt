package com.campusdigitalfp.filmotecav2.common

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.campusdigitalfp.filmotecav2.utils.saveImageToAppFolder
import com.campusdigitalfp.filmotecav2.viewmodel.FilmViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraCapture(viewModel: FilmViewModel, filmId: String, currentImagen: String) {
    val context = LocalContext.current

    var imageUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var savedImageUri by remember { mutableStateOf<Uri?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val imageUri = imageUriString?.let { Uri.parse(it) }
        if (success && imageUri != null) {
            val savedUri = saveImageToAppFolder(context, imageUri)
            savedUri?.let { galleryUri ->
                savedImageUri = galleryUri
                viewModel.updateFilmImage(filmId, galleryUri.toString())
            }
        }
    }

    val createImageFile: () -> Uri? = {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: IOException) {
            Log.e("createImageFile", "Error al crear el archivo de imagen", e)
            null
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                if (!hasCameraPermission) {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                    )
                } else {
                    createImageFile()?.let { uri ->
                        imageUriString = uri.toString()
                        cameraLauncher.launch(uri)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(text = "Capturar fotografía")
        }
    }
}
