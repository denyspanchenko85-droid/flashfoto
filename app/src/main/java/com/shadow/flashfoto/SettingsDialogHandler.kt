package com.shadow.flashfoto

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.*
import java.io.File

class SettingsDialogHandler(private val context: Context, private val settings: SettingsManager) {

    fun show() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Налаштування")

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 10)
        }

        // 1. РЕЖИМ РОБОТИ (RadioButtons)
        layout.addView(TextView(context).apply { text = "Режим роботи:"; setPadding(0, 10, 0, 10) })
        val rgMode = RadioGroup(context)
        val rbHistory = RadioButton(context).apply { text = "Історія (Готові)"; id = View.generateViewId() }
        val rbDual = RadioButton(context).apply { text = "Конструктор (2 шари)"; id = View.generateViewId() }
        
        rgMode.addView(rbHistory); rgMode.addView(rbDual)
        if (settings.appMode == 0) rbHistory.isChecked = true else rbDual.isChecked = true
        layout.addView(rgMode)

        // 2. ЧЕКБОКСИ
        val checkAutoPrint = CheckBox(context).apply {
            text = "Автоматичний друк"
            isChecked = settings.isAutoPrintEnabled
        }
        layout.addView(checkAutoPrint)

        val checkKeepRaw = CheckBox(context).apply {
            text = "Зберігати оригінали (Raw)"
            isChecked = settings.isKeepOriginalEnabled
        }
        layout.addView(checkKeepRaw)

        // 3. ШАБЛОНИ
        layout.addView(TextView(context).apply { text = "\nШаблони:"; textSize = 14f })
        val btnImport = Button(context).apply {
            text = "Додати новий PNG..."
            setOnClickListener { (context as? MainActivity)?.pickTemplateIntent() }
        }
        layout.addView(btnImport)

        // 4. УПРАВЛІННЯ ПРИНТЕРАМИ (ЗАМІСТЬ IP EDITTEXT)
        layout.addView(TextView(context).apply { text = "\nПринтер:"; textSize = 14f })
        
        val printerManager = PrinterManager(context)
        val btnManagePrinters = Button(context).apply {
            val activeIp = printerManager.getActiveIp()
            text = if (activeIp.isEmpty()) "Вибрати принтер" else "Принтер: $activeIp"
            setOnClickListener { 
                // Викликаємо нове вікно керування принтерами
                PrinterDialogHandler(context).show() 
            }
        }
        layout.addView(btnManagePrinters)

        builder.setView(layout)
        builder.setPositiveButton("Зберегти") { _, _ ->
            settings.appMode = if (rbHistory.isChecked) 0 else 1
            settings.isAutoPrintEnabled = checkAutoPrint.isChecked
            settings.isKeepOriginalEnabled = checkKeepRaw.isChecked
            
            // IP тепер зберігається всередині PrinterManager через його власний діалог
            
            (context as? Activity)?.recreate()
        }
        builder.setNegativeButton("Скасувати", null)
        builder.show()
    }
}
