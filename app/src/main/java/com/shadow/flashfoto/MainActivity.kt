package com.shadow.flashfoto

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.print.PrintHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private var currentPhotoPath: String? = null
    private var finalBitmap: Bitmap? = null
    
    private lateinit var resultImage: ImageView
    private lateinit var btnPrint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)
        val btnCapture = findViewById<Button>(R.id.btnCapture)

        btnCapture.setOnClickListener { dispatchTakePictureIntent() }
        btnPrint.setOnClickListener { doPrint() }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()
        
        photoFile?.let {
            val photoURI: Uri = FileProvider.getUriForFile(this,
                "com.shadow.flashfoto.provider", it)
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val photo = BitmapFactory.decodeFile(currentPhotoPath)
            finalBitmap = ImageOverlayProcessor.applyFrame(this, photo)
            resultImage.setImageBitmap(finalBitmap)
            btnPrint.visibility = View.VISIBLE
        }
    }

    private fun doPrint() {
        finalBitmap?.let {
            PrintHelper(this).apply {
                scaleMode = PrintHelper.SCALE_MODE_FIT
                printBitmap("FlashFoto-Print", it)
            }
        }
    }
}
