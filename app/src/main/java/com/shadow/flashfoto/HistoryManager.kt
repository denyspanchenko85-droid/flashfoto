package com.shadow.flashfoto

import android.content.Context
import java.io.File

class HistoryManager(private val context: Context, private val directory: File) {
    private var files: List<File> = emptyList()
    var currentIndex: Int = -1

    init { updateHistory() }

    fun updateHistory() {
        if (!directory.exists()) directory.mkdirs()

        files = directory.listFiles { f -> 
            val ext = f.extension.lowercase()
            ext == "jpg" || ext == "jpeg" || ext == "png" 
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

        if (files.isNotEmpty()) {
            currentIndex = 0
        }
        
        // ТЕПЕР ПИШЕМО В ТВОЙ ФАЙЛ
        Logger.log(context, "Історія оновлена. Знайдено фото: ${files.size} у папці ${directory.name}")
    }

    fun getNext(): File? {
        if (currentIndex > 0) {
            currentIndex--
            return files[currentIndex]
        }
        return null
    }

    fun getPrev(): File? {
        if (currentIndex < files.size - 1) {
            currentIndex++
            return files[currentIndex]
        }
        return null
    }
    
    fun getCurrent(): File? = files.getOrNull(currentIndex)
}
