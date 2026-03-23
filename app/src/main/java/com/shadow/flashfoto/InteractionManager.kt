package com.shadow.flashfoto

import android.view.View
import android.widget.Button

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
        refreshPreview()

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
        
        // Видалення Фото (викликає Workflow)
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
        
        // Видалення Шаблону (викликає Workflow)
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
                hEdited.getCurrent()?.let { PrintManager.printFromFile(activity, it, settings) }
            }
        }
        
        activity.findViewById<View>(R.id.btnSettings).setOnClickListener { 
            SettingsDialogHandler(activity, settings).show() 
        }
    }

    fun refreshPreview() {
        if (settings.appMode == 1) {
            val bitmap = CompositionManager.generatePreview(activity, hRaw.getCurrent(), hTpl.getCurrent(), settings)
            activity.resultImage.setImageBitmap(bitmap)
            activity.btnPrint.visibility = View.VISIBLE
        } else {
            activity.display(hEdited.getCurrent())
        }
    }
}
