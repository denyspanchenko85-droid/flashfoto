package com.shadow.flashfoto

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    // Копіює вбудований шаблон з Drawable у папку Templates
    fun copyRawToTemplates(context: Context, resId: Int, fileName: String) {
        val dir = File(context.getExternalFilesDir(null), "Templates")
        if (!dir.exists()) dir.mkdirs()
        
        val destFile = File(dir, fileName)
        if (!destFile.exists()) {
            context.resources.openRawResource(resId).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    // Копіює вибраний користувачем файл (ззовні) у нашу папку
    fun saveCustomTemplate(context: Context, uri: Uri): String? {
        try {
            val templateDir = File(context.getExternalFilesDir(null), "Templates")
            val fileName = "custom_${System.currentTimeMillis()}.png"
            val destFile = File(templateDir, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output -> input.copyTo(output) }
            }
            return destFile.absolutePath
        } catch (e: Exception) {
            Logger.log(context, "Save template error", e)
            return null
        }
    }
}
