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

        if (files.isNotEmpty() && currentIndex == -1) currentIndex = 0
        Logger.log(context, "Папка ${directory.name}: знайдено ${files.size}")
    }

    fun getNext(): File? {
        if (currentIndex > 0) { currentIndex--; return files[currentIndex] }
        return null
    }

    fun getPrev(): File? {
        if (currentIndex < files.size - 1) { currentIndex++; return files[currentIndex] }
        return null
    }

    fun deleteCurrent(): Boolean {
        val file = getCurrent() ?: return false
        if (file.delete()) {
            updateHistory()
            if (currentIndex >= files.size) currentIndex = files.size - 1
            return true
        }
        return false
    }
    
    fun getCurrent(): File? = files.getOrNull(currentIndex)
}
