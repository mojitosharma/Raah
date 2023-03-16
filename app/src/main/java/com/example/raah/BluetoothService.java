package com.example.raah;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;
public class BluetoothService extends Service {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    BluetoothSocket tmp = null;
    private boolean mRunning;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private final IBinder mBinder = new LocalBinder();


    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @SuppressLint("MissingPermission")
    public int connectBluetooth(UUID uuid, String macAddress) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddress);

        try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
            tmp = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mBluetoothSocket = tmp;
        try {
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            Log.e("Status", "Device connected");
            mInputStream = mBluetoothSocket.getInputStream();
            mOutputStream = mBluetoothSocket.getOutputStream();
            return 1;
        } catch (IOException connectException) {
            connectException.printStackTrace();
            // Unable to connect; close the socket and return.
            try {
                mBluetoothSocket.close();
                Log.e("Status", "Cannot connect to device");
                return -1;
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
        }
        return 0;
    }

    public void disconnectBluetooth() {
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendData(final byte[] data) {
        // Run the send operation on a separate thread
        new Thread(() -> {
            try {
                Log.i("ServiceSending","aagya1: "+ Arrays.toString(data));
                mBluetoothSocket.getOutputStream().write(data);
                mBluetoothSocket.getOutputStream().flush();
                Log.i("ServiceSending","aagya 2");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    Activity activity;
    public void startReceive(Activity activity){
        ReceiveThread run = new ReceiveThread();
        this.activity = activity;
        run.start();
        Log.i("ReceiveThread","Started");
    }


    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            // Run the receive operation in a loop
            mRunning = true;
            while (mRunning) {
                byte[] buffer = new byte[1024];
                int numBytes;
                try {
                    numBytes = mInputStream.read(buffer);
                    String data = new String(buffer, 0, numBytes);
                    ((GameScreen)activity).receivedData(data);
                    // Process the received data
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }





//    public void sendCommand(String command) {
//        try {
//            mOutputStream.write(command.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public String readData() {
//        byte[] buffer = new byte[1024];
//        int bytes;
//
//        try {
//            bytes = mInputStream.read(buffer);
//            return new String(buffer, 0, bytes);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        try {
//            mBluetoothSocket.close();
//        } catch (IOException e) {
//            Log.e(TAG, "Could not close the client socket", e);
//        }
//    }
}
