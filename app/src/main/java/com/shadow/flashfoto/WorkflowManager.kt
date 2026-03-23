package com.shadow.flashfoto

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.view.View
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
                btnPrint.visibility = View.VISIBLE
            }

            saveAndExport(finalBitmap)
            PrintManager.print(activity, finalBitmap, settings)

            if (!settings.isKeepOriginalEnabled) File(rawPath).delete()
        } catch (e: Exception) {
            Logger.log(activity, "Workflow Error", e)
        }
    }

    fun saveManual(bitmap: Bitmap) {
        saveAndExport(bitmap)
    }

    // ТЕПЕР ТУТ: Логіка видалення з підтвердженням
    fun deleteWithConfirm(manager: HistoryManager, onDeleted: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Видалення")
            .setMessage("Видалити цей файл назавжди?")
            .setPositiveButton("Так") { _, _ ->
                if (manager.deleteCurrent()) {
                    onDeleted() // Викликаємо оновлення UI після успішного видалення
                }
            }
            .setNegativeButton("Ні", null)
            .show()
    }

    private fun saveAndExport(bitmap: Bitmap) {
        try {
            val editedDir = File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Edited")
            if (!editedDir.exists()) editedDir.mkdirs()

            val prefix = if (settings.appMode == 1) "MIX_" else "IMG_"
            val file = File(editedDir, "${prefix}${System.currentTimeMillis()}.jpg")
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            GalleryManager.saveToGallery(activity, bitmap)
            history.updateHistory()
        } catch (e: Exception) {
            Logger.log(activity, "Save failed", e)
        }
    }
}
