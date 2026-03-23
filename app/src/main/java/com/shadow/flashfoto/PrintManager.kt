// Responsibility: Logic for socket printing, system dialog, and file decoding
package com.shadow.flashfoto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.print.PrintHelper
import java.io.File
import java.net.Socket
import kotlin.concurrent.thread

object PrintManager {

    // Друк безпосередньо з файлу (зручно для історії)
    fun printFromFile(context: Context, file: File, settings: SettingsManager) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        if (bitmap != null) {
            print(context, bitmap, settings)
        }
    }

    fun print(context: Context, bitmap: Bitmap, settings: SettingsManager) {
        if (settings.isAutoPrintEnabled && !settings.printerIp.isNullOrBlank()) {
            silentPrint(context, bitmap, settings.printerIp!!)
        } else {
            showSystemPrintDialog(context, bitmap)
        }
    }

    private fun silentPrint(context: Context, bitmap: Bitmap, ip: String) {
        thread {
            try {
                val socket = Socket(ip, 9100)
                val out = socket.getOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
                socket.close()
            } catch (e: Exception) {
                Logger.log(context, "Silent Print Failed", e)
                // Можна додати сповіщення про помилку, якщо треба
            }
        }
    }

    private fun showSystemPrintDialog(context: Context, bitmap: Bitmap) {
        PrintHelper(context).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
            printBitmap("FlashFoto-Print", bitmap)
        }
    }
}
