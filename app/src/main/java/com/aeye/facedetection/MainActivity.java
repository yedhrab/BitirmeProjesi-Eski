package com.aeye.facedetection;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btn_gallery, btn_telemetry;

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
    }

    void bindEvents() {
        btn_gallery.setOnClickListener((view) -> startActivity(new Intent(this, GalleryActivity.class)));
        btn_telemetry.setOnClickListener((view) -> startTelemetryService());
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
            startService(new Intent(this, TelemetryService.class));
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
