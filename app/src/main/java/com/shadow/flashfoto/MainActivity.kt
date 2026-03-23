package com.shadow.flashfoto

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import java.io.File

class MainActivity : Activity() {
    lateinit var settings: SettingsManager
    lateinit var hEdited: HistoryManager
    lateinit var hRaw: HistoryManager
    lateinit var hTpl: HistoryManager
    
    lateinit var camera: CameraHandler
    lateinit var workflow: WorkflowManager
    lateinit var interaction: InteractionManager
    
    lateinit var resultImage: ImageView
    lateinit var btnPrint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Базова ініціалізація
        settings = SettingsManager(this)
        camera = CameraHandler(this)
        Bootstrapper.run(this, settings)
        
        // 2. Ініціалізація трьох менеджерів історії для різних папок
        hEdited = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Edited"))
        hRaw = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Raw"))
        hTpl = HistoryManager(this, File(getExternalFilesDir(null), "Templates"))

        // 3. Ініціалізація логіки обробки (Workflow)
        workflow = WorkflowManager(this, settings, hEdited)

        // 4. UI компоненти
        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        // 5. Налаштування взаємодії (передаємо всі історії та workflow)
        interaction = InteractionManager(this, camera, hEdited, hRaw, hTpl, settings, workflow)
        interaction.setup()
        
        // Камера більше не запускається автоматично при старті додатка
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            camera.REQUEST_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    // Успішна зйомка: обробляємо та оновлюємо список Raw
                    workflow.execute(camera.currentPhotoPath, resultImage, btnPrint)
                    hRaw.updateHistory()
                } else {
                    // Користувач скасував: видаляємо пустий файл і оновлюємо прев'ю
                    camera.cleanup()
                    interaction.refreshPreview()
                }
            }
            2 -> { // REQUEST_PICK_TEMPLATE
                if (resultCode == RESULT_OK) {
                    data?.data?.let { uri ->
                        val path = FileUtils.saveCustomTemplate(this, uri)
                        if (path != null) {
                            settings.customTemplatePath = path
                            hTpl.updateHistory()
                            interaction.refreshPreview()
                        }
                    }
                }
            }
        }
    }

    // Метод відображення для HistoryManager та InteractionManager
    fun display(file: File?) {
        ImageDisplayHelper.show(file, resultImage, btnPrint)
    }

    // Виклик системного вікна вибору PNG шаблону
    fun pickTemplateIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/png"
        }
        startActivityForResult(intent, 2)
    }

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        if (rc == camera.PERMISSION_CAMERA && g.isNotEmpty() && g[0] == 0) {
            camera.capture()
        }
    }
}
