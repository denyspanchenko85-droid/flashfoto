package com.shadow.flashfoto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

object CompositionManager {
    fun generatePreview(context: Context, photoFile: File?, templateFile: File?, settings: SettingsManager): Bitmap? {
        if (photoFile == null || !photoFile.exists()) return null
        
        val photo = BitmapFactory.decodeFile(photoFile.absolutePath) ?: return null
        
        // Тимчасово підміняємо шлях у налаштуваннях для процесора
        val oldPath = settings.customTemplatePath
        if (templateFile != null) settings.customTemplatePath = templateFile.absolutePath
        
        val result = ImageOverlayProcessor.applyFrame(context, photo, settings)
        
        settings.customTemplatePath = oldPath // повертаємо оригінал
        return result
    }
}
