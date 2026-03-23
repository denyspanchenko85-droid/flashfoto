package com.shadow.flashfoto

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.print.PrintHelper
import java.io.File

class MainActivity : Activity() {
    private lateinit var settings: SettingsManager
    private lateinit var history: HistoryManager
    private var currentPhotoPath: String? = null
    
    // UI elements
    private lateinit var resultImage: ImageView
    private lateinit var btnPrint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        settings = SettingsManager(this)
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        history = HistoryManager(storageDir)

        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        findViewById<Button>(R.id.btnCapture).setOnClickListener {
            checkPermissionAndCapture() // Метод з твого попереднього робочого коду
        }

        // Кнопки навігації (якщо ти їх додав у layout)
        findViewById<Button>(R.id.btnPrev)?.setOnClickListener { showImage(history.getPrev()) }
        findViewById<Button>(R.id.btnNext)?.setOnClickListener { showImage(history.getNext()) }

        try {
            checkPermissionAndCapture()
        } catch (e: Exception) {
            Logger.log(this, "Crash in onCreate", e)
        }
    }

    private fun showImage(file: File?) {
        file?.let {
            val bitmap = BitmapFactory.decodeFile(it.absolutePath)
            resultImage.setImageBitmap(bitmap)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == RESULT_OK) {
                val photo = BitmapFactory.decodeFile(currentPhotoPath)
                val finalBitmap = ImageOverlayProcessor.applyFrame(this, photo)
                
                resultImage.setImageBitmap(finalBitmap)
                history.updateHistory()
                
                if (settings.isAutoPrintEnabled) {
                    doPrint(finalBitmap)
                }
            }
        } catch (e: Exception) {
            Logger.log(this, "Error in onActivityResult", e)
        }
    }

    private fun doPrint(bitmap: android.graphics.Bitmap?) {
        bitmap?.let {
            PrintHelper(this).apply {
                scaleMode = PrintHelper.SCALE_MODE_FIT
                printBitmap("FlashFoto-Print", it)
            }
        }
    }

    // ТУТ МАЄ БУТИ ТВОЯ ЛОГІКА checkPermissionAndCapture ТА dispatchTakePictureIntent
    private fun checkPermissionAndCapture() {
        // ... (код, який ми писали раніше)
    }
}
