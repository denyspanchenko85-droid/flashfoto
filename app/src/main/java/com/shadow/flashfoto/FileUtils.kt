package com.shadow.flashfoto

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun saveTemplate(context: Context, uri: Uri): String? {
        try {
            val templateDir = File(context.getExternalFilesDir(null), "Templates")
            if (!templateDir.exists()) templateDir.mkdirs()

            val destFile = File(templateDir, "custom_frame.png")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            return destFile.absolutePath
        } catch (e: Exception) {
            Logger.log(context, "FileUtils: Failed to save template", e)
            return null
        }
    }
}
