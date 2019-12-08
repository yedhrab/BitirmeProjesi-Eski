package com.aeye.facedetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.io.IOException;

public class GalleryActivity extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    ImageView iv_detected_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        iv_detected_image = findViewById(R.id.iv_detected_image);
        Button btnChoseImage = findViewById(R.id.btnChoseImage);


        btnChoseImage.setOnClickListener((view) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    //permission no
                    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions, PERMISSION_CODE);
                } else {
                    //permission already granted
                    PickImageFromGallery();
                }

            } else {
                PickImageFromGallery();
            }
        });
    }

    private void PickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PickImageFromGallery();
                } else {
                    //
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null) {
            Uri imageUri = data.getData();
            iv_detected_image.setImageURI(imageUri);

            try {
                FirebaseVisionImage fImage = FaceDetectionAPI.converFirebaseVisionImage(this, imageUri);
                FaceDetectionAPI.detectFaces(fImage, faceList -> {
                    for (FirebaseVisionFace face : faceList) {
                        // TODO: Burada işlemler yapılacak
                        Rect bounds = face.getBoundingBox();
                        Log.i(TAG, String.format("(%s, %s, %s, %s", bounds.bottom, bounds.left, bounds.right, bounds.top));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}