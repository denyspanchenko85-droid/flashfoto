// Responsibility: Main UI Controller and Lifecycle Orchestrator
package com.shadow.flashfoto

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    // Менеджери даних та налаштувань
    lateinit var settings: SettingsManager
    lateinit var hEdited: HistoryManager
    lateinit var hRaw: HistoryManager
    lateinit var hTpl: HistoryManager
    
    // Обробники логіки
    lateinit var camera: CameraHandler
    lateinit var workflow: WorkflowManager
    lateinit var interaction: InteractionManager
    
    // Wi-Fi Direct компоненти
    private lateinit var wifiLifecycleHelper: WifiDirectLifecycleHelper
    private lateinit var discoveryHandler: WifiDiscoveryHandler
    private lateinit var printerManager: PrinterManager
    
    // UI елементи
    lateinit var resultImage: ImageView
    lateinit var btnPrint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        // Застосування динамічних кольорів Material 3
        com.google.android.material.color.DynamicColors.applyToActivitiesIfAvailable(application)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Ініціалізація базових сервісів
        settings = SettingsManager(this)
        camera = CameraHandler(this)
        printerManager = PrinterManager(this)
        Bootstrapper.run(this, settings)
        
        // 2. Ініціалізація Wi-Fi Direct
        wifiLifecycleHelper = WifiDirectLifecycleHelper(this)
        discoveryHandler = WifiDiscoveryHandler(this, printerManager)

        // 3. Ініціалізація менеджерів історії
        hEdited = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Edited"))
        hRaw = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Raw"))
        hTpl = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Templates"))

        workflow = WorkflowManager(this, settings)
        
        // 4. Прив'язка UI
        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        // 5. Налаштування взаємодії (InteractionManager)
        // Порядок аргументів виправлено згідно з вимогами конструктора
        interaction = InteractionManager(
            this, 
            camera, 
            hEdited, 
            hRaw, 
            hTpl, 
            settings, 
            workflow
        )
        interaction.setup()

        // 6. Кнопка пошуку принтера
        btnPrint.setOnClickListener {
            Toast.makeText(this, "Пошук принтерів...", Toast.LENGTH_SHORT).show()
            discoveryHandler.start()
        }
    }

    override fun onResume() {
        super.onResume()
        // Реєстрація слухача подій Wi-Fi Direct
        wifiLifecycleHelper.register(
            onPeersAvailable = { devices ->
                if (devices.isNotEmpty()) {
                    discoveryHandler.showPeerDialog(devices) {
                        Toast.makeText(this, "Принтер підключено успішно", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onConnectionChanged = {
                // Логування стану з'єднання при змінах у мережі
                val wdManager = WifiDirectManager(this)
                wdManager.requestInfo { info ->
                    if (info.groupFormed) {
                        val status = if (info.isGroupOwner) "Host (Printer)" else "Client (Camera)"
                        Logger.log(this, "P2P Connection Active: $status")
                    }
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()
        // Обов'язкове зняття реєстрації для запобігання витоку пам'яті
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
                    camera.cleanup()
                    interaction.refreshPreview()
                }
            }
            2 -> { // Вибір користувацького шаблону
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

    /**
     * Відображення файлу в головному ImageView
     */
    fun display(file: File?) {
        ImageDisplayHelper.show(file, resultImage, btnPrint)
    }

    /**
     * Запуск інтенту для вибору файлу зображення (шаблону)
     */
    fun pickTemplateIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/png"
        }
        startActivityForResult(intent, 2)
    }

    /**
     * Обробка результатів запиту дозволів
     */
    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        super.onRequestPermissionsResult(rc, p, g)
        if (rc == camera.PERMISSION_CAMERA && g.isNotEmpty() && g[0] == 0) {
            camera.capture()
        }
        // Дозволи для Wi-Fi (Android 13+) обробляються всередині discoveryHandler.start()
    }
}
