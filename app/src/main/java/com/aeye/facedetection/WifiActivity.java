package com.aeye.facedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;

/**
 * Wifi işlemleri yöneten sınıf
 * https://developer.android.com/guide/topics/connectivity/wifip2p.html#create-app
 */
public class WifiActivity extends AppCompatActivity {
    WifiP2pManager manager;
    Channel channel;
    BroadcastReceiver wifiReceiver;

    IntentFilter wifiFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        // WiFi değişikliklerinde reciever'ı çalıştırma
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (manager != null) {
            channel = manager.initialize(this, getMainLooper(), null);
            wifiReceiver = new WiFiDirectBroadcastReciever(manager, channel, this);
        }

        // reciever'a aktarılacak broadcast türlerini belirleme
        wifiFilter = new IntentFilter();
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Alıcıyı kaydetme
        registerReceiver(wifiReceiver, wifiFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Alının kaydını silme
        unregisterReceiver(wifiReceiver);
    }
}
