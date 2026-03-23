package com.shadow.flashfoto

import android.view.View
import android.widget.Button
import android.widget.ImageView

class InteractionManager(
    private val activity: MainActivity,
    private val camera: CameraHandler,
    private val hEdited: HistoryManager,
    private val hRaw: HistoryManager,
    private val hTpl: HistoryManager,
    private val settings: SettingsManager,
    private val workflow: WorkflowManager
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
        
        // Видалення Фото
        activity.findViewById<Button>(R.id.btnPhotoDel).setOnClickListener {
            workflow.deleteWithConfirm(if (isDual) hRaw else hEdited) { refreshPreview() }
        }

        // Шаблон навігація
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
        
        // Видалення Шаблону
        activity.findViewById<Button>(R.id.btnFrameDel).setOnClickListener {
            workflow.deleteWithConfirm(hTpl) { refreshPreview() }
        }

        // Друк
        activity.findViewById<Button>(R.id.btnPrint).setOnClickListener {
            if (isDual) {
                val bitmap = CompositionManager.generatePreview(activity, hRaw.getCurrent(), hTpl.getCurrent(), settings)
                bitmap?.let {
                    workflow.saveManual(it)
                    PrintManager.print(activity, it, settings)
                }
            } else {
                hEdited.getCurrent()?.let { file ->
                    PrintManager.printFromFile(activity, file, settings)
                }
            }
        }
        
        // Share
        activity.findViewById<View>(R.id.btnShare).setOnClickListener {
            ShareManager.share(activity, hEdited.getCurrent())
        }
        
        activity.findViewById<View>(R.id.btnSettings).setOnClickListener { 
            SettingsDialogHandler(activity, settings).show() 
        }

        refreshPreview()
    }

    fun refreshPreview() {
        val btnShare = activity.findViewById<View>(R.id.btnShare)
        
        if (settings.appMode == 1) {
            // Режим Конструктора
            val bitmap = CompositionManager.generatePreview(activity, hRaw.getCurrent(), hTpl.getCurrent(), settings)
            activity.resultImage.setImageBitmap(bitmap)
            activity.btnPrint.visibility = View.VISIBLE
            btnShare.visibility = View.GONE // Ховаємо Share у другому режимі
        } else {
            // Режим Історії
            val currentFile = hEdited.getCurrent()
            activity.display(currentFile)
            
            // Показуємо Share тільки якщо є що відправляти
            btnShare.visibility = if (currentFile != null) View.VISIBLE else View.GONE
        }
    }
}
