package com.campusdigitalfp.filmotecav2.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.campusdigitalfp.filmotecav2.network.ApiService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun saveImageToAppFolder(context: Context, imageUri: Uri): Uri? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val directory = File(context.filesDir, "MyAppImages")

    if (!directory.exists() && !directory.mkdirs()) {
        Log.e("saveImageToAppFolder", "No se pudo crear el directorio")
        return null
    }

    val file = File(directory, "IMG_$timeStamp.jpg")

    return try {
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        Uri.fromFile(file)
    } catch (e: IOException) {
        Log.e("saveImageToAppFolder", "Error al guardar la imagen", e)
        null
    }
}

suspend fun uploadImageToServer(imageUri: Uri, context: Context): Uri? {
    val file = File(imageUri.path ?: return null)

    return try {
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.68.105/") //  IP de mi servidor XAMPP
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val response = apiService.uploadImage(multipartBody)

        if (response.isSuccessful) {
            val jsonResponse = response.body()?.string()
            Log.d("Upload", "Respuesta del servidor: $jsonResponse")
            val jsonObject = JSONObject(jsonResponse ?: "")
            val imageUrl = jsonObject.optString("url", "")
            if (imageUrl.isNotEmpty()) {
                Uri.parse(imageUrl)
            } else {
                Log.e("Upload", "No se encontró una URL válida en la respuesta")
                null
            }
        } else {
            Log.e("Upload", "Código de error HTTP: ${response.code()}")
            null
        }
    } catch (e: Exception) {
        Log.e("Upload", "Error al subir imagen", e)
        null
    }
}

fun saveVideoToAppFolder(context: Context, videoUri: Uri): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val appDirectory = File(context.filesDir, "MyAppVideos")
    if (!appDirectory.exists()) {
        appDirectory.mkdirs()
    }

    val videoFile = File(appDirectory, "VID_$timeStamp.mp4")

    val inputStream = context.contentResolver.openInputStream(videoUri)
    val outputStream = FileOutputStream(videoFile)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    return Uri.fromFile(videoFile)
}