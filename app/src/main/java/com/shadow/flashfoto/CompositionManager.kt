package com.shadow.flashfoto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.File

object CompositionManager {
    fun generatePreview(context: Context, photoFile: File?, templateFile: File?, settings: SettingsManager): Bitmap? {
        // ПУНКТ 3: Якщо фото немає, створюємо темну заглушку, щоб бачити рамку
        val photo = if (photoFile != null && photoFile.exists()) {
            BitmapFactory.decodeFile(photoFile.absolutePath)
        } else {
            Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.parseColor("#222222")) // Темно-сірий фон
            }
        } ?: return null
        
        val oldPath = settings.customTemplatePath
        if (templateFile != null) settings.customTemplatePath = templateFile.absolutePath
        
        val result = ImageOverlayProcessor.applyFrame(context, photo, settings)
        
        settings.customTemplatePath = oldPath
        return result
    }
}
