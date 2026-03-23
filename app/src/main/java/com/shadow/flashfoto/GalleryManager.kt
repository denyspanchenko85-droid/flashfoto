package com.shadow.flashfoto

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import java.io.OutputStream

object GalleryManager {

    fun saveToGallery(context: Context, bitmap: Bitmap?): Boolean {
        if (bitmap == null) return false
        
        val filename = "FlashFoto_${System.currentTimeMillis()}.jpg"
        var out: OutputStream? = null

        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/FlashFoto")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val imageUri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
                contentValues
            )

            imageUri?.let { uri ->
                out = context.contentResolver.openOutputStream(uri)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out!!)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                }
                return true
            }
        } catch (e: Exception) {
            Logger.log(context, "MediaStore Save Failed", e)
        } finally {
            out?.close()
        }
        return false
    }
}
