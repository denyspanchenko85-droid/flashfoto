package com.shadow.flashfoto

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

class CameraHandler(private val activity: Activity) {
    val REQUEST_CAPTURE = 1
    val PERMISSION_CAMERA = 101
    var currentPhotoPath: String? = null

    fun capture() {
        if (activity.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(arrayOf(android.Manifest.permission.CAMERA), PERMISSION_CAMERA)
            return
        }

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
            val photoFile = try {
                createImageFile()
            } catch (ex: Exception) {
                Logger.log(activity, "CameraHandler: Error creating raw file", ex)
                null
            }

            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.provider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                activity.startActivityForResult(takePictureIntent, REQUEST_CAPTURE)
            }
        }
    }

    private fun createImageFile(): File {
        // Створюємо папку Raw всередині приватного сховища
        val storageDir = File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Raw")
        if (!storageDir.exists()) storageDir.mkdirs()

        return File.createTempFile("RAW_${System.currentTimeMillis()}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
}
