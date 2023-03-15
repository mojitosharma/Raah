package com.example.raah;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
public class BluetoothService extends Service {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private final IBinder mBinder = new LocalBinder();
    private String mUUID;
    private String mMacAddress;

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        mUUID = intent.getStringExtra("UUID");
        mMacAddress = intent.getStringExtra("MAC_ADDRESS");
        return mBinder;
    }

    @SuppressLint("MissingPermission")
    public void connectBluetooth() {
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mMacAddress);

        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(mUUID));
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            mInputStream = mBluetoothSocket.getInputStream();
            mOutputStream = mBluetoothSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnectBluetooth() {
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String command) {
        try {
            mOutputStream.write(command.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readData() {
        byte[] buffer = new byte[1024];
        int bytes;

        try {
            bytes = mInputStream.read(buffer);
            return new String(buffer, 0, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
