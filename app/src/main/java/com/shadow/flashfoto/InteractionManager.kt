package com.shadow.flashfoto

import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

class InteractionManager(
    private val activity: MainActivity,
    private val camera: CameraHandler,
    private val history: HistoryManager,
    private val settings: SettingsManager
) {

    fun setup() {
        // Кнопка Камери
        activity.findViewById<Button>(R.id.btnCapture).setOnClickListener { 
            camera.capture() 
        }

        // Навігація Назад
        activity.findViewById<Button>(R.id.btnPrev).setOnClickListener { 
            val file = history.getPrev()
            if (file != null) activity.display(file) 
            else Toast.makeText(activity, "Це початок історії", Toast.LENGTH_SHORT).show()
        }

        // Навігація Вперед
        activity.findViewById<Button>(R.id.btnNext).setOnClickListener { 
            val file = history.getNext()
            if (file != null) activity.display(file) 
            else Toast.makeText(activity, "Це останнє фото", Toast.LENGTH_SHORT).show()
        }
        
        // Кнопка Налаштувань
        activity.findViewById<ImageView>(R.id.btnSettings).setOnClickListener { 
            SettingsDialogHandler(activity, settings).show() 
        }
        
        // Кнопка Друку (повторного)
        val btnPrint = activity.findViewById<Button>(R.id.btnPrint)
        btnPrint.setOnClickListener { 
            history.getCurrent()?.let { file ->
                PrintManager.printFromFile(activity, file, settings) 
            }
        }
    }
}
