package com.shadow.flashfoto

import android.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.Toast
import java.io.File

class InteractionManager(
    private val activity: MainActivity,
    private val camera: CameraHandler,
    private val hEdited: HistoryManager,
    private val hRaw: HistoryManager,
    private val hTpl: HistoryManager,
    private val settings: SettingsManager
) {

    fun setup() {
        val rowFrame = activity.findViewById<View>(R.id.rowFrame)
        val isDual = settings.appMode == 1
        
        rowFrame.visibility = if (isDual) View.VISIBLE else View.GONE

        activity.findViewById<Button>(R.id.btnCapture).setOnClickListener { camera.capture() }

        // Фото навігація
        activity.findViewById<Button>(R.id.btnPhotoPrev).setOnClickListener {
            if (isDual) hRaw.getPrev() else hEdited.getPrev()
            refreshPreview()
        }
        activity.findViewById<Button>(R.id.btnPhotoNext).setOnClickListener {
            if (isDual) hRaw.getNext() else hEdited.getNext()
            refreshPreview()
        }
        activity.findViewById<Button>(R.id.btnPhotoDel).setOnClickListener {
            confirmDelete(if (isDual) hRaw else hEdited)
        }

        // Шаблон навігація (Layer 2)
        activity.findViewById<Button>(R.id.btnFramePrev).setOnClickListener {
            hTpl.getPrev()?.let { 
                settings.customTemplatePath = it.absolutePath
                refreshPreview()
            }
        }
        activity.findViewById<Button>(R.id.btnFrameNext).setOnClickListener {
            hTpl.getNext()?.let { 
                settings.customTemplatePath = it.absolutePath
                refreshPreview()
            }
        }
        activity.findViewById<Button>(R.id.btnFrameDel).setOnClickListener { confirmDelete(hTpl) }

        // Друк
        activity.findViewById<Button>(R.id.btnPrint).setOnClickListener {
            if (isDual) {
                val bitmap = CompositionManager.generatePreview(activity, hRaw.getCurrent(), hTpl.getCurrent(), settings)
                bitmap?.let { PrintManager.print(activity, it, settings) }
            } else {
                hEdited.getCurrent()?.let { PrintManager.printFromFile(activity, it, settings) }
            }
        }
        
        activity.findViewById<View>(R.id.btnSettings).setOnClickListener { 
            SettingsDialogHandler(activity, settings).show() 
        }
    }

    private fun refreshPreview() {
        if (settings.appMode == 1) {
            val bitmap = CompositionManager.generatePreview(activity, hRaw.getCurrent(), hTpl.getCurrent(), settings)
            activity.resultImage.setImageBitmap(bitmap)
            activity.btnPrint.visibility = View.VISIBLE
        } else {
            activity.display(hEdited.getCurrent())
        }
    }

    private fun confirmDelete(manager: HistoryManager) {
        AlertDialog.Builder(activity)
            .setTitle("Видалення")
            .setMessage("Видалити цей файл?")
            .setPositiveButton("Так") { _, _ -> 
                if (manager.deleteCurrent()) refreshPreview()
            }
            .setNegativeButton("Ні", null).show()
    }
}
