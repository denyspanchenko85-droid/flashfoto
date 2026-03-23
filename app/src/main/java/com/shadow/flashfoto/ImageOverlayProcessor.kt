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

        // Визначаємо джерело шаблону
        val frame = if (settings.customTemplatePath != null) {
            val file = File(settings.customTemplatePath!!)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                getDefaultFrame(context, w, h)
            }
        } else {
            getDefaultFrame(context, w, h)
        } ?: return photo

        val scaledFrame = Bitmap.createScaledBitmap(frame, w, h, true)
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        
        canvas.drawBitmap(photo, 0f, 0f, null)
        canvas.drawBitmap(scaledFrame, 0f, 0f, null)

        frame.recycle()
        scaledFrame.recycle()
        return result
    }

    private fun getDefaultFrame(context: Context, w: Int, h: Int): Bitmap? {
        val resId = if (w > h) R.drawable.easter_horiz_1 else R.drawable.easter_vert_1
        return BitmapFactory.decodeResource(context.resources, resId)
    }
}
