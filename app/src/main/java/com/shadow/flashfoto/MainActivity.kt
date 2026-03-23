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
    
    private lateinit var resultImage: ImageView
    private lateinit var btnPrint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Ініціалізація менеджерів
        settings = SettingsManager(this)
        camera = CameraHandler(this)
        
        // Шлях до публічної папки Pictures/FlashFoto
        val galleryDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), 
            "FlashFoto"
        )
        
        // Передаємо 'this' (context), щоб History міг писати в лог-файл
        history = HistoryManager(this, galleryDir)
        workflow = WorkflowManager(this, settings, history)

        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        // 2. Налаштування кнопок
        findViewById<Button>(R.id.btnCapture).setOnClickListener { 
            camera.capture() 
        }

        findViewById<Button>(R.id.btnPrev).setOnClickListener { 
            val file = history.getPrev()
            if (file != null) display(file) 
            else Toast.makeText(this, "Це початок історії", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnNext).setOnClickListener { 
            val file = history.getNext()
            if (file != null) display(file) 
            else Toast.makeText(this, "Це останнє фото", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<ImageView>(R.id.btnSettings).setOnClickListener { 
            SettingsDialogHandler(this, settings).show() 
        }
        
        btnPrint.setOnClickListener { 
            history.getCurrent()?.let { 
                PrintManager.printFromFile(this, it, settings) 
            }
        }

        // Автозапуск камери
        camera.capture()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == camera.REQUEST_CAPTURE && resultCode == RESULT_OK) {
            // Workflow обробить фото, збереже в галерею і дасть команду історії оновитися
            workflow.execute(camera.currentPhotoPath, resultImage, btnPrint)
        }
    }

    private fun display(file: File?) {
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
        }
    }
}
