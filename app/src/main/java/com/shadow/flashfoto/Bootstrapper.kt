package com.shadow.flashfoto

import android.app.Activity
import android.os.Environment
import java.io.File

object Bootstrapper {

    fun run(activity: Activity, settings: SettingsManager) {
        // 1. Створюємо структуру папок (Raw, Edited, Templates)
        val baseDir = activity.getExternalFilesDir(null)
        val folders = listOf("Templates", "Pictures/Raw", "Pictures/Edited")
        
        folders.forEach { path ->
            val dir = File(baseDir, path)
            if (!dir.exists()) dir.mkdirs()
        }

        // 2. Розгортаємо дефолтні шаблони з ресурсів у нашу папку Templates
        FileUtils.copyRawToTemplates(activity, R.drawable.easter_vert_1, "default_vertical.png")
        FileUtils.copyRawToTemplates(activity, R.drawable.easter_horiz_1, "default_horizontal.png")

        // 3. Якщо в налаштуваннях ще немає шляху — ставимо наш дефолт
        if (settings.customTemplatePath == null) {
            val defaultPath = File(baseDir, "Templates/default_vertical.png").absolutePath
            settings.customTemplatePath = defaultPath
        }
    }
}
