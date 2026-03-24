// Responsibility: Main UI Controller and lifecycle orchestration
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
    private lateinit var printerManager: PrinterManager
    
    lateinit var resultImage: ImageView
    lateinit var btnPrint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        com.google.android.material.color.DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settings = SettingsManager(this)
        camera = CameraHandler(this)
        printerManager = PrinterManager(this)
        Bootstrapper.run(this, settings)
        
        wifiLifecycleHelper = WifiDirectLifecycleHelper(this)

        hEdited = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Edited"))
        hRaw = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Raw"))
        hTpl = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Templates"))

        // FIX: WorkflowManager потребує 3 параметри (додано hEdited)
        workflow = WorkflowManager(this, settings, hEdited)
        
        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        // FIX: InteractionManager строго 7 параметрів згідно з твоїм файлом
        interaction = InteractionManager(
            this,      // activity
            camera,    // camera
            hEdited,   // hEdited
            hRaw,      // hRaw
            hTpl,      // hTpl
            settings,  // settings
            workflow   // workflow
        )
        interaction.setup()

        btnPrint.setOnClickListener {
            PrinterDialogHandler(this).show()
        }
    }

    override fun onResume() {
        super.onResume()
        wifiLifecycleHelper.register(
            onPeersAvailable = { _ -> },
            onConnectionChanged = {
                val wdManager = WifiDirectManager(this)
                wdManager.requestInfo { info ->
                    if (info.groupFormed) Logger.log(this, "P2P Connection Established")
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()
        wifiLifecycleHelper.unregister()
    }

    // Потрібно для SettingsDialogHandler
    fun pickTemplateIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/png"
        }
        startActivityForResult(intent, 2)
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

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        super.onRequestPermissionsResult(rc, p, g)
        if (rc == camera.PERMISSION_CAMERA && g.isNotEmpty() && g[0] == 0) {
            camera.capture()
        }
    }
}
