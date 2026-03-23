package com.shadow.flashfoto

import android.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.Toast

class InteractionManager(
    private val activity: MainActivity,
    private val camera: CameraHandler,
    private val hEdited: HistoryManager,   // Історія готових
    private val hRaw: HistoryManager,      // Історія сирих фото
    private val hFrames: HistoryManager,   // Історія шаблонів
    private val settings: SettingsManager
) {

    fun setup() {
        val rowFrame = activity.findViewById<View>(R.id.rowFrame)
        
        // Визначаємо видимість другого шару залежно від режиму
        rowFrame.visibility = if (settings.isDualLayerMode) View.VISIBLE else View.GONE

        activity.findViewById<Button>(R.id.btnCapture).setOnClickListener { camera.capture() }

        // --- ЛОГІКА ФОТО (Слой 1 або звичайна історія) ---
        activity.findViewById<Button>(R.id.btnPhotoPrev).setOnClickListener {
            val file = if (settings.isDualLayerMode) hRaw.getPrev() else hEdited.getPrev()
            updatePreview(file)
        }

        activity.findViewById<Button>(R.id.btnPhotoNext).setOnClickListener {
            val file = if (settings.isDualLayerMode) hRaw.getNext() else hEdited.getNext()
            updatePreview(file)
        }

        activity.findViewById<Button>(R.id.btnPhotoDel).setOnClickListener {
            confirmDelete(if (settings.isDualLayerMode) hRaw else hEdited)
        }

        // --- ЛОГІКА РАМКИ (Слой 2) ---
        activity.findViewById<Button>(R.id.btnFramePrev).setOnClickListener {
            hFrames.getPrev()?.let { 
                settings.customTemplatePath = it.absolutePath
                updatePreview(hRaw.getCurrent())
            }
        }

        activity.findViewById<Button>(R.id.btnFrameNext).setOnClickListener {
            hFrames.getNext()?.let { 
                settings.customTemplatePath = it.absolutePath
                updatePreview(hRaw.getCurrent())
            }
        }

        activity.findViewById<Button>(R.id.btnFrameDel).setOnClickListener { confirmDelete(hFrames) }

        // --- ДРУК ---
        activity.findViewById<Button>(R.id.btnPrint).setOnClickListener {
            if (settings.isDualLayerMode) {
                val bitmap = CompositionManager.preview(activity, hRaw.getCurrent(), hFrames.getCurrent(), settings)
                bitmap?.let { PrintManager.print(activity, it, settings) }
            } else {
                hEdited.getCurrent()?.let { PrintManager.printFromFile(activity, it, settings) }
            }
        }
        
        activity.findViewById<View>(R.id.btnSettings).setOnClickListener { SettingsDialogHandler(activity, settings).show() }
    }

    private fun updatePreview(photoFile: android.net.Uri? = null) { // Спрощено для прикладу
        if (settings.isDualLayerMode) {
            val bitmap = CompositionManager.preview(activity, hRaw.getCurrent(), hFrames.getCurrent(), settings)
            activity.resultImage.setImageBitmap(bitmap)
        } else {
            activity.display(hEdited.getCurrent())
        }
    }

    private fun confirmDelete(manager: HistoryManager) {
        AlertDialog.Builder(activity)
            .setTitle("Видалення")
            .setMessage("Видалити цей файл назавжди?")
            .setPositiveButton("Так") { _, _ -> 
                if (manager.deleteCurrent()) updatePreview()
            }
            .setNegativeButton("Ні", null).show()
    }
}
