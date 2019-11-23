package com.aeye.facedetection;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class TelemetryService extends Service {
    private static final String TAG = "TelemetryService";

    private Looper telemetryLooper;
    private TelemetryHandler telemetryHandler;

    public TelemetryService() {
    }

    private final class TelemetryHandler extends Handler {
        public TelemetryHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                Log.i(TAG, "Mesaj alındı");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Kesme isteği geldiğinde isteği uygulama
                Thread.currentThread().interrupt();
            }

            // Servisin tekrar tekrar oluşturulmasından dolayı oluşan maliyeti engellemek adına kapatıldı
            // stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Servis oluşturuldu");

        // Arkaplanda çalışacak thread'in tanımlanması ve başlatılması (UI thread'i bloklamaması lazım)
        HandlerThread thread = new HandlerThread("TelemetryStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        telemetryLooper = thread.getLooper();
        telemetryHandler = new TelemetryHandler(telemetryLooper);

        Notification notification = showNotification();
        startForeground(1, notification);
    }

    public Notification showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return new Notification.Builder(this, NotificationChannel.DEFAULT_CHANNEL_ID)
                    .setContentTitle("Temp")
                    .setContentText("Temp")
                    .setSmallIcon(R.mipmap.face_deteciton)
                    .setContentIntent(pendingIntent)
                    .setTicker("Temp")
                    .build();
        }

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Servis başlatıldı");

        Message msg = telemetryHandler.obtainMessage();
        msg.arg1 = startId; // İsteklerin yönetimi için kimlikleri saklamalıyız
        telemetryHandler.sendMessage(msg);

        // Eğer servis öldüyse, bu dönüşten sonra Intent'siz tekrar başlat
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Servis kapatıldı");
    }
}
