package com.shadow.flashfoto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import java.io.File

object ImageOverlayProcessor {
    fun applyFrame(context: Context, photo: Bitmap?, settings: SettingsManager): Bitmap? {
        if (photo == null) return null

        val w = photo.width
        val h = photo.height

        // 1. Пріоритет: вибраний у налаштуваннях
        // 2. Запасний: default_vertical.png у папці Templates
        val templatePath = settings.customTemplatePath ?: 
            File(context.getExternalFilesDir(null), "Templates/default_vertical.png").absolutePath

        val frame = BitmapFactory.decodeFile(templatePath) ?: return photo

        val scaledFrame = Bitmap.createScaledBitmap(frame, w, h, true)
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        
        canvas.drawBitmap(photo, 0f, 0f, null)
        canvas.drawBitmap(scaledFrame, 0f, 0f, null)

        frame.recycle()
        scaledFrame.recycle()
        return result
    }
}
