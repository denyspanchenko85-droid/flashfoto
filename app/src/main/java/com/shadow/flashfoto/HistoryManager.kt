package com.shadow.flashfoto

import android.util.Log
import java.io.File

class HistoryManager(private val directory: File) {
    private var files: List<File> = emptyList()
    var currentIndex: Int = -1

    init { updateHistory() }

    fun updateHistory() {
        if (!directory.exists()) {
            directory.mkdirs() // Створюємо папку, якщо її немає
        }

        files = directory.listFiles { f -> 
            val ext = f.extension.lowercase()
            ext == "jpg" || ext == "jpeg" || ext == "png" 
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

        // Після оновлення завжди стаємо на найсвіжіше фото
        if (files.isNotEmpty()) {
            currentIndex = 0
        }
        
        Log.d("FlashFoto", "History updated. Found ${files.size} photos in ${directory.absolutePath}")
    }

    fun getNext(): File? { // Новіші (вгору до 0)
        if (currentIndex > 0) {
            currentIndex--
            return files[currentIndex]
        }
        return null
    }

    fun getPrev(): File? { // Старіші (вниз до кінця списку)
        if (currentIndex < files.size - 1) {
            currentIndex++
            return files[currentIndex]
        }
        return null
    }
    
    fun getCurrent(): File? = files.getOrNull(currentIndex)
    
    fun getCount(): Int = files.size
}
