package com.example.raah;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameScreen extends AppCompatActivity {
    public static ConnectedThread connectedThread;
    public static Handler handler;
    private static Context mContext;
    public static BluetoothSocket mmSocket;
    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private String deviceName = null;
    private String deviceAddress;
    private TextView textViewInfo;
    private List<Float> mReceivedData = new ArrayList<>();

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);
        mContext = getApplicationContext();
        textViewInfo = findViewById(R.id.textViewInfo);
        final Button buttonToggle = findViewById(R.id.buttonToggle);
        deviceAddress=Variables.deviceAddress;
        deviceName=Variables.deviceName;
        buttonToggle.setEnabled(false);
        mmSocket = Variables.mmsocket;
        connectedThread = new ConnectedThread(mmSocket);
        connectedThread.run();

         /*
        Second most important piece of Code. GUI Handler
         */
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                if (msg.what == MESSAGE_READ) {
                    byte[] data = (byte[]) msg.obj;
                    String receivedData = new String(data, 0, msg.arg1);
                    try {
                        float value = Float.parseFloat(receivedData.trim());
                        mReceivedData.add(value);
                        if (mReceivedData.size() == 10) {
                            onDataReceived(mReceivedData);
                            mReceivedData.clear();
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        };



        // Button to ON/OFF LED on Arduino Board
        buttonToggle.setOnClickListener(view -> {
            String cmdText = null;
            String btnState = buttonToggle.getText().toString().toLowerCase();
            switch (btnState){
                case "turn on":
                    buttonToggle.setText("Turn Off");
                    // Command to turn on LED on Arduino. Must match with the command in Arduino code
                    cmdText = "<turn on>";
                    break;
                case "turn off":
                    buttonToggle.setText("Turn On");
                    // Command to turn off LED on Arduino. Must match with the command in Arduino code
                    cmdText = "<turn off>";
                    break;
            }
            // Send command to Arduino board
            connectedThread.write(cmdText);
        });
    }
    private void onDataReceived(List<Float> data) {
        StringBuilder sb = new StringBuilder();
        for (float value : data) {
            sb.append(value).append(" ");
        }
        final String receivedData = sb.toString().trim();
        runOnUiThread(() -> textViewInfo.setText(receivedData));
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
//                    String readMessage;
                    if (buffer[bytes] == '\n'){
//                        readMessage = new String(buffer,0,bytes);
//                        Log.e("Arduino Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {

            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
                Toast.makeText(mContext , "In write", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}

