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

        // Вибір ресурсу: якщо ширина більше за висоту — горизонтальна рамка
        val resId = if (w > h) R.drawable.easter_horiz_1 else R.drawable.easter_vert_1

        // Завантажуємо рамку з ресурсів
        val frame = BitmapFactory.decodeResource(context.resources, resId) ?: return photo
        
        // Масштабуємо рамку під розмір фото
        val scaledFrame = Bitmap.createScaledBitmap(frame, w, h, true)

        // Створюємо фінальний Bitmap
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Малюємо фото, потім рамку зверху
        canvas.drawBitmap(photo, 0f, 0f, null)
        canvas.drawBitmap(scaledFrame, 0f, 0f, null)

        // Чистимо пам'ять від тимчасових об'єктів
        frame.recycle()
        scaledFrame.recycle()

        return result
    }
}
