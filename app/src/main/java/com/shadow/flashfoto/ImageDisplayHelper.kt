package com.shadow.flashfoto

import android.graphics.BitmapFactory
import android.view.View
import android.widget.Button
import android.widget.ImageView
import java.io.File

object ImageDisplayHelper {
    fun show(file: File?, targetView: ImageView, actionButton: Button) {
        if (file == null || !file.exists()) return
        
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            targetView.setImageBitmap(bitmap)
            actionButton.visibility = View.VISIBLE
        } catch (e: Exception) {
            // Тут можна додати лог, якщо бітмап битий
        }
    }
}
