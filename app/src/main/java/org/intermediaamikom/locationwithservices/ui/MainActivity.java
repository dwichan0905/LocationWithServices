package org.intermediaamikom.locationwithservices.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.intermediaamikom.locationwithservices.R;
import org.intermediaamikom.locationwithservices.databinding.ActivityMainBinding;
import org.intermediaamikom.locationwithservices.service.SendLocationService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_LOCATION = 10;
    // buat mengurangi pemakaian findViewById() yang bisa menyebabkan NPE
    private ActivityMainBinding binding;
    private boolean isBind = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SendLocationService.MyBinder binder = (SendLocationService.MyBinder) service;
            binder.getService();
            isBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnStartService.setOnClickListener(this);
        binding.btnGetPermission.setOnClickListener(this);
        binding.btnStopService.setOnClickListener(this);
        binding.btnStartService.setVisibility(View.GONE);
        binding.btnStopService.setVisibility(View.GONE);

        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            binding.btnStartService.setVisibility(View.VISIBLE);
            binding.btnStopService.setVisibility(View.VISIBLE);
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            Toast.makeText(this, "Memerlukan izin akses!", Toast.LENGTH_SHORT).show();
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    @Override
    protected void onDestroy() {
        // Mencegah Service is not registered, harus dihancurkan dulu service nya saat activity di destroy
        if (isBind) unbindService(connection);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start_service) {
            // jalankan service
            Intent i = new Intent(this, SendLocationService.class);
            bindService(i, connection, BIND_AUTO_CREATE);
        } else if (v.getId() == R.id.btn_stop_service) {
            // akhiri service
            if (isBind) unbindService(connection);
        } else if (v.getId() == R.id.btn_get_permission) {
            requestLocationPermission();
        }
    }
}