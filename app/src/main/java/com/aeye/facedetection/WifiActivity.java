package com.aeye.facedetection;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Wifi işlemleri yöneten sınıf
 * https://developer.android.com/guide/topics/connectivity/wifip2p.html#create-app
 */
public class WifiActivity extends AppCompatActivity {

    public static final String TAG = "WiFiActivity";

    private static final int PRC_ACCES_FINE_LOCATION = 1;

    WifiP2pManager manager;
    Channel channel;
    BroadcastReceiver wifiReceiver;

    IntentFilter wifiFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        // Wifi alıcısına aktarılacak broadcast türlerini belirleme
        wifiFilter = new IntentFilter();
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // WiFi değişikliklerinde reciever'ı çalıştırma
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (manager != null) {
            // Wi-Fi P2P Frameworkü ile uygulamamıza bağlanmayı sağlayacak obje
            channel = manager.initialize(this, getMainLooper(), null);

            getRequiredPermissions();
        }
    }

    /**
     * Wi-Fi P2P için gerekli izinleri alma
     */
    private void getRequiredPermissions() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, WifiActivity.PRC_ACCES_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PRC_ACCES_FINE_LOCATION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Fine locaiton izni gereklidir");
            } else {
                Toast.makeText(this, "İzinler gereklidir 😥", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Alıcıyı oluşturma ve sisteme kaydetme
        wifiReceiver = new WiFiDirectBroadcastReciever(manager, channel, this);
        registerReceiver(wifiReceiver, wifiFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Alıcının kaydını silme
        unregisterReceiver(wifiReceiver);
    }

    public void onDiscoverButtonClick(View view) {
        Log.d(TAG, "onDiscoverButtonClick: Discover butonuna tıklandı");

        // Eşleşebilir cihazları arama
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: Keşif başarılı");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure: Keşif başarısız" + reason);
            }
        });
    }
}
