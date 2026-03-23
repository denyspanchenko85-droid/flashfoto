package com.shadow.flashfoto

import android.app.Activity
import android.graphics.BitmapFactory
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import java.io.File

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

            // 2. Накладання шаблону (ImageOverlayProcessor)
            val finalBitmap = ImageOverlayProcessor.applyFrame(activity, photo) ?: return

            // 3. Візуалізація результату на екрані
            activity.runOnUiThread {
                resultImage.setImageBitmap(finalBitmap)
                btnPrint.visibility = View.VISIBLE
            }

            // 4. Збереження в системну Галерею (GalleryManager)
            val isSaved = GalleryManager.saveToGallery(activity, finalBitmap)

            // 5. Оновлення списку історії
            if (isSaved) {
                history.updateHistory()
            }

            // 6. Друк (PrintManager - тепер він сам вирішує, чи слати "тихо")
            PrintManager.print(activity, finalBitmap, settings)

            // 7. Очищення: видаляємо сирий оригінал, якщо це ввімкнено в налаштуваннях
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
}
