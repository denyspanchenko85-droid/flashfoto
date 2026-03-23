package com.shadow.flashfoto

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.print.PrintHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private val PERMISSION_REQUEST_CAMERA = 100
    
    private lateinit var settings: SettingsManager
    private lateinit var history: HistoryManager
    private var currentPhotoPath: String? = null
    
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

        findViewById<Button>(R.id.btnCapture).setOnClickListener { checkPermissionAndCapture() }
        findViewById<Button>(R.id.btnPrev).setOnClickListener { showImage(history.getPrev()) }
        findViewById<Button>(R.id.btnNext).setOnClickListener { showImage(history.getNext()) }
        btnPrint.setOnClickListener { 
            val file = history.getCurrent()
            if (file != null) doPrint(BitmapFactory.decodeFile(file.absolutePath))
        }

        checkPermissionAndCapture()
    }

    private fun checkPermissionAndCapture() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = try {
            createImageFile()
        } catch (ex: Exception) {
            Logger.log(this, "File creation failed", ex)
            null
        }
        
        photoFile?.let {
            val photoURI: Uri = FileProvider.getUriForFile(this, "com.shadow.flashfoto.provider", it)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                val photo = BitmapFactory.decodeFile(currentPhotoPath)
                val finalBitmap = ImageOverlayProcessor.applyFrame(this, photo)
                
                resultImage.setImageBitmap(finalBitmap)
                btnPrint.visibility = View.VISIBLE
                
                history.updateHistory()
                
                if (settings.isAutoPrintEnabled) {
                    doPrint(finalBitmap)
                }
            } catch (e: Exception) {
                Logger.log(this, "Overlay or print error", e)
            }
        }
    }

    private fun showImage(file: File?) {
        file?.let {
            val bitmap = BitmapFactory.decodeFile(it.absolutePath)
            resultImage.setImageBitmap(bitmap)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        }
    }
}
