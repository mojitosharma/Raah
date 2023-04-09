package com.example.raah;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity{

    Toolbar toolbar;
    private FirebaseAuth mAuth;
    private static Context mContext;
    public static Handler handler;
    public static int returned_value;
    public static boolean ifFromFailedConnection=false;
    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
//    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private Button startButton, buttonConnect,playerListButton;
    FloatingActionButton addNewPlayerButton;
    IntentFilter intentFilter;
    String[] permissions= new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_ADMIN,Manifest.permission.BLUETOOTH};

    private static BluetoothService mBluetoothService;
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
    public void initialize(){
        buttonConnect = findViewById(R.id.buttonConnect);
        addNewPlayerButton = findViewById(R.id.addNewPlayerButton);
        startButton = findViewById(R.id.startButton);
        toolbar = findViewById(R.id.toolbar);
        playerListButton = findViewById(R.id.playerListButton);
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user==null){
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }
        // connecting to bound service
        initialize();
        setSupportActionBar(toolbar);
        Intent serviceIntent = new Intent(this, BluetoothService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        Variables.isMainActivityRestarted=true;
        intentFilter= new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        startButton.setEnabled(false);
        returned_value = getIntent().getIntExtra("ConnectionStatus",0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            if (checkPermission(permissions)) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }

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
            Log.i("testing123", String.valueOf(bluetoothAdapter==null));
//            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress);
//            createConnectThread.start();
            if(bluetoothAdapter!=null){
                connectBluetoothDevice(bluetoothAdapter, Variables.deviceAddress);
            }
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
            Intent intent = new Intent(this,SelectGameActivity.class);
            startActivity(intent);
        });
        playerListButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this,PlayerListActivity.class);
            startActivity(intent);
        });
        addNewPlayerButton.setOnClickListener(view -> {
            Intent addPlayerIntent = new Intent(MainActivity.this,AddPlayerActivity.class);
            startActivity(addPlayerIntent);
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
            Log.i("connectedBluetooth","null here");
            Toast.makeText(this, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show();
            finishAffinity();
            recreate();
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
    private boolean isMyBackgroundServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BluetoothService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if(isMyBackgroundServiceRunning()){
            disconnectBluetoothDevice();
        }
//        Intent a = new Intent(Intent.ACTION_MAIN);
//        a.addCategory(Intent.CATEGORY_HOME);
//        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(a);
        this.finishAffinity();
        finish();
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_logout && mAuth.getCurrentUser()!=null) {
            mAuth.signOut();
            Toast.makeText(mContext, "Logged out successfully", Toast.LENGTH_SHORT).show();
            finishAffinity();
            finish();
            startActivity(new Intent(this, LoginOrSignUpActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}