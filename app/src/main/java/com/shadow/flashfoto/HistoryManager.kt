package com.shadow.flashfoto

import java.io.File

class HistoryManager(private val directory: File) {
    private var files: List<File> = emptyList()
    var currentIndex: Int = -1

    fun updateHistory() {
        files = directory.listFiles { file -> file.extension == "jpg" || file.extension == "png" }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
        if (currentIndex == -1 && files.isNotEmpty()) currentIndex = 0
    }

    fun getNext(): File? {
        if (currentIndex > 0) currentIndex--
        return files.getOrNull(currentIndex)
    }

    fun getPrev(): File? {
        if (currentIndex < files.size - 1) currentIndex++
        return files.getOrNull(currentIndex)
    }
    
    fun getCurrent(): File? = files.getOrNull(currentIndex)
}
