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
    lateinit var settings: SettingsManager
    lateinit var hEdited: HistoryManager
    lateinit var hRaw: HistoryManager
    lateinit var hTpl: HistoryManager
    
    lateinit var camera: CameraHandler
    lateinit var workflow: WorkflowManager
    lateinit var interaction: InteractionManager
    
    private lateinit var wifiLifecycleHelper: WifiDirectLifecycleHelper
    private lateinit var discoveryHandler: WifiDiscoveryHandler
    private lateinit var printerManager: PrinterManager
    
    lateinit var resultImage: ImageView
    lateinit var btnPrint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Динамічні кольори
        com.google.android.material.color.DynamicColors.applyToActivitiesIfAvailable(application)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2. Ініціалізація менеджерів
        settings = SettingsManager(this)
        camera = CameraHandler(this)
        printerManager = PrinterManager(this)
        Bootstrapper.run(this, settings)
        
        wifiLifecycleHelper = WifiDirectLifecycleHelper(this)
        discoveryHandler = WifiDiscoveryHandler(this, printerManager)

        hEdited = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Edited"))
        hRaw = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Raw"))
        hTpl = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Templates"))

        workflow = WorkflowManager(this, settings)
        
        // 3. UI Елементи
        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        interaction = InteractionManager(this, workflow, camera)
        interaction.setup()

        // 4. Логіка кнопки друку (Пошук принтера)
        btnPrint.setOnClickListener {
            Toast.makeText(this, "Шукаю пристрої...", Toast.LENGTH_SHORT).show()
            discoveryHandler.start()
        }
    }

    override fun onResume() {
        super.onResume()
        // Реєструємо слухача подій Wi-Fi Direct
        wifiLifecycleHelper.register(
            onPeersAvailable = { devices ->
                // Коли система знайшла нові пристрої — показуємо діалог вибору
                if (devices.isNotEmpty()) {
                    discoveryHandler.showPeerDialog(devices) {
                        Toast.makeText(this, "Принтер готовий до роботи", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onConnectionChanged = {
                // Перевіряємо статус підключення при зміні стану мережі
                val wdManager = WifiDirectManager(this)
                wdManager.requestInfo { info ->
                    if (info.groupFormed) {
                        val role = if (info.isGroupOwner) "Сервер (Принтер)" else "Клієнт (Камера)"
                        Logger.log(this, "P2P Connected: $role, IP: ${info.groupOwnerAddress?.hostAddress}")
                    }
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()
        // Обов'язково вимикаємо ресивер, щоб не садити батарею і не плодити помилки
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
            2 -> { // Вибір власного шаблону
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

    fun display(file: File?) {
        ImageDisplayHelper.show(file, resultImage, btnPrint)
    }

    fun pickTemplateIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/png"
        }
        startActivityForResult(intent, 2)
    }

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        super.onRequestPermissionsResult(rc, p, g)
        // Перевірка дозволів для камери
        if (rc == camera.PERMISSION_CAMERA && g.isNotEmpty() && g[0] == 0) {
            camera.capture()
        }
        // Дозволи для Wi-Fi Direct (Android 13+) обробляються всередині WifiDiscoveryHandler
    }
}
