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
    
    // Утиліта для Wi-Fi Direct
    private lateinit var wifiLifecycleHelper: WifiDirectLifecycleHelper
    
    lateinit var resultImage: ImageView
    lateinit var btnPrint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settings = SettingsManager(this)
        camera = CameraHandler(this)
        Bootstrapper.run(this, settings)
        
        wifiLifecycleHelper = WifiDirectLifecycleHelper(this)

        hEdited = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Edited"))
        hRaw = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Raw"))
        hTpl = HistoryManager(this, File(getExternalFilesDir(null), "Templates"))

        workflow = WorkflowManager(this, settings, hEdited)

        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        interaction = InteractionManager(this, camera, hEdited, hRaw, hTpl, settings, workflow)
        interaction.setup()
    }

    override fun onResume() {
        super.onResume()
        // Реєструємо слухача Wi-Fi Direct
        wifiLifecycleHelper.register(
            onPeersChanged = { /* Можна додати сповіщення в UI */ },
            onConnectionChanged = { /* Логіка при зміні з'єднання */ }
        )
    }

    override fun onPause() {
        super.onPause()
        // Обов'язково відключаємо, щоб не висаджувати батарею
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
            2 -> {
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
        if (rc == camera.PERMISSION_CAMERA && g.isNotEmpty() && g[0] == 0) {
            camera.capture()
        }
    }
}
