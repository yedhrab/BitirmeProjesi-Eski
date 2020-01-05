package com.aeye.facedetection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    Button btn_gallery, btn_telemetry, btn_wifi;

    static final int PERMISSION_FOREGORUND = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        bindEvents();
    }

    void initViews() {
        btn_gallery = findViewById(R.id.btn_gallery);
        btn_telemetry = findViewById(R.id.btn_telemetry);
        btn_wifi = findViewById(R.id.btn_wifi);
    }

    void bindEvents() {
        btn_gallery.setOnClickListener((view) -> startActivity(new Intent(MainActivity.this, GalleryActivity.class)));
        btn_telemetry.setOnClickListener((view) -> startTelemetryService());
        btn_wifi.setOnClickListener((view) -> startActivity(new Intent(MainActivity.this,
                WiFiDirectActivity.class)));
    }

    void startTelemetryService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.FOREGROUND_SERVICE)) {
                Toast.makeText(this, "Haberleşme hizmeti için izne ihtiyaç vardır", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, PERMISSION_FOREGORUND);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, PERMISSION_FOREGORUND);
                }
            }
        } else {
            startService(new Intent(this, TelemetryService.class).setAction(TelemetryService.ACTION_START_SERVICE));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_FOREGORUND) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTelemetryService();
            }
        }
    }
}
