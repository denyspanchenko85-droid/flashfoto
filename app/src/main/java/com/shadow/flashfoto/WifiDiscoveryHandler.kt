package com.shadow.flashfoto

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class WifiDiscoveryHandler(private val context: Context, private val manager: PrinterManager) {
    private val wdManager = WifiDirectManager(context)

    fun start(onSuccess: () -> Unit) {
        val activity = context as? Activity ?: return
        
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissions.any { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(activity, permissions, 1001)
            return
        }

        // Викликаємо пошук
        wdManager.discoverPeers { devices ->
            // Тут буде діалог вибору, який викликає onSuccess()
        }
    }
}
