// Responsibility: UI generation for the printer list entries
package com.shadow.flashfoto

import android.content.Context
import android.view.Gravity
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton

class PrinterListRenderer(private val context: Context, private val manager: PrinterManager) {

    fun fill(layout: LinearLayout, onUpdate: () -> Unit) {
        layout.removeAllViews()
        val printers = manager.getPrinters()

        printers.forEach { printer ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 10, 0, 10)
                gravity = Gravity.CENTER_VERTICAL
            }

            val rb = RadioButton(context).apply {
                isChecked = printer.isActive
                text = "${printer.name}\n${printer.ip}"
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                setOnClickListener {
                    manager.setActive(printer.ip)
                    onUpdate()
                }
            }

            val btnDel = ImageButton(context).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                setBackgroundColor(0)
                setOnClickListener {
                    manager.deletePrinter(printer)
                    onUpdate()
                }
            }

            row.addView(rb)
            row.addView(btnDel)
            layout.addView(row)
        }
    }
}
