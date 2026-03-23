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
    private lateinit var settings: SettingsManager
    private lateinit var history: HistoryManager
    private lateinit var camera: CameraHandler
    private lateinit var workflow: WorkflowManager
    private lateinit var interaction: InteractionManager
    
    lateinit var resultImage: ImageView
    lateinit var btnPrint: Button
    
    private val REQUEST_PICK_TEMPLATE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Базова ініціалізація
        settings = SettingsManager(this)
        camera = CameraHandler(this)
        
        // 2. "ІНШИЙ ШЛЯХ" для історії: використовуємо внутрішню папку додатка.
        // Це гарантує, що ми завжди бачимо свої файли без READ_EXTERNAL_STORAGE.
        val historyDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FlashFoto")
        if (!historyDir.exists()) historyDir.mkdirs()
        
        history = HistoryManager(this, historyDir)
        workflow = WorkflowManager(this, settings, history)

        // 3. UI компоненти
        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        // 4. Логіка взаємодії (кнопки)
        interaction = InteractionManager(this, camera, history, settings)
        interaction.setup()

        // Автозапуск камери при старті
        camera.capture()
    }

    // Метод для вибору власного PNG шаблону (викликається з InteractionManager)
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
            // Результат з камери
            camera.REQUEST_CAPTURE -> {
                workflow.execute(camera.currentPhotoPath, resultImage, btnPrint)
            }
            
            // Результат вибору шаблону
            REQUEST_PICK_TEMPLATE -> {
                data?.data?.let { uri ->
                    // Android 11+ вимагає Persistent URI або копіювання файлу.
                    // Поки що просто зберігаємо URI як шлях (для тестів).
                    settings.customTemplatePath = uri.toString()
                    Toast.makeText(this, "Шаблон вибрано", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Метод відображення фото з історії
    fun display(file: File?) {
        file?.let { 
            if (it.exists()) {
                val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                resultImage.setImageBitmap(bitmap)
                btnPrint.visibility = View.VISIBLE
            }
        }
    }

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        if (rc == camera.PERMISSION_CAMERA && g.isNotEmpty() && g[0] == 0) {
            camera.capture()
        } else {
            Toast.makeText(this, "Дозвіл відхилено", Toast.LENGTH_SHORT).show()
        }
    }
}
