package com.shadow.flashfoto

import java.io.File

class HistoryManager(private val directory: File) {
    private var files: List<File> = emptyList()
    var currentIndex: Int = -1

    init { updateHistory() }

    fun updateHistory() {
        // Додаємо перевірку exists(), щоб не було помилок, якщо папки ще немає
        files = if (directory.exists()) {
            directory.listFiles { f -> 
                val ext = f.extension.lowercase()
                ext == "jpg" || ext == "png" 
            }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }

        // ВАЖЛИВО: Коли ми робимо нове фото, ми хочемо, щоб воно стало поточним (індекс 0)
        if (files.isNotEmpty()) {
            currentIndex = 0
        } else {
            currentIndex = -1
        }
    }

    // getNext — іде до НОВІШИХ фото (вгору по списку до 0)
    fun getNext(): File? {
        if (currentIndex > 0) currentIndex--
        return files.getOrNull(currentIndex)
    }

    // getPrev — іде до СТАРІШИХ фото (вниз по списку до кінця)
    fun getPrev(): File? {
        if (currentIndex < files.size - 1) currentIndex++
        return files.getOrNull(currentIndex)
    }
    
    fun getCurrent(): File? = files.getOrNull(currentIndex)
}
