package com.shadow.flashfoto

import android.app.AlertDialog
import android.content.Context
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class SettingsDialogHandler(private val context: Context, private val settings: SettingsManager) {

    fun show() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Налаштування")

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 10)
        }

        val checkAutoPrint = CheckBox(context).apply {
            text = "Автоматичний друк"
            isChecked = settings.isAutoPrintEnabled
        }
        layout.addView(checkAutoPrint)

        val checkKeepRaw = CheckBox(context).apply {
            text = "Зберігати оригінал фото"
            isChecked = settings.isKeepOriginalEnabled
        }
        layout.addView(checkKeepRaw)

        // КНОПКА ВИБОРУ ШАБЛОНУ
        val btnPick = Button(context).apply {
            text = "Вибрати шаблон (PNG)"
            setOnClickListener {
                // Викликаємо метод MainActivity через каст контексту
                (context as? MainActivity)?.pickTemplateIntent()
            }
        }
        layout.addView(btnPick)

        val txtPath = TextView(context).apply {
            text = "Файл: ${settings.customTemplatePath?.let { it.split("/").last() } ?: "Стандартний"}"
            textSize = 10f
        }
        layout.addView(txtPath)

        val editIp = EditText(context).apply {
            hint = "IP принтера"
            setText(settings.printerIp)
        }
        layout.addView(editIp)

        builder.setView(layout)
        builder.setPositiveButton("Зберегти") { _, _ ->
            settings.isAutoPrintEnabled = checkAutoPrint.isChecked
            settings.isKeepOriginalEnabled = checkKeepRaw.isChecked
            settings.printerIp = editIp.text.toString()
            Toast.makeText(context, "Збережено", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Скасувати", null)
        builder.show()
    }
}
