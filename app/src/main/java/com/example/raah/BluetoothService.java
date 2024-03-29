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
    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothDevice mBluetoothDevice;
    private static BluetoothSocket mBluetoothSocket;
    private static ReceiveThread run;
    private static UUID uuid;
    private boolean mRunning;
    private static InputStream mInputStream;
    private static OutputStream mOutputStream;
    private static int returnVar=0;
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
    public int connectBluetooth(UUID uuid1, String macAddress) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddress);
        uuid=uuid1;
        CreateConnectThread createConnectThread= new CreateConnectThread();
        createConnectThread.start();
        while(createConnectThread.isAlive()){}
        return returnVar;
    }

    public void disconnectBluetooth() {
        try {
            stopReceive();
            Variables.deviceAddress=null;
            Variables.deviceName=null;
            if(mBluetoothSocket!=null){
                mBluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendData(final byte[] data) {
        // Run the send operation on a separate thread
        new Thread(() -> {
            try {
                Log.i("ServiceSending","aagya1: "+ Arrays.toString(data));
                mOutputStream.write(data);
                mOutputStream.flush();
                Log.i("ServiceSending","aagya 2");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public Activity activity;
    public void startReceive(Activity activity){
        run = new ReceiveThread();
        this.activity = activity;
        run.start();
        Log.i("ReceiveThread","Started");
    }
    public void stopReceive(){
        if(mRunning){
            run.interrupt();
            run=null;
            mRunning=false;
        }
    }


    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            // Run the receive operation in a loop
            mRunning = true;
            byte[] buffer = new byte[1024];
            int numBytes;
            while (mRunning) {
                try {
                    numBytes = mInputStream.read(buffer);
                    String data = new String(buffer, 0, numBytes);
                    activity.runOnUiThread(() -> ((GameScreen)activity).receivedData(data));
                    // Process the received data
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
//                if (buffer[numBytes] == '\n'){
//                    Log.i("Buffer", Arrays.toString(buffer));
//                    numBytes = 0;
//                } else {
//                    Log.i("Buffer","Error: "+Arrays.toString(buffer));
//                    numBytes++;
//                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    public static class CreateConnectThread extends Thread {

        public CreateConnectThread() {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
//            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
//            UUID uuid = mBluetoothDevice.getUuids()[0].getUuid();
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
        }
        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
//            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mBluetoothSocket.connect();
                Log.i("Status", "Device connected");
            mInputStream = mBluetoothSocket.getInputStream();
            mOutputStream = mBluetoothSocket.getOutputStream();
                returnVar=1;
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    Variables.deviceAddress=null;
                    Variables.deviceName=null;
                    mBluetoothSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    returnVar=-1;
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            stopReceive();
            Variables.deviceAddress=null;
            Variables.deviceName=null;
            mBluetoothSocket.close();
//            unregisterReceiver(mBluetoothDisconnectReceiver);
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
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
    //        try {
//                /*
//                Get a BluetoothSocket to connect with the given BluetoothDevice.
//                Due to Android device varieties,the method below may not work fo different devices.
//                You should try using other methods i.e. :
//                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
//                 */
//            tmp = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
//
//        } catch (IOException e) {
//            Log.e(TAG, "Socket's create() method failed", e);
//        }
//        mBluetoothSocket = tmp;
//        try {
//            mBluetoothAdapter.cancelDiscovery();
//            mBluetoothSocket.connect();
//            Log.e("Status", "Device connected");
//            mInputStream = mBluetoothSocket.getInputStream();
//            mOutputStream = mBluetoothSocket.getOutputStream();
//            returnVar =1;
//        } catch (IOException connectException) {
//            connectException.printStackTrace();
//            // Unable to connect; close the socket and return.
//            try {
//                mBluetoothSocket.close();
//                Log.e("Status", "Cannot connect to device");
//                returnVar = 1;
//            } catch (IOException closeException) {
//                Log.e(TAG, "Could not close the client socket", closeException);
//            }
//        }
}
