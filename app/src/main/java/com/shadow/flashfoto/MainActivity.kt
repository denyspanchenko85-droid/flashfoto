package com.shadow.flashfoto

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.print.PrintHelper

class MainActivity : Activity() {
    private lateinit var settings: SettingsManager
    private lateinit var history: HistoryManager
    private lateinit var camera: CameraHandler
    
    private lateinit var resultImage: ImageView
    private lateinit var btnPrint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ініціалізація
        settings = SettingsManager(this)
        camera = CameraHandler(this)
        history = HistoryManager(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!)

        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        // Кнопки - тепер кожна в один рядок
        findViewById<Button>(R.id.btnCapture).setOnClickListener { camera.capture() }
        findViewById<Button>(R.id.btnPrev).setOnClickListener { display(history.getPrev()) }
        findViewById<Button>(R.id.btnNext).setOnClickListener { display(history.getNext()) }
        
        // Виклик винесеного діалогу
        findViewById<ImageView>(R.id.btnSettings).setOnClickListener { 
            SettingsDialogHandler(this, settings).show() 
        }
        
        btnPrint.setOnClickListener { history.getCurrent()?.let { printFile(it) } }

        camera.capture()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == camera.REQUEST_CAPTURE && resultCode == RESULT_OK) {
            processResult()
        }
    }
    private fun processResult() {
        try {
            val rawPath = camera.currentPhotoPath ?: return
            val photo = BitmapFactory.decodeFile(rawPath)
            val finalBitmap = ImageOverlayProcessor.applyFrame(this, photo) ?: return
            
            // 1. Показ та збереження результату
            resultImage.setImageBitmap(finalBitmap)
            btnPrint.visibility = View.VISIBLE
            GalleryManager.saveToGallery(this, finalBitmap)
            history.updateHistory()

            // 2. ДРУК
            if (settings.isAutoPrintEnabled) printBitmap(finalBitmap)

            // 3. ПЕРЕВІРКА: Видаляти оригінал чи ні?
            if (!settings.isKeepOriginalEnabled) {
                val rawFile = java.io.File(rawPath)
                if (rawFile.exists()) {
                    rawFile.delete()
                    // Очищаємо шлях, щоб не було помилок при повторному доступі
                    camera.currentPhotoPath = null 
                }
            }
            
        } catch (e: Exception) {
            Logger.log(this, "Process result error", e)
        }
    }

    private fun display(file: java.io.File?) {
        file?.let { resultImage.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath)) }
    }

    private fun printFile(file: java.io.File) = printBitmap(BitmapFactory.decodeFile(file.absolutePath))

    private fun printBitmap(bitmap: android.graphics.Bitmap?) {
        bitmap?.let {
            PrintHelper(this).apply {
                scaleMode = PrintHelper.SCALE_MODE_FIT
                printBitmap("FlashFoto-Print", it)
            }
        }
    }

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        if (rc == camera.PERMISSION_CAMERA && g.isNotEmpty() && g[0] == 0) camera.capture()
    }
}
