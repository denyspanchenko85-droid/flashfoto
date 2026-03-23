package com.shadow.flashfoto

import android.app.AlertDialog
import android.content.Context
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

        // 1. ПЕРЕМИКАЧІ
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

        // 2. РОБОТА З ШАБЛОНАМИ
        layout.addView(TextView(context).apply { text = "\nШаблон:"; textSize = 14f; setTextColor(0xFF000000.toInt()) })

        // Кнопка вибору зі списку (у папці Templates)
        val btnSelectInternal = Button(context).apply {
            text = "Вибрати зі списку"
            setOnClickListener { showInternalTemplatePicker() }
        }
        layout.addView(btnSelectInternal)

        // Кнопка завантаження нового
        val btnImportExternal = Button(context).apply {
            text = "Додати новий PNG..."
            setOnClickListener { (context as? MainActivity)?.pickTemplateIntent() }
        }
        layout.addView(btnImportExternal)

        val txtCurrent = TextView(context).apply {
            val name = settings.customTemplatePath?.split("/")?.last() ?: "Не вибрано"
            text = "Активний: $name"
            textSize = 10f
        }
        layout.addView(txtCurrent)

        // 3. ПРИНТЕР
        layout.addView(TextView(context).apply { text = "\nIP принтера:"; textSize = 14f })
        val editIp = EditText(context).apply {
            hint = "192.168.x.x"
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

    private fun showInternalTemplatePicker() {
        val templateDir = File(context.getExternalFilesDir(null), "Templates")
        val files = templateDir.listFiles { f -> f.extension == "png" } ?: emptyArray()
        
        if (files.isEmpty()) {
            Toast.makeText(context, "Папка шаблонів порожня", Toast.LENGTH_SHORT).show()
            return
        }

        val names = files.map { it.name }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("Виберіть шаблон")
            .setItems(names) { _, which ->
                settings.customTemplatePath = files[which].absolutePath
                Toast.makeText(context, "Активовано: ${names[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
