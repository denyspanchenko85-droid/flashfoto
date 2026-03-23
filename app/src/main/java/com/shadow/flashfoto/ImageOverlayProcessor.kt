package com.shadow.flashfoto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas

object ImageOverlayProcessor {
    fun applyFrame(context: Context, photo: Bitmap?): Bitmap? {
        if (photo == null) return null

        val w = photo.width
        val h = photo.height
        
        // Вибір ресурсу за орієнтацією
        val resId = if (w > h) R.drawable.easter_horiz_1 else R.drawable.easter_vert_1
        
        val frame = BitmapFactory.decodeResource(context.resources, resId) ?: return photo
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
