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

        // РЕЖИМ РОБОТИ
        layout.addView(TextView(context).apply { text = "Режим роботи:"; setPadding(0, 10, 0, 10) })
        val rgMode = RadioGroup(context)
        val rbHistory = RadioButton(context).apply { text = "Історія"; id = View.generateViewId() }
        val rbDual = RadioButton(context).apply { text = "Конструктор (2 шари)"; id = View.generateViewId() }
        rgMode.addView(rbHistory)
        rgMode.addView(rbDual)
        if (settings.appMode == 0) rbHistory.isChecked = true else rbDual.isChecked = true
        layout.addView(rgMode)

        val checkAutoPrint = CheckBox(context).apply {
            text = "Автоматичний друк"
            isChecked = settings.isAutoPrintEnabled
        }
        layout.addView(checkAutoPrint)

        val checkKeepRaw = CheckBox(context).apply {
            text = "Зберігати оригінал"
            isChecked = settings.isKeepOriginalEnabled
        }
        layout.addView(checkKeepRaw)

        // КНОПКИ ШАБЛОНІВ
        val btnSelect = Button(context).apply {
            text = "Вибрати шаблон зі списку"
            setOnClickListener { showInternalPicker() }
        }
        layout.addView(btnSelect)

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
            settings.isKeepOriginalEnabled = checkKeepRaw.isChecked
            settings.printerIp = editIp.text.toString()
            
            // Тепер Activity імпортовано і каст спрацює
            (context as? Activity)?.recreate()
        }
        builder.setNegativeButton("Скасувати", null)
        builder.show()
    }

    private fun showInternalPicker() {
        val templateDir = File(context.getExternalFilesDir(null), "Templates")
        val files = templateDir.listFiles { f -> f.extension == "png" } ?: emptyArray()
        val names = files.map { it.name }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("Шаблони")
            .setItems(names) { _, which ->
                settings.customTemplatePath = files[which].absolutePath
                Toast.makeText(context, "Змінено", Toast.LENGTH_SHORT).show()
            }.show()
    }
}
