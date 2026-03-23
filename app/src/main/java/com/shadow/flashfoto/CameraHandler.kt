package com.shadow.flashfoto

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraHandler(private val activity: Activity) {
    var currentPhotoPath: String? = null
    val REQUEST_CAPTURE = 1
    val PERMISSION_CAMERA = 100

    fun capture() {
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startIntent()
        } else {
            activity.requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSION_CAMERA)
        }
    }

    private fun startIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = try {
            createImageFile()
        } catch (ex: Exception) {
            Logger.log(activity, "Не вдалося створити файл", ex)
            null
        }

        file?.let {
            val uri: Uri = FileProvider.getUriForFile(activity, "com.shadow.flashfoto.provider", it)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            activity.startActivityForResult(intent, REQUEST_CAPTURE)
        }
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
}
