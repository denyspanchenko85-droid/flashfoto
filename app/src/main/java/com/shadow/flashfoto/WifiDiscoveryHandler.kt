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

    // Тепер start приймає обов'язковий колбек, як того вимагає твій PrinterDialogHandler
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

        wdManager.discoverPeers { devices ->
            if (devices.isNotEmpty()) {
                // Тут логіка показу списку пристроїв та виклику onSuccess() після коннекту
                // (Реалізація showPeerDialog може бути викликана тут)
            }
        }
    }
}
