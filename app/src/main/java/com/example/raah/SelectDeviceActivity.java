package com.example.raah;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SelectDeviceActivity extends AppCompatActivity {
    List<Object> deviceList = new ArrayList<>();
    ArrayList<String> addresses= new ArrayList<>();
    int pos=-1;
    String deviceName="";
    String deviceAddress="";
    boolean isDiscovering=false;
    DeviceListAdapter deviceListAdapter;
    Button selectDeviceBackButton,selectDeviceStartButton;
    IntentFilter deviceFoundFilter,pairedRequestFilter,bondFilter;
    ArrayList<BluetoothDevice> devices;
    ArrayList<String> pairedDevices= new ArrayList<>();
    BluetoothAdapter bluetoothAdapter;
    TextView scanTextView;
    private final BroadcastReceiver pairedRequestReceiver= new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();
            Log.i("Action",action);
            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(intent.getAction())) {
                abortBroadcast();
                Log.i("Tag","Pairing Request Is Here");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int pairingVariant = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
                if (pairingVariant == BluetoothDevice.PAIRING_VARIANT_PIN) {
                    final EditText input = new EditText(context);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Pairing Request")
                            .setMessage("Please enter the PIN for " + device.getName())
                            .setView(input)
                            .setPositiveButton("Pair", (dialog, which) -> {
                                // Retrieve the PIN entered by the user
                                String pin = input.getText().toString();

                                // Initiate the pairing process with the entered PIN
                                device.setPin(pin.getBytes());
                                device.createBond();

                                // To prevent the automatic redirection to MainActivity,
                                // you can abort the broadcast to prevent the default pairing process
                                abortBroadcast();
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                // Cancel the pairing process

                                // To prevent the automatic redirection to MainActivity,
                                // you can abort the broadcast to prevent the default pairing process
                                abortBroadcast();
                            })
                            .create()
                            .show();
                }
            }
        }
    };
    private final BroadcastReceiver bondReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();
            Log.i("Action",action);
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice bluetoothDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Intent i = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        i = new Intent(SelectDeviceActivity.this, MainActivity.class);
                    }
                    // Send device details to the MainActivity
                    pairedDevices.add(bluetoothDevice.getAddress());
                    Log.i("Tag","Bonded");
                    Variables.deviceName = bluetoothDevice.getName();
                    Variables.deviceAddress = bluetoothDevice.getAddress();
                    Toast.makeText(context, "Paired to "+bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
                    // Call MainActivity
                    context.startActivity(i);
                }else if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.i("Tag","Bonding");
                }else if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.i("Tag","Not Bonded");
                }
            }
        }
    };
    private final BroadcastReceiver deviceFoundReceiver= new BroadcastReceiver() {
        @SuppressLint({"SetTextI18n", "MissingPermission"})
        @Override
        public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();
            Log.i("Action",action);
            if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                if(isDiscovering){
                    Toast.makeText(context, "Finished searching", Toast.LENGTH_SHORT).show();
                    scanTextView.setEnabled(true);
                    scanTextView.setTextColor(Color.WHITE);
                    isDiscovering=false;
                }
            }else if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name= device.getName();
                String address= device.getAddress();
                String rssi= Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE));
                if(!addresses.contains(address)){
                    DeviceInfoModel deviceInfoModel;
                    if(name==null || name.equals("")){
                        deviceInfoModel = new DeviceInfoModel("Unknown Device "+ rssi, address);
                    }
                    else{
                        deviceInfoModel = new DeviceInfoModel(name,address);
                    }
                    devices.add(device);
                    addresses.add(address);
                    deviceList.add(deviceInfoModel);
                    deviceListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
        setContentView(R.layout.activity_select_device);
            // Display paired device using recyclerView
        scanTextView= findViewById(R.id.scanTextView);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewDevice);
        selectDeviceStartButton=findViewById(R.id.selectDeviceStartButton);
        selectDeviceStartButton.setEnabled(false);
        selectDeviceBackButton = findViewById(R.id.selectDeviceBackButton);
        selectDeviceBackButton.setOnClickListener(view->onBackPressed());
        selectDeviceStartButton.setOnClickListener(view -> {
            BluetoothDevice myDevice = devices.get(pos);
            if(!deviceName.equals("") && !deviceAddress.equals("")){
                if(!pairedDevices.contains(deviceAddress)){
                    myDevice.createBond();
                }else{
                    Intent i = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        i = new Intent(SelectDeviceActivity.this, MainActivity.class);
                    }
                    // Send device details to the MainActivity
                    Variables.deviceName = myDevice.getName();
                    Variables.deviceAddress = myDevice.getAddress();
                    Toast.makeText(this, "Paired to "+myDevice.getName(), Toast.LENGTH_SHORT).show();
                    // Call MainActivity
                    startActivity(i);
                }
            }
        });
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devices = new ArrayList<>(bluetoothAdapter.getBondedDevices());
        if (devices.size() > 0) {
            for (BluetoothDevice device : devices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                addresses.add(deviceHardwareAddress);
                DeviceInfoModel deviceInfoModel = new DeviceInfoModel(deviceName,deviceHardwareAddress);
                deviceList.add(deviceInfoModel);
                pairedDevices.add(deviceHardwareAddress);
            }
        }
        deviceFoundFilter= new IntentFilter();
        deviceFoundFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        deviceFoundFilter.addAction(BluetoothDevice.ACTION_FOUND);
        deviceFoundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        deviceFoundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(deviceFoundReceiver,deviceFoundFilter);
        pairedRequestFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(pairedRequestReceiver, pairedRequestFilter);
        bondFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bondReceiver,bondFilter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceListAdapter = new DeviceListAdapter(this,deviceList);
        recyclerView.setAdapter(deviceListAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        scanTextView.setOnClickListener(view -> {
            Toast.makeText(this, "Searching. Please wait!", Toast.LENGTH_SHORT).show();
            addresses.clear();
            deviceList.clear();
            devices.clear();
            pairedDevices.clear();
            devices = new ArrayList<>(bluetoothAdapter.getBondedDevices());
            if (devices.size() > 0) {
                for (BluetoothDevice device : devices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress();
                    addresses.add(deviceHardwareAddress);
                    DeviceInfoModel deviceInfoModel = new DeviceInfoModel(deviceName,deviceHardwareAddress);
                    deviceList.add(deviceInfoModel);
                    pairedDevices.add(deviceHardwareAddress);
                }
            }
            deviceListAdapter.notifyDataSetChanged();
            scanTextView.setEnabled(false);
            scanTextView.setTextColor(Color.GRAY);
            bluetoothAdapter.startDiscovery();
            isDiscovering=true;
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(deviceFoundReceiver);
        unregisterReceiver(pairedRequestReceiver);
        unregisterReceiver(bondReceiver);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(deviceFoundReceiver,deviceFoundFilter);
        registerReceiver(pairedRequestReceiver, pairedRequestFilter);
        registerReceiver(bondReceiver,bondFilter);
    }
}