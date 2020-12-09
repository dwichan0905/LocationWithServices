package org.intermediaamikom.locationwithservices.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.intermediaamikom.locationwithservices.R;
import org.intermediaamikom.locationwithservices.remote.ApiConfig;
import org.intermediaamikom.locationwithservices.remote.entity.LocationEntity;
import org.intermediaamikom.locationwithservices.ui.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class SendLocationService extends Service {
    private static final String CHANNEL_ID = "CHANNEL_01";
    private static final int SERVICE_LOCATION_REQUEST_CODE = 12;
    private static final int LOCATION_SERVICE_NOTIFY_ID = 13;
    private final String TAG = SendLocationService.class.getSimpleName();
    private final MyBinder mBinder = new MyBinder();
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location currentLocation = locationResult.getLastLocation();
            Log.d("Locations", currentLocation.getLatitude() + "," + currentLocation.getLongitude());

            //Share/Publish Location
            Call<LocationEntity> call = ApiConfig.getApiService(SendLocationService.this)
                    .sendLocation(
                            String.valueOf(currentLocation.getLatitude()),
                            String.valueOf(currentLocation.getLongitude())
                    );
            call.enqueue(new Callback<LocationEntity>() {
                @Override
                @EverythingIsNonNull
                public void onResponse(Call<LocationEntity> call, Response<LocationEntity> response) {
                    Log.d(TAG, "API: " + currentLocation.getLatitude() + "," + currentLocation.getLongitude());
                }

                @Override
                @EverythingIsNonNull
                public void onFailure(Call<LocationEntity> call, Throwable t) {
                    Log.e(TAG, "Failed to send API via Service.");
                }
            });
        }
    };
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Layanan " + TAG + " sedang dimuat!");
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        prepareForegroundNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: " + TAG + " berhasil dikaitkan!");
        startGetLocation();
        return (IBinder) mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: " + TAG + " berhasil dihentikan!");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: " + TAG + " sedang dilepas kaitannya!");
        fusedLocationClient.removeLocationUpdates(locationCallback);
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "onRebind: " + TAG + " berhasil dikaitkan ulang!");
    }

    private void startGetLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(this.locationRequest,
                this.locationCallback, Looper.getMainLooper());
    }

    private void prepareForegroundNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                SERVICE_LOCATION_REQUEST_CODE,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentTitle("Lokasi sedang dikirim!")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(LOCATION_SERVICE_NOTIFY_ID, notification);
    }

    public class MyBinder extends Binder {
        public SendLocationService getService() {
            return SendLocationService.this;
        }
    }
}