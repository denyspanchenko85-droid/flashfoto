// Responsibility: Safe BroadcastReceiver lifecycle management
package com.shadow.flashfoto

import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager

class WifiDirectLifecycleHelper(private val context: Context) {
    private val wdManager = WifiDirectManager(context)
    private var receiver: WifiDirectReceiver? = null

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
    }

    fun register(onPeersAvailable: (List<WifiP2pDevice>) -> Unit, onConnectionChanged: () -> Unit) {
        receiver = WifiDirectReceiver(
            wdManager.manager, 
            wdManager.channel, 
            onPeersAvailable, 
            onConnectionChanged
        )
        context.registerReceiver(receiver, intentFilter)
    }

    fun unregister() {
        receiver?.let {
            context.unregisterReceiver(it)
            receiver = null
        }
    }
}
