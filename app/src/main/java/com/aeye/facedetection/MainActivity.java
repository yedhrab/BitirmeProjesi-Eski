package com.aeye.facedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn_gallery, btn_wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        bindEvents();
    }

    void initViews() {
        btn_gallery = findViewById(R.id.btn_gallery);
        btn_wifi = findViewById(R.id.btn_wifi);
    }

    void bindEvents() {
        btn_gallery.setOnClickListener((view) -> startActivity(new Intent(MainActivity.this, GalleryActivity.class)));
        btn_wifi.setOnClickListener((view) -> startActivity(new Intent(MainActivity.this, WifiActivity.class)));
    }

}
