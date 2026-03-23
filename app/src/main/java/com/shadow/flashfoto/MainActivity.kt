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
    
    // Утиліта для Wi-Fi Direct (керує ресівером)
    private lateinit var wifiLifecycleHelper: WifiDirectLifecycleHelper
    
    lateinit var resultImage: ImageView
    lateinit var btnPrint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Ініціалізація бази та папок
        settings = SettingsManager(this)
        camera = CameraHandler(this)
        Bootstrapper.run(this, settings)
        
        // 2. Ініціалізація допоміжних утиліт
        wifiLifecycleHelper = WifiDirectLifecycleHelper(this)

        // 3. Ініціалізація трьох потоків історії
        hEdited = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Edited"))
        hRaw = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Raw"))
        hTpl = HistoryManager(this, File(getExternalFilesDir(null), "Templates"))

        // 4. Ініціалізація логіки обробки (Workflow)
        workflow = WorkflowManager(this, settings, hEdited)

        // 5. UI компоненти
        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        // 6. Setup Interaction (Передаємо всі 7 аргументів)
        // ShareManager не передається в конструктор, бо він викликається як статичний об'єкт
        interaction = InteractionManager(this, camera, hEdited, hRaw, hTpl, settings, workflow)
        interaction.setup()
        
        // Камера чекає на клік по кнопці, не стартує сама
    }

    override fun onResume() {
        super.onResume()
        // Активуємо слухача Wi-Fi Direct при поверненні в додаток
        wifiLifecycleHelper.register(
            onPeersChanged = { /* Події пошуку обробляються в WifiDiscoveryHandler */ },
            onConnectionChanged = { /* Логіка зміни статусу мережі */ }
        )
    }

    override fun onPause() {
        super.onPause()
        // Вимикаємо слухача для економії енергії та безпеки
        wifiLifecycleHelper.unregister()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            camera.REQUEST_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    workflow.execute(camera.currentPhotoPath, resultImage, btnPrint)
                    hRaw.updateHistory()
                } else {
                    camera.cleanup() // Видаляємо пустий файл, якщо скасували зйомку
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

    // Хелпер для відображення фото (викликається менеджерами)
    fun display(file: File?) {
        ImageDisplayHelper.show(file, resultImage, btnPrint)
    }

    // Виклик системного вікна для вибору PNG
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
