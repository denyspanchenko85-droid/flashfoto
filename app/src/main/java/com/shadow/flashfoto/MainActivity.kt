// Responsibility: Pure UI coordination and event routing
package com.shadow.flashfoto

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import java.io.File

class MainActivity : Activity() {
    private lateinit var settings: SettingsManager
    private lateinit var history: HistoryManager
    private lateinit var camera: CameraHandler
    private lateinit var workflow: WorkflowManager
    
    private lateinit var resultImage: ImageView
    private lateinit var btnPrint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ініціалізація
        settings = SettingsManager(this)
        camera = CameraHandler(this)
        
        val galleryDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), 
            "FlashFoto"
        )
        history = HistoryManager(galleryDir)
        workflow = WorkflowManager(this, settings, history)

        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        // Прив'язка подій
        findViewById<Button>(R.id.btnCapture).setOnClickListener { camera.capture() }
        findViewById<Button>(R.id.btnPrev).setOnClickListener { display(history.getPrev()) }
        findViewById<Button>(R.id.btnNext).setOnClickListener { display(history.getNext()) }
        
        findViewById<ImageView>(R.id.btnSettings).setOnClickListener { 
            SettingsDialogHandler(this, settings).show() 
        }
        
        btnPrint.setOnClickListener { 
            history.getCurrent()?.let { PrintManager.printFromFile(this, it, settings) }
        }

        // Автозапуск
        camera.capture()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == camera.REQUEST_CAPTURE && resultCode == RESULT_OK) {
            workflow.execute(camera.currentPhotoPath, resultImage, btnPrint)
        }
    }

    private fun display(file: File?) {
        file?.let { 
            val bitmap = BitmapFactory.decodeFile(it.absolutePath)
            resultImage.setImageBitmap(bitmap)
            btnPrint.visibility = android.view.View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        if (rc == camera.PERMISSION_CAMERA && g.isNotEmpty() && g[0] == 0) {
            camera.capture()
        }
    }
}
