package com.shadow.flashfoto

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.view.View
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

        // 1. Ініціалізація
        settings = SettingsManager(this)
        camera = CameraHandler(this)
        
        // 2. РОЗГОРТАННЯ СТРУКТУРИ ТА ШАБЛОНІВ
        initAppStorage()
        
        // Шлях для історії: Тільки оброблені фото (Edited)
        val editedDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Edited")
        history = HistoryManager(this, editedDir)
        workflow = WorkflowManager(this, settings, history)

        // 3. UI Binding
        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        // 4. Setup Interaction
        interaction = InteractionManager(this, camera, history, settings)
        interaction.setup()

        // Старт камери
        camera.capture()
    }

    private fun initAppStorage() {
        // Створюємо папку Templates
        val templateDir = File(getExternalFilesDir(null), "Templates")
        if (!templateDir.exists()) templateDir.mkdirs()

        // Копіюємо дефолтні шаблони з ресурсів у внутрішню папку
        FileUtils.copyRawToTemplates(this, R.drawable.easter_vert_1, "default_vertical.png")
        FileUtils.copyRawToTemplates(this, R.drawable.easter_horiz_1, "default_horizontal.png")

        // Якщо шаблон ще не вибрано — ставимо вертикальний за замовчуванням
        if (settings.customTemplatePath == null) {
            val defaultPath = File(templateDir, "default_vertical.png").absolutePath
            settings.customTemplatePath = defaultPath
        }
        
        // Також перевіримо папку Edited
        val editedDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Edited")
        if (!editedDir.exists()) editedDir.mkdirs()
    }

    fun pickTemplateIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/png"
        }
        startActivityForResult(intent, REQUEST_PICK_TEMPLATE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return

        when (requestCode) {
            camera.REQUEST_CAPTURE -> {
                workflow.execute(camera.currentPhotoPath, resultImage, btnPrint)
            }
            REQUEST_PICK_TEMPLATE -> {
                data?.data?.let { uri ->
                    // Копіюємо зовнішній файл у нашу папку Templates
                    val savedPath = FileUtils.saveCustomTemplate(this, uri)
                    if (savedPath != null) {
                        settings.customTemplatePath = savedPath
                        Toast.makeText(this, "Шаблон додано та активовано", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun display(file: File?) {
        if (file != null && file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            resultImage.setImageBitmap(bitmap)
            btnPrint.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        if (rc == camera.PERMISSION_CAMERA && g.isNotEmpty() && g[0] == 0) {
            camera.capture()
        }
    }
}
