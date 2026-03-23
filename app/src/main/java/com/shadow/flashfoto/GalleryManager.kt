package com.shadow.flashfoto

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object GalleryManager {

    fun saveToGallery(context: Context, bitmap: Bitmap?): File? {
        if (bitmap == null) return null
        
        try {
            // Створюємо папку FlashFoto в загальних малюнках
            val galleryDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "FlashFoto"
            )
            if (!galleryDir.exists()) galleryDir.mkdirs()

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(galleryDir, "FlashFoto_$timeStamp.jpg")

            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            out.flush()
            out.close()

            // ПОВІДОМЛЯЄМО СИСТЕМУ: «Ей, тут нове фото, покажи його в галереї!»
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
            
            return file
        } catch (e: Exception) {
            Logger.log(context, "Failed to save to gallery", e)
            return null
        }
    }
}
