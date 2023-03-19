package com.example.raah;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity{

    Toolbar toolbar;
    private static Context mContext;
    public static Handler handler;
    public static int returned_value;
    public static boolean ifFromFailedConnection=false;
    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
//    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private Button startButton, buttonConnect;
    IntentFilter intentFilter;
    String[] permissions= new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_ADMIN,Manifest.permission.BLUETOOTH};

    private BluetoothService mBluetoothService;
    private boolean mBound = false;

    private final ServiceConnection mConnection = new ServiceConnection() {
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

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED) || (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) &&(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF) == BluetoothAdapter.STATE_OFF))) {
                if(!Variables.isMainActivityRestarted){
                    Toast.makeText(MainActivity.this, "Bluetooth disconnected.", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    i.putExtra("failedConnection", true);
                    i.putExtra("ConnectionStatus",0);
                    Variables.deviceAddress=null;
                    Variables.deviceName=null;
                    finish();
                    startActivity(i);
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_main);
        Variables.isMainActivityRestarted=true;
        intentFilter= new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        // connecting to bound service
        Intent serviceIntent = new Intent(this, BluetoothService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);


        startButton = findViewById(R.id.startButton);
        startButton.setEnabled(false);
        buttonConnect = findViewById(R.id.buttonConnect);
        returned_value = getIntent().getIntExtra("ConnectionStatus",0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            if (checkPermission(permissions)) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }

        // UI Initialization
        toolbar = findViewById(R.id.toolbar);

        String deviceName = Variables.deviceName;
        ifFromFailedConnection=getIntent().getBooleanExtra("failedConnection",false);
        System.out.println("Working: "+ifFromFailedConnection);
        if (deviceName != null && !ifFromFailedConnection){
            Toast.makeText(this, deviceName, Toast.LENGTH_SHORT).show();
            // Show progress and connection status
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
            connectBluetoothDevice(bluetoothAdapter, Variables.deviceAddress);
        }else{
            buttonConnect.setEnabled(true);
            startButton.setEnabled(false);
            toolbar.setSubtitle("");
        }


        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == CONNECTING_STATUS) {
                    switch (msg.arg1) {
                        case 1:
                            toolbar.setSubtitle("Connected to " + Variables.deviceName);
                            Variables.isMainActivityRestarted=false;
                            buttonConnect.setEnabled(false);
                            break;
                        case -1:
                            toolbar.setSubtitle("Device fails to connect");
                            buttonConnect.setEnabled(true);
                            break;
                    }
                }
            }
        };

        if(returned_value == 1){
            handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            runOnUiThread(() -> buttonConnect.setEnabled(false));
            runOnUiThread(() -> startButton.setEnabled(true));
        }
        else if(returned_value == -1){
            handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
            runOnUiThread(() -> buttonConnect.setEnabled(true));
        }


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
                    if (checkPermission(permissions)) {
                        ActivityCompat.requestPermissions(this, permissions, 1);
                    }
                }
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });

        startButton.setOnClickListener(view -> {
            Intent intent = new Intent(this,GameScreen.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent serviceIntent = new Intent(this, BluetoothService.class);
//        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mReceiver);
    }

    @SuppressLint("MissingPermission")
    private void connectBluetoothDevice(BluetoothAdapter bluetoothAdapter, String address) {
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        UUID uuid = bluetoothDevice.getUuids()[0].getUuid();
        if(mBluetoothService == null){
            Toast.makeText(this, "null error", Toast.LENGTH_SHORT).show();
        }
        else {
            returned_value = mBluetoothService.connectBluetooth(uuid, address);
            if (returned_value == 1) {
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
                runOnUiThread(() -> startButton.setEnabled(true));
                runOnUiThread(() -> buttonConnect.setEnabled(true));
            } else {
                handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
            }
        }
    }

    private void disconnectBluetoothDevice() {
        mBluetoothService.disconnectBluetooth();
    }

    public boolean checkPermission(String[] permissions){
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
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
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}