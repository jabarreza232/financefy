package id.co.evolution.financefy.worker;

import androidx.work.Worker;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
public class ModelDownloadWorker extends Worker {
    public static final String PROGRESS = "PROGRESS";
    public static final String SPEED = "SPEED";

    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "download_channel";
    private static final int NOTIFICATION_ID = 1;

    public ModelDownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @NonNull
    @Override
    public Result doWork() {
        String urlString = getInputData().getString("URL");
        String fileName = getInputData().getString("FILENAME");

        if (urlString == null || fileName == null) {
            return Result.failure();
        }

        // Tampilkan notifikasi Foreground agar Worker tidak dimatikan OS
        setForegroundAsync(createForegroundInfo(0, "Memulai unduhan..."));

        File file = new File(getApplicationContext().getExternalFilesDir(null), fileName);

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return Result.failure();
            }

            long fileLength = connection.getContentLength();

            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = new FileOutputStream(file);

            byte[] data = new byte[8192];
            long total = 0;
            int count;

            long lastTime = System.currentTimeMillis();
            long lastBytes = 0;

            while ((count = input.read(data)) != -1) {
                // Jika user membatalkan task dari WorkManager
                if (isStopped()) {
                    input.close();
                    output.close();
                    file.delete(); // Hapus file yang tidak selesai
                    return Result.failure();
                }

                total += count;
                output.write(data, 0, count);

                long currentTime = System.currentTimeMillis();
                long timeDiff = currentTime - lastTime;

                // Update progress setiap 1 detik
                if (timeDiff > 1000) {
                    int progress = (int) (total * 100 / fileLength);
                    long bytesDiff = total - lastBytes;
                    double speedInBytesPerSec = (bytesDiff / (timeDiff / 1000.0));
                    double speedInMbPerSec = speedInBytesPerSec / (1024.0 * 1024.0);

                    // Kirim data progress ke UI
                    Data progressData = new Data.Builder()
                            .putInt(PROGRESS, progress)
                            .putDouble(SPEED, speedInMbPerSec)
                            .build();
                    setProgressAsync(progressData);

                    // Update notifikasi Foreground
                    setForegroundAsync(createForegroundInfo(progress, String.format("%.1f MB/s", speedInMbPerSec)));

                    lastTime = currentTime;
                    lastBytes = total;
                }
            }

            output.flush();
            output.close();
            input.close();

            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            if (file.exists()) file.delete();
            return Result.failure();
        }
    }

    @NonNull
    private ForegroundInfo createForegroundInfo(int progress, String info) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Download Status", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Mengunduh Model AI Qwen2")
                .setContentText(info)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(100, progress, false)
                .setOngoing(true)
                .build();

        // PERUBAHAN DI SINI: Sisipkan tipe service untuk Android 14+ (API 34+) / Android 10+ (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new ForegroundInfo(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            );
        } else {
            return new ForegroundInfo(NOTIFICATION_ID, notification);
        }
    }
}
