// Responsibility: Main UI Controller and Orchestrator
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

        // Responsibility: Initialize InteractionManager with named parameters to satisfy 'history'
        interaction = InteractionManager(
            context = this,
            camera = camera,
            hEdited = hEdited,
            hRaw = hRaw,
            hTpl = hTpl,
            settings = settings,
            workflow = workflow,
            history = hRaw // Додано для виправлення помилки "No value passed for parameter 'history'"
        )
        interaction.setup()

        btnPrint.setOnClickListener {
            discoveryHandler.start()
        }
    }

    override fun onResume() {
        super.onResume()
        wifiLifecycleHelper.register(
            onPeersAvailable = { devices ->
                if (devices.isNotEmpty()) {
                    discoveryHandler.showPeerDialog(devices) {
                        Toast.makeText(this, "Принтер підключено", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onConnectionChanged = {
                val wdManager = WifiDirectManager(this)
                wdManager.requestInfo { info ->
                    if (info.groupFormed) {
                        Logger.log(this, "Connected. Owner: ${info.isGroupOwner}")
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

    fun pickTemplateIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/png"
        }
        startActivityForResult(intent, 2)
    }

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        super.onRequestPermissionsResult(rc, p, g)
        if (rc == camera.PERMISSION_CAMERA && g.isNotEmpty() && g[0] == 0) {
            camera.capture()
        }
    }
}
