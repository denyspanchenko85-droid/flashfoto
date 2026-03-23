package com.shadow.flashfoto

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

        // --- ВИБІР РЕЖИМУ (RadioButton) ---
        layout.addView(TextView(context).apply { text = "Режим роботи:"; textSize = 14f; setPadding(0, 10, 0, 10) })
        
        val rgMode = RadioGroup(context)
        val rbHistory = RadioButton(context).apply { text = "Історія (Готові фото)"; id = View.generateViewId() }
        val rbConstructor = RadioButton(context).apply { text = "Конструктор (2 шари)"; id = View.generateViewId() }
        
        rgMode.addView(rbHistory)
        rgMode.addView(rbConstructor)
        
        if (settings.appMode == 0) rbHistory.isChecked = true else rbConstructor.isChecked = true
        layout.addView(rgMode)

        // --- ПЕРЕМИКАЧІ ---
        val checkAutoPrint = CheckBox(context).apply {
            text = "Автоматичний друк"
            isChecked = settings.isAutoPrintEnabled
            setPadding(0, 20, 0, 10)
        }
        layout.addView(checkAutoPrint)

        val checkKeepRaw = CheckBox(context).apply {
            text = "Зберігати оригінал (Raw)"
            isChecked = settings.isKeepOriginalEnabled
        }
        layout.addView(checkKeepRaw)

        // --- УПРАВЛІННЯ ШАБЛОНАМИ ---
        layout.addView(TextView(context).apply { text = "\nШаблони:"; textSize = 14f })
        
        val btnSelect = Button(context).apply {
            text = "Вибрати зі списку"
            setOnClickListener { showInternalTemplatePicker() }
        }
        layout.addView(btnSelect)

        val btnImport = Button(context).apply {
            text = "Додати новий PNG..."
            setOnClickListener { (context as? MainActivity)?.pickTemplateIntent() }
        }
        layout.addView(btnImport)

        // --- ПРИНТЕР ---
        layout.addView(TextView(context).apply { text = "\nIP принтера:"; textSize = 14f })
        val editIp = EditText(context).apply {
            hint = "192.168.x.x"
            setText(settings.printerIp)
        }
        layout.addView(editIp)

        builder.setView(layout)
        builder.setPositiveButton("Зберегти") { _, _ ->
            // Зберігаємо режим: 0 - Історія, 1 - Конструктор
            settings.appMode = if (rbHistory.isChecked) 0 else 1
            settings.isAutoPrintEnabled = checkAutoPrint.isChecked
            settings.isKeepOriginalEnabled = checkKeepRaw.isChecked
            settings.printerIp = editIp.text.toString()
            
            // Перезапускаємо Activity, щоб InteractionManager оновив кнопки
            (context as? Activity)?.recreate()
        }
        builder.setNegativeButton("Скасувати", null)
        builder.show()
    }

    private fun showInternalTemplatePicker() {
        val templateDir = File(context.getExternalFilesDir(null), "Templates")
        val files = templateDir.listFiles { f -> f.extension == "png" } ?: emptyArray()
        
        if (files.isEmpty()) {
            Toast.makeText(context, "Немає шаблонів", Toast.LENGTH_SHORT).show()
            return
        }

        val names = files.map { it.name }.toTypedArray()
        AlertDialog.Builder(context)
            .setTitle("Виберіть шаблон")
            .setItems(names) { _, which ->
                settings.customTemplatePath = files[which].absolutePath
                Toast.makeText(context, "Активовано: ${names[which]}", Toast.LENGTH_SHORT).show()
            }.show()
    }
}
