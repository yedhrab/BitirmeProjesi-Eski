package com.aeye.facedetection;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn_gallery, btn_telemetry;

    static final int TELEMETRY_CHANNEL_ID = 1;

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
        btn_telemetry.setOnClickListener((view) -> startService(new Intent(this, TelemetryService.class)));
    }
}
