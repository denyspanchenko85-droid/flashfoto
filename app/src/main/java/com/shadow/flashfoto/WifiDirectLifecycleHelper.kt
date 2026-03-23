package com.shadow.flashfoto

import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager

class WifiDirectLifecycleHelper(private val context: Context) {
    private val manager: WifiP2pManager? = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    private val channel: WifiP2pManager.Channel? = manager?.initialize(context, context.mainLooper, null)
    private var receiver: WifiDirectReceiver? = null

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    fun register(onPeersChanged: () -> Unit, onConnectionChanged: () -> Unit) {
        receiver = WifiDirectReceiver(manager, channel, onPeersChanged, onConnectionChanged)
        context.registerReceiver(receiver, intentFilter)
    }

    fun unregister() {
        receiver?.let {
            context.unregisterReceiver(it)
            receiver = null
        }
    }
}
