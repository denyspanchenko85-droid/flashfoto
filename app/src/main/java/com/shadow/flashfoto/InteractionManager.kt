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
        val rowFrame = activity.findViewById<View>(R.id.layerTemplates)
        val isDual = settings.appMode == 1
        
        rowFrame.visibility = if (isDual) View.VISIBLE else View.GONE

        activity.findViewById<Button>(R.id.btnCapture).setOnClickListener { camera.capture() }

        // Фото навігація
        activity.findViewById<Button>(R.id.btnPrev).setOnClickListener {
            val file = if (isDual) hRaw.getPrev() else hEdited.getPrev()
            refreshPreview(file)
        }

        activity.findViewById<Button>(R.id.btnNext).setOnClickListener {
            val file = if (isDual) hRaw.getNext() else hEdited.getNext()
            refreshPreview(file)
        }

        activity.findViewById<Button>(R.id.btnDelete).setOnClickListener {
            confirmDelete(if (isDual) hRaw else hEdited)
        }

        // Шаблон навігація (Layer 2)
        activity.findViewById<Button>(R.id.btnTplPrev).setOnClickListener {
            hTpl.getPrev()?.let { 
                settings.customTemplatePath = it.absolutePath
                refreshPreview(hRaw.getCurrent())
            }
        }

        activity.findViewById<Button>(R.id.btnTplNext).setOnClickListener {
            hTpl.getNext()?.let { 
                settings.customTemplatePath = it.absolutePath
                refreshPreview(hRaw.getCurrent())
            }
        }

        activity.findViewById<Button>(R.id.btnTplDelete).setOnClickListener { confirmDelete(hTpl) }

        // Друк
        activity.findViewById<Button>(R.id.btnPrint).setOnClickListener {
            if (isDual) {
                val bitmap = CompositionManager.generatePreview(activity, hRaw.getCurrent(), settings.customTemplatePath, settings)
                bitmap?.let { PrintManager.print(activity, it, settings) }
            } else {
                hEdited.getCurrent()?.let { PrintManager.printFromFile(activity, it, settings) }
            }
        }
        
        activity.findViewById<View>(R.id.btnSettings).setOnClickListener { 
            SettingsDialogHandler(activity, settings).show() 
        }
    }

    private fun refreshPreview(file: File? = null) {
        if (settings.appMode == 1) {
            val bitmap = CompositionManager.generatePreview(activity, hRaw.getCurrent(), settings.customTemplatePath, settings)
            activity.resultImage.setImageBitmap(bitmap)
            activity.btnPrint.visibility = View.VISIBLE
        } else {
            activity.display(file ?: hEdited.getCurrent())
        }
    }

    private fun confirmDelete(manager: HistoryManager) {
        AlertDialog.Builder(activity)
            .setTitle("Видалення")
            .setMessage("Видалити цей файл?")
            .setPositiveButton("Так") { _, _ -> 
                if (manager.deleteCurrent()) refreshPreview(manager.getCurrent())
            }
            .setNegativeButton("Ні", null).show()
    }
}
