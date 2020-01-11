package com.aeye.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Wi-Fi Direct (P2P) ile veri gönderme
 * https://developer.android.com/guide/topics/connectivity/wifip2p#transfer
 */
public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String TAG = FileServerAsyncTask.class.getSimpleName();
    public static final int PORT = 0; // For auto port allocation, make it 0

    private Context context;
    private InetAddress inetAddress;

    public FileServerAsyncTask(Context context, InetAddress inetAddress) {
        this.context = context;
        this.inetAddress = inetAddress;
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte[] buf = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return false;
        }
        return true;
    }

    @Override
    protected String doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground: Dosya alma hizmeti başlatıldı");
        try {
            Log.d(TAG, "doInBackground: Soket oluşturulup, bağlanmaya çalışılyor...");
            // Sunucu oluşturma ve istemcinin bağlanmasını bekleme (UI threadi bloklar)
            // https://stackoverflow.com/a/27266476/9770490 (yeni yöntem)
            Socket socket = new Socket();
            socket.bind(null);
            socket.connect(new InetSocketAddress(inetAddress, FileServerAsyncTask.PORT));

            Log.i(TAG, "doInBackground: Socket'e başarıyla bağlanıldı.");

            // Bağlantı başarılı olursa bu adıma geçecektir
            final File file = new File(Environment.getExternalStorageDirectory() + "/temp.jpg");
            File dirs = file.getParentFile();
            if (!dirs.exists()) {
                dirs.mkdirs();
            }
            file.createNewFile();

            InputStream inputstream = socket.getInputStream();
            if (copyFile(inputstream, new FileOutputStream(file))) {
                Log.d(TAG, "doInBackground: Dosya başarıyla kopyalandı " + file.getAbsolutePath());
            } else {
                Log.w(TAG, "doInBackground: Dosya kopyalaması yarım kaldı " + file.getAbsolutePath());
            }

            socket.close();
            return file.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        // Resmi dosya sistemiyle açma
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + result), "image/*");
        context.startActivity(intent);
    }
}
