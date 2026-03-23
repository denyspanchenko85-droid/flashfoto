package com.shadow.flashfoto

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream

class WorkflowManager(
    private val activity: Activity,
    private val settings: SettingsManager,
    private val history: HistoryManager
) {

    fun execute(rawPath: String?, resultImage: ImageView, btnPrint: Button) {
        if (rawPath == null) return

        try {
            val photo = BitmapFactory.decodeFile(rawPath) ?: return
            val finalBitmap = ImageOverlayProcessor.applyFrame(activity, photo, settings) ?: return

            activity.runOnUiThread {
                resultImage.setImageBitmap(finalBitmap)
                btnPrint.visibility = android.view.View.VISIBLE
            }

            // 1. Зберігаємо в Edited (для Історії)
            saveInternal(finalBitmap, "Edited")
            
            // 2. Експорт в публічну Галерею
            GalleryManager.saveToGallery(activity, finalBitmap)

            history.updateHistory()
            PrintManager.print(activity, finalBitmap, settings)

            // 3. Обробка Raw
            if (!settings.isKeepOriginalEnabled) {
                File(rawPath).delete()
            }
        } catch (e: Exception) {
            Logger.log(activity, "Workflow Error", e)
        }
    }

    private fun saveInternal(bitmap: Bitmap, folder: String) {
        val dir = File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), folder)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "IMG_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
    }
}
