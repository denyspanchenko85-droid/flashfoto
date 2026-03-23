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

        // РЕЖИМ РОБОТИ (RadioButtons)
        layout.addView(TextView(context).apply { text = "Режим роботи:"; setPadding(0, 10, 0, 10) })
        val rgMode = RadioGroup(context)
        val rbHistory = RadioButton(context).apply { text = "Історія (Готові)"; id = View.generateViewId() }
        val rbDual = RadioButton(context).apply { text = "Конструктор (2 шари)"; id = View.generateViewId() }
        
        rgMode.addView(rbHistory); rgMode.addView(rbDual)
        if (settings.appMode == 0) rbHistory.isChecked = true else rbDual.isChecked = true
        layout.addView(rgMode)

        val checkAutoPrint = CheckBox(context).apply {
            text = "Автоматичний друк"
            isChecked = settings.isAutoPrintEnabled
        }
        layout.addView(checkAutoPrint)

        // Кнопки завантаження та IP...
        val btnImport = Button(context).apply {
            text = "Додати новий PNG..."
            setOnClickListener { (context as? MainActivity)?.pickTemplateIntent() }
        }
        layout.addView(btnImport)

        val editIp = EditText(context).apply {
            hint = "IP принтера"
            setText(settings.printerIp)
        }
        layout.addView(editIp)

        builder.setView(layout)
        builder.setPositiveButton("Зберегти") { _, _ ->
            settings.appMode = if (rbHistory.isChecked) 0 else 1
            settings.isAutoPrintEnabled = checkAutoPrint.isChecked
            settings.printerIp = editIp.text.toString()
            
            // Перезапуск для оновлення кнопок на головному екрані
            (context as? Activity)?.recreate()
        }
        builder.show()
    }
}
