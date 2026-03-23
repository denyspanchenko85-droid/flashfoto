package com.shadow.flashfoto

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import java.io.File

class MainActivity : Activity() {
    lateinit var settings: SettingsManager
    lateinit var camera: CameraHandler
    lateinit var workflow: WorkflowManager
    lateinit var interaction: InteractionManager
    
    // Три окремі потоки історії
    lateinit var hEdited: HistoryManager
    lateinit var hRaw: HistoryManager
    lateinit var hTpl: HistoryManager
    
    lateinit var resultImage: ImageView
    lateinit var btnPrint: Button
    
    private val REQUEST_PICK_TEMPLATE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Ініціалізація бази та папок
        settings = SettingsManager(this)
        camera = CameraHandler(this)
        Bootstrapper.run(this, settings)
        
        // 2. Ініціалізація трьох менеджерів історії
        hEdited = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Edited"))
        hRaw = HistoryManager(this, File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Raw"))
        hTpl = HistoryManager(this, File(getExternalFilesDir(null), "Templates"))

        workflow = WorkflowManager(this, settings, hEdited)

        // 3. UI
        resultImage = findViewById(R.id.resultImage)
        btnPrint = findViewById(R.id.btnPrint)

        // 4. Підключення взаємодії (Конструктор або Історія)
        interaction = InteractionManager(this, camera, hEdited, hRaw, hTpl, settings)
        interaction.setup()

        // Старт камери
        camera.capture()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return

        when (requestCode) {
            camera.REQUEST_CAPTURE -> {
                workflow.execute(camera.currentPhotoPath, resultImage, btnPrint)
                // Оновлюємо історію Raw, щоб нове фото з'явилося в конструкторі
                hRaw.updateHistory()
            }
            REQUEST_PICK_TEMPLATE -> {
                data?.data?.let { uri ->
                    val path = FileUtils.saveCustomTemplate(this, uri)
                    if (path != null) {
                        settings.customTemplatePath = path
                        hTpl.updateHistory()
                        Toast.makeText(this, "Шаблон додано", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Міст для InteractionManager
    fun display(file: File?) {
        ImageDisplayHelper.show(file, resultImage, btnPrint)
    }

    fun pickTemplateIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/png"
        }
        startActivityForResult(intent, REQUEST_PICK_TEMPLATE)
    }

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        if (rc == camera.PERMISSION_CAMERA && g.isNotEmpty() && g[0] == 0) camera.capture()
    }
}
