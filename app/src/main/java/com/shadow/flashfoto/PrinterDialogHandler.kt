package com.shadow.flashfoto

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.*

class PrinterDialogHandler(private val context: Context) {
    private val manager = PrinterManager(context)
    private val listRenderer = PrinterListRenderer(context, manager)
    private val wifiHandler = WifiDiscoveryHandler(context, manager)
    private val manualHandler = ManualPrinterHandler(context, manager)

    fun show() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Управління принтерами")

        val mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        // Кнопка Wi-Fi Direct
        val btnWifiDirect = Button(context).apply {
            text = "Пошук Wi-Fi Direct (P2P)"
            setOnClickListener { 
                wifiHandler.start { show() } // Перезапуск діалогу після успіху
            }
        }
        mainLayout.addView(btnWifiDirect)

        // Контейнер списку
        val listLayout = LinearLayout(context).apply { 
            orientation = LinearLayout.VERTICAL 
            setPadding(0, 20, 0, 20)
        }
        
        // Початковий рендер
        val refresh = { listRenderer.fill(listLayout) { refresh() } }
        listRenderer.fill(listLayout) { listRenderer.fill(listLayout) { /* оновлення всередині */ } }

        val scroll = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
            addView(listLayout)
        }
        mainLayout.addView(scroll)

        // Додати вручну
        val btnAdd = Button(context).apply {
            text = "+ Додати IP вручну"
            setOnClickListener { 
                manualHandler.show { listRenderer.fill(listLayout) { /* оновлення */ } } 
            }
        }
        mainLayout.addView(btnAdd)

        builder.setView(mainLayout)
        builder.setPositiveButton("Закрити", null)
        builder.show()
    }
}
