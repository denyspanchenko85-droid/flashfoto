package com.shadow.flashfoto

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
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
            // 1. Декодування оригіналу
            val photo = BitmapFactory.decodeFile(rawPath) ?: return

            // 2. ОНОВЛЕНО: Накладання шаблону з урахуванням налаштувань
            val finalBitmap = ImageOverlayProcessor.applyFrame(activity, photo, settings) ?: return

            // 3. Візуалізація результату
            activity.runOnUiThread {
                resultImage.setImageBitmap(finalBitmap)
                btnPrint.visibility = View.VISIBLE
            }

            // 4. "ІНШИЙ ШЛЯХ": Зберігаємо копію у ВНУТРІШНЮ папку для роботи Історії
            // Це виправляє помилку "Found 0 photos", бо сюди доступ завжди відкритий.
            saveToInternalStorage(finalBitmap)

            // 5. Копіюємо в системну Галерею (для користувача)
            GalleryManager.saveToGallery(activity, finalBitmap)

            // 6. Оновлюємо список історії
            history.updateHistory()

            // 7. Друк
            PrintManager.print(activity, finalBitmap, settings)

            // 8. Очищення оригіналу
            if (!settings.isKeepOriginalEnabled) {
                val rawFile = File(rawPath)
                if (rawFile.exists()) rawFile.delete()
            }

        } catch (e: Exception) {
            Logger.log(activity, "Workflow Execution Failed", e)
            activity.runOnUiThread {
                Toast.makeText(activity, "Помилка процесу: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Допоміжний метод для збереження в приватну папку додатка
    private fun saveToInternalStorage(bitmap: Bitmap) {
        try {
            val internalDir = File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FlashFoto")
            if (!internalDir.exists()) internalDir.mkdirs()

            val file = File(internalDir, "IMG_${System.currentTimeMillis()}.jpg")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            Logger.log(activity, "Internal save error", e)
        }
    }
}
