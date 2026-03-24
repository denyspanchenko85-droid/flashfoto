package com.shadow.flashfoto

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
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
        com.google.android.material.color.DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        
        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        // Responsibility: Initialize InteractionManager strictly matching your provided code (7 params)
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

        // В MainActivity кнопка може просто відкривати діалог управління принтерами
        btnPrint.setOnClickListener {
            PrinterDialogHandler(this).show()
        }
    }

    override fun onResume() {
        super.onResume()
        wifiLifecycleHelper.register(
            onPeersAvailable = { devices ->
                // Якщо діалог пошуку активний, він отримає список через свій механізм
            },
            onConnectionChanged = {
                val wdManager = WifiDirectManager(this)
                wdManager.requestInfo { info ->
                    if (info.groupFormed) {
                        Logger.log(this, "P2P Connection Established")
                    }
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()
        wifiLifecycleHelper.unregister()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == camera.REQUEST_CAPTURE && resultCode == RESULT_OK) {
            workflow.execute(camera.currentPhotoPath, resultImage, btnPrint)
            hRaw.updateHistory()
        }
    }

    fun display(file: File?) {
        ImageDisplayHelper.show(file, resultImage, btnPrint)
    }
}
