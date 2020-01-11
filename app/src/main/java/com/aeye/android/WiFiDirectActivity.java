package com.aeye.android;

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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

/**
 * Wifi işlemleri yöneten sınıf
 * https://developer.android.com/guide/topics/connectivity/wifip2p.html#create-app
 */
public class WiFiDirectActivity extends AppCompatActivity {

    public static final String TAG = WiFiDirectActivity.class.getSimpleName();
    private static final int PRC_ACCES_FINE_LOCATION = 1;
    private final IntentFilter wifiFilter = new IntentFilter();
    WifiP2pManager manager;
    Channel channel;
    BroadcastReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct);

        initWifiFilter();
        initDependences();
    }

    /**
     * Wifi alıcısına aktarılacak broadcast türlerini belirleme
     */
    private void initWifiFilter() {
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void initDependences() {
        // WiFi değişikliklerinde reciever'ı çalıştırma
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        Objects.requireNonNull(manager);

        // Wi-Fi P2P Frameworkü ile uygulamamıza bağlanmayı sağlayacak obje
        channel = manager.initialize(this, getMainLooper(), null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getRequiredPermissions();
        }
    }

    /**
     * Wi-Fi P2P için gerekli izinleri alma
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getRequiredPermissions() {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, WiFiDirectActivity.PRC_ACCES_FINE_LOCATION);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean hasPermission(String permission) {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PRC_ACCES_FINE_LOCATION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "onRequestPermissionsResult: Fine location izni gereklidir");
                Toast.makeText(this, "İzinler gereklidir 😥", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerWifiFilter();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterWifiFilter();
    }

    /**
     * Broadcast alıcısını oluşturma ve sisteme kaydetme
     */
    private void registerWifiFilter() {
        wifiReceiver = new WiFiDirectBroadcastReciever(manager, channel, this);
        registerReceiver(wifiReceiver, wifiFilter);
    }

    /**
     * Broadcast alıcısının kaydını silme
     */
    private void unregisterWifiFilter() {
        unregisterReceiver(wifiReceiver);
    }


    public void onDiscoverButtonClick(View view) {
        // TODO: Bağlnatı sağladıktan sonra tekrar tekrar istek atmayı bırakmalı

        Log.v(TAG, "onDiscoverButtonClick: Discover butonuna tıklandı");

        // Eşleşebilir cihazları arama
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "onSuccess: Keşif başarılı, eşleşebilir cihazlar aranıyor...");
            }

            @Override
            public void onFailure(int reason) {
                String reasonMsg = "";
                switch (reason) {
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        reasonMsg = "P2P desteklenmiyor";
                        break;
                    case WifiP2pManager.ERROR:
                        reasonMsg = "hata oluştur";
                        break;
                    case WifiP2pManager.BUSY:
                        reasonMsg = "cihaz başka bir bağlantı ile meşgul";
                        break;
                }
                ;
                Log.e(TAG, "onFailure: Keşif başarısız, " + reasonMsg);
            }
        });
    }
}
