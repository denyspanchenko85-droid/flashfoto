package com.shadow.flashfoto

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import java.io.File

class MainActivity : Activity() {
    lateinit var settings: SettingsManager
    lateinit var history: HistoryManager
    lateinit var camera: CameraHandler
    lateinit var workflow: WorkflowManager
    lateinit var interaction: InteractionManager
    
    lateinit var resultImage: ImageView
    lateinit var btnPrint: Button
    
    private val REQUEST_PICK_TEMPLATE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Ініціалізація менеджерів
        settings = SettingsManager(this)
        camera = CameraHandler(this)
        
        // 2. Весь "бруд" ініціалізації папок тепер тут
        Bootstrapper.run(this, settings)
        
        val editedDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Edited")
        history = HistoryManager(this, editedDir)
        workflow = WorkflowManager(this, settings, history)

        // 3. UI
        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        // 4. Setup Interaction
        interaction = InteractionManager(this, camera, history, settings)
        interaction.setup()

        camera.capture()
    }

    // Лише перенаправлення системних відповідей
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return

        when (requestCode) {
            camera.REQUEST_CAPTURE -> workflow.execute(camera.currentPhotoPath, resultImage, btnPrint)
            REQUEST_PICK_TEMPLATE -> data?.data?.let { uri ->
                val path = FileUtils.saveCustomTemplate(this, uri)
                if (path != null) settings.customTemplatePath = path
            }
        }
    }

    // Тепер це просто виклик хелпера
    fun display(file: File?) {
        ImageDisplayHelper.show(file, resultImage, btnPrint)
    }

    fun pickTemplateIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/png"
        }
        startActivityForResult(intent, REQUEST_PICK_TEMPLATE)
    }

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        if (rc == camera.PERMISSION_CAMERA && g.isNotEmpty() && g[0] == 0) camera.capture()
    }
}
