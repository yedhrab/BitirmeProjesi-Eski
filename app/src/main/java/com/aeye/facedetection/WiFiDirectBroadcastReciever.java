package com.aeye.facedetection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

import static com.aeye.facedetection.WifiActivity.TAG;

/**
 * Önemli WiFi olaylarını yayınlayan sınıf
 * https://developer.android.com/guide/topics/connectivity/wifip2p.html#create-br
 */
public class WiFiDirectBroadcastReciever extends BroadcastReceiver {

    WifiP2pManager manager;
    Channel channel;
    WifiActivity wifiActivity;

    public WiFiDirectBroadcastReciever(WifiP2pManager manager, Channel channel, WifiActivity wifiActivity) {
        super();

        this.manager = manager;
        this.channel = channel;
        this.wifiActivity = wifiActivity;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // TODO
        if (action != null) {
            switch (action) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    Log.d(TAG, "WIFI_P2P_STATE_CHANGED_ACTION");

                    // WiFi P2P durumununa göre işlemler yapma
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Log.d(TAG, "WIFI_P2P_STATE_ENABLED");

                    } else {
                        Log.d(TAG, "WIFI_P2P_STATE_DISABLED");
                    }
                    break;
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                    Log.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");

                    if (manager != null) {
                        Log.d(TAG, "Request peers");
                        // manager.requestPeers(channel, myPeerListListener);
                    }
                    break;
                // Respond to new connection or disconnections
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                    Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
                    break;
                // Respond to this device's wifi state changing
                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                    Log.d(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
                    break;
            }
        }

    }
}
