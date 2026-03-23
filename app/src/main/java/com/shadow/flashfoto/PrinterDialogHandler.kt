package com.shadow.flashfoto

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.*

class PrinterDialogHandler(private val context: Context) {
    private val manager = PrinterManager(context)

    fun show() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Управління принтерами")

        val mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val listLayout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        refreshList(listLayout)

        val btnAdd = Button(context).apply {
            text = "+ Додати принтер"
            setOnClickListener { showAddDialog(listLayout) }
        }

        mainLayout.addView(ScrollView(context).apply { addView(listLayout) })
        mainLayout.addView(btnAdd)

        builder.setView(mainLayout)
        builder.setPositiveButton("Закрити", null)
        builder.show()
    }

    private fun refreshList(layout: LinearLayout) {
        layout.removeAllViews()
        val printers = manager.getPrinters()

        if (printers.isEmpty()) {
            layout.addView(TextView(context).apply { text = "Список порожній"; setPadding(0, 20, 0, 20) })
            return
        }

        printers.forEach { printer ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 10, 0, 10)
            }

            val rb = RadioButton(context).apply {
                isChecked = printer.isActive
                text = "${printer.name} (${printer.ip})"
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                setOnClickListener {
                    manager.setActive(printer.ip)
                    refreshList(layout)
                }
            }

            val btnDel = ImageButton(context).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                setBackgroundColor(0)
                setOnClickListener {
                    manager.deletePrinter(printer)
                    refreshList(layout)
                }
            }

            row.addView(rb)
            row.addView(btnDel)
            layout.addView(row)
        }
    }

    private fun showAddDialog(listLayout: LinearLayout) {
        val builder = AlertDialog.Builder(context)
        val inputLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
        }

        val editName = EditText(context).apply { hint = "Назва (напр. HP Office)" }
        val editIp = EditText(context).apply { hint = "IP (напр. 192.168.1.100)" }

        inputLayout.addView(editName)
        inputLayout.addView(editIp)

        builder.setTitle("Новий принтер")
            .setView(inputLayout)
            .setPositiveButton("Додати") { _, _ ->
                val name = editName.text.toString()
                val ip = editIp.text.toString()
                if (name.isNotEmpty() && ip.isNotEmpty()) {
                    manager.addPrinter(name, ip)
                    refreshList(listLayout)
                }
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }
}
