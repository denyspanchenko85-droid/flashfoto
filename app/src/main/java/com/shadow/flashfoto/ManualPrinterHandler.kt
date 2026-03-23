package com.shadow.flashfoto

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout

class ManualPrinterHandler(private val context: Context, private val manager: PrinterManager) {

    fun show(onAdded: () -> Unit) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }
        val editName = EditText(context).apply { hint = "Назва" }
        val editIp = EditText(context).apply { hint = "IP адреса" }
        layout.addView(editName)
        layout.addView(editIp)

        AlertDialog.Builder(context)
            .setTitle("Новий принтер")
            .setView(layout)
            .setPositiveButton("Зберегти") { _, _ ->
                val n = editName.text.toString()
                val i = editIp.text.toString()
                if (n.isNotEmpty() && i.isNotEmpty()) {
                    manager.addPrinter(n, i)
                    onAdded()
                }
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }
}
