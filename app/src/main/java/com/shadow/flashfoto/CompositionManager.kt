package com.shadow.flashfoto

import android.content.Context
import android.graphics.BitmapFactory
import java.io.File

object CompositionManager {
    fun preview(context: Context, photoFile: File?, templateFile: File?, settings: SettingsManager): android.graphics.Bitmap? {
        if (photoFile == null) return null
        val photo = BitmapFactory.decodeFile(photoFile.absolutePath)
        
        // Використовуємо існуючий ImageOverlayProcessor, але з можливістю підміни файлу шаблону
        // Тимчасово підміняємо шлях у налаштуваннях для процесора
        val oldPath = settings.customTemplatePath
        if (templateFile != null) settings.customTemplatePath = templateFile.absolutePath
        
        val result = ImageOverlayProcessor.applyFrame(context, photo, settings)
        
        settings.customTemplatePath = oldPath // повертаємо як було
        return result
    }
}
