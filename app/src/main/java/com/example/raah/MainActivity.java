package com.example.raah;

import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.content.ContentValues.TAG;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress;
    private static Context mContext;public static Handler handler;
    public static BluetoothSocket mmSocket;
    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private static Button startButton;
    boolean isAllPermissionsAvailable=false;
    String[] permissions= new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.BLUETOOTH_SCAN};

    private BluetoothService mBluetoothService;
    private boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) iBinder;
            mBluetoothService = binder.getService();
            if(mBluetoothService == null){
                Log.i("onServiceConnected", "why null");
            }
            else{
                Log.i("onServiceConnected", "not null here");
            }
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_main);

        // connecting to binded service
        Intent serviceintent = new Intent(this, BluetoothService.class);
        startService(serviceintent);
        bindService(serviceintent, mConnection, Context.BIND_AUTO_CREATE);


        startButton = findViewById(R.id.startButton);
        startButton.setEnabled(false);
        final Button buttonConnect = findViewById(R.id.buttonConnect);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            if (!checkPermission(permissions)) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }

        // UI Initialization
        final Toolbar toolbar = findViewById(R.id.toolbar);

        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null){
            Toast.makeText(this, deviceName, Toast.LENGTH_SHORT).show();
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progree and connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress);
//            createConnectThread.start();
            connectBluetoothDevice(bluetoothAdapter, deviceAddress);
        }


        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == CONNECTING_STATUS) {
                    switch (msg.arg1) {
                        case 1:
                            toolbar.setSubtitle("Connected to " + deviceName);
                            buttonConnect.setEnabled(true);
                            break;
                        case -1:
                            toolbar.setSubtitle("Device fails to connect");
                            buttonConnect.setEnabled(true);
                            break;
                    }
                }
            }
        };


        // Select Bluetooth Device
        buttonConnect.setOnClickListener(view -> {
            // Move to adapter list
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                // Device does not support Bluetooth
                Toast.makeText(mContext, "Bluetooth not detected :(", Toast.LENGTH_SHORT).show();
            } else if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enabled :)
                Toast.makeText(mContext, "Bluetooth not enabled. Please enable bluetooth to proceed", Toast.LENGTH_SHORT).show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
                    if (!checkPermission(permissions)) {
                        ActivityCompat.requestPermissions(this, permissions, 1);
                    }
                }
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });

        startButton.setOnClickListener(view -> {
            Variables.deviceName=deviceName;
            Variables.deviceAddress=deviceAddress;
            Variables.mmsocket=mmSocket;
            Intent intent = new Intent(this,GameScreen.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }


    @SuppressLint("MissingPermission")
    private void connectBluetoothDevice(BluetoothAdapter bluetoothAdapter, String address) {
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        BluetoothSocket tmp = null;
        UUID uuid = bluetoothDevice.getUuids()[0].getUuid();
        if(mBluetoothService == null){
            Toast.makeText(this, "null error", Toast.LENGTH_SHORT).show();

        }
        int returned_value = mBluetoothService.connectBluetooth(uuid, address);
        if(returned_value == 1){
            handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            runOnUiThread((Runnable) () -> startButton.setEnabled(true));
        }
        else{
            handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
        }
    }

    private void disconnectBluetoothDevice() {
        mBluetoothService.disconnectBluetooth();
    }

    public boolean checkPermission(String[] permissions){
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        disconnectBluetoothDevice();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                Toast.makeText(this, "Please allow the Permission", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    };
}