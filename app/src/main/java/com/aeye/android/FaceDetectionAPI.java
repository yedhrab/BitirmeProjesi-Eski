package com.aeye.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

import static android.content.Context.CAMERA_SERVICE;

abstract class FaceDetectionAPI {
    private static final String TAG = "ML KIT";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static FirebaseVisionImage converFirebaseVisionImage(Image mediaImage, int rotation) {
        return FirebaseVisionImage.fromMediaImage(mediaImage, rotation);
    }

    static FirebaseVisionImage converFirebaseVisionImage(Bitmap bitmap) {
        return FirebaseVisionImage.fromBitmap(bitmap);
    }

    static FirebaseVisionImage converFirebaseVisionImage(ByteBuffer buffer, int rotation) {
        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(480)   // 480x360 is typically sufficient for
                .setHeight(360)  // image recognition
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(rotation)
                .build();

        return FirebaseVisionImage.fromByteBuffer(buffer, metadata);
    }

    static FirebaseVisionImage converFirebaseVisionImage(byte[] byteArray, int rotation) {
        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(480)   // 480x360 is typically sufficient for
                .setHeight(360)  // image recognition
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(rotation)
                .build();

        return FirebaseVisionImage.fromByteArray(byteArray, metadata);
    }

    static FirebaseVisionImage converFirebaseVisionImage(Context context, Uri uri) throws IOException {
        return FirebaseVisionImage.fromFilePath(context, uri);
    }

    /**
     * Resim üzerindeki yüzleri algılama
     *
     * @param image Resim verisi
     */
    static void detectFaces(FirebaseVisionImage image, DetectListener detectListener) {
        // Yüz algılama modeli ayaları
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)
                        .enableTracking()
                        .build();

        // Algılayıcıyı tanımalama
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        Task<List<FirebaseVisionFace>> result = detector.detectInImage(image)
                .addOnSuccessListener(detectListener::onDetect)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static private int getRotationCompensation(String cameraId, Activity activity, Context context)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        int result;

        try {
            CameraManager cameraManager = Objects.requireNonNull((CameraManager) context.getSystemService(CAMERA_SERVICE));
            CameraCharacteristics cameraCharacteristics = Objects.requireNonNull(cameraManager.getCameraCharacteristics(cameraId));
            int sensorOrientation = Objects.requireNonNull(cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION));
            rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

            switch (rotationCompensation) {
                case 0:
                    result = FirebaseVisionImageMetadata.ROTATION_0;
                    break;
                case 90:
                    result = FirebaseVisionImageMetadata.ROTATION_90;
                    break;
                case 180:
                    result = FirebaseVisionImageMetadata.ROTATION_180;
                    break;
                case 270:
                    result = FirebaseVisionImageMetadata.ROTATION_270;
                    break;
                default:
                    result = FirebaseVisionImageMetadata.ROTATION_0;
                    Log.e(TAG, "Bad rotation value: " + rotationCompensation);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            result = FirebaseVisionImageMetadata.ROTATION_0;
            Log.e(TAG, "Error on camera");
        }

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        return result;
    }

    interface DetectListener {
        void onDetect(List<FirebaseVisionFace> faceList);
    }
}
