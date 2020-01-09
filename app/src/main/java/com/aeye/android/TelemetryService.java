package com.aeye.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class TelemetryService extends Service {
    static final String ACTION_START_SERVICE = "Start telemetry service";
    static final String ACTION_STOP_SERVICE = "Stop telemetry service";
    private static final String TAG = "TelemetryService";
    private static final int REQUEST_SHOW_CONTENT = 0;
    private static final int REQUEST_STOP = 1;
    private Looper telemetryLooper;
    private TelemetryHandler telemetryHandler;

    public TelemetryService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Servis oluşturuldu");

        // Arkaplanda çalışacak thread'in tanımlanması ve başlatılması (UI thread'i bloklamaması lazım)
        HandlerThread thread = new HandlerThread("TelemetryStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        telemetryLooper = thread.getLooper();
        telemetryHandler = new TelemetryHandler(telemetryLooper);

        startForeground();
    }

    public void startForeground() {
        String channelId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel();
        }

        // Main uygulamayı açma isteği oluşturma
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, REQUEST_SHOW_CONTENT, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Servisi kapatma isteği oluşturma
        Intent stopSelf = new Intent(this, TelemetryService.class);
        stopSelf.setAction(TelemetryService.ACTION_STOP_SERVICE);
        PendingIntent pStopSelf;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            pStopSelf = PendingIntent.getForegroundService(this, REQUEST_STOP, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            pStopSelf = PendingIntent.getService(this, REQUEST_STOP, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        // Intent exitIntent = new Intent(this, MainActivity.class);
        // exitIntent.setAction(Intent.)


        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("Gözlük ile haberleşme aktif")
                .setContentText("Akıllı gözlüğünüz ile arkaplanda haberleşme gerçekleşmektedir")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher_foreground, "Kapat", pStopSelf)
                .build();

        startForeground(101, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        String channelId = "telemetry";
        NotificationChannel channel = new NotificationChannel(channelId, "Telemetry Service", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }

        return channelId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String intentAction = intent.getAction();
        if (intentAction != null) {
            switch (intentAction) {
                case TelemetryService.ACTION_START_SERVICE: {
                    Log.d(TAG, "Servis başlatıldı");

                    Message msg = telemetryHandler.obtainMessage();
                    msg.arg1 = startId; // İsteklerin yönetimi için kimlikleri saklamalıyız
                    telemetryHandler.sendMessage(msg);

                    break;
                }
                case TelemetryService.ACTION_STOP_SERVICE: {
                    stopForegroundService();
                }
            }
        }

        // Eğer servis öldüyse, bu dönüşten sonra Intent'siz tekrar başlat
        return START_STICKY;
    }

    private void stopForegroundService() {
        Log.d(TelemetryService.TAG, "Servis sonlandırılıyor");

        stopForeground(true);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Servis kapatıldı");
    }

    private final class TelemetryHandler extends Handler {
        public TelemetryHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                Log.d(TAG, "Mesaj alındı");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Kesme isteği geldiğinde isteği uygulama
                Thread.currentThread().interrupt();
            }

            // Servisin tekrar tekrar oluşturulmasından dolayı oluşan maliyeti engellemek adına kapatıldı
            // stopSelf(msg.arg1);
        }
    }
}
