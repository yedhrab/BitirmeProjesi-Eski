package com.aeye.facedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn_gallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        bindEvents();
    }

    void initViews() {
        btn_gallery = findViewById(R.id.btn_gallery);
    }

    void bindEvents() {
        btn_gallery.setOnClickListener((view) -> startActivity(new Intent(MainActivity.this, GalleryActivity.class)));
    }

}
