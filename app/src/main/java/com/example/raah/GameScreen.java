package com.example.raah;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

public class GameScreen extends AppCompatActivity {
//    public static ConnectedThread connectedThread;
//    public static Handler handler;
//    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
//    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private TextView currTextView, prevTextView,nextTextView;
    private BluetoothService mBluetoothService;
    private boolean mBound = false;
    private int curr,prev,next;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) iBinder;
            mBluetoothService = binder.getService();
            mBluetoothService.startReceive(GameScreen.this);
            mBluetoothService.sendData("1".getBytes());
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);
        currTextView= findViewById(R.id.currTextView);
        prevTextView =findViewById(R.id.prevTextView);
        nextTextView=findViewById(R.id.nextTextView);
        curr=0;
        prev=0;
        next=1;
        currTextView.setText(String.valueOf(curr));
        prevTextView.setText(String.valueOf(prev));
        nextTextView.setText(String.valueOf(next));
        // connecting to bound service
        Intent serviceIntent = new Intent(this, BluetoothService.class);
//        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothService.sendData("0".getBytes());
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }

    public void receivedData(String data){
        Log.i("ArduinoReceived",data);
        if(data.equals(String.valueOf(curr))){
            prev=curr;
            curr=next;
            next++;
            currTextView.setText(String.valueOf(curr));
            prevTextView.setText(String.valueOf(prev));
            nextTextView.setText(String.valueOf(next));
        }
    }

}

//        connectedThread = new ConnectedThread(mmSocket);
//        connectedThread.run();

         /*
        Second most important piece of Code. GUI Handler
         */
//        handler = new Handler(Looper.getMainLooper()) {
//            @Override
//            public void handleMessage(Message msg){
//                if (msg.what == MESSAGE_READ) {
//                    byte[] data = (byte[]) msg.obj;
//                    String receivedData = new String(data, 0, msg.arg1);
//                    try {
//                        float value = Float.parseFloat(receivedData.trim());
//                        mReceivedData.add(value);
//                        if (mReceivedData.size() == 10) {
//                            onDataReceived(mReceivedData);
//                            mReceivedData.clear();
//                        }
//                    } catch (NumberFormatException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };



// Button to ON/OFF LED on Arduino Board
//        buttonToggle.setOnClickListener(view -> {
//            String cmdText = null;
//            String btnState = buttonToggle.getText().toString().toLowerCase();
//            switch (btnState){
//                case "turn on":
//                    buttonToggle.setText(R.string.turn_off);
//                    // Command to turn on LED on Arduino. Must match with the command in Arduino code
//                    cmdText = "<turn on>";
//
//                    break;
//                case "turn off":
//                    buttonToggle.setText(R.string.turn_on);
//                    // Command to turn off LED on Arduino. Must match with the command in Arduino code
//                    cmdText = "<turn off>";
//                    break;
//            }
//            if(cmdText!=null){
//                cmdText = "2";
//                Log.i("SendFromGame",cmdText);
//                byte[] byteArray = cmdText.getBytes();
//                mBluetoothService.sendData(byteArray);
//            }else{
//                Toast.makeText(mContext, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
//            }
//            // Send command to Arduino board
////            connectedThread.write(cmdText);
//        });
//    private void onDataReceived(List<Float> data) {
//        StringBuilder sb = new StringBuilder();
//        for (float value : data) {
//            sb.append(value).append(" ");
//        }
//        final String receivedData = sb.toString().trim();
//        runOnUiThread(() -> textViewInfo.setText(receivedData));
//    }

/* =============================== Thread for Data Transfer =========================================== */
//    public static class ConnectedThread extends Thread {
//        private final BluetoothSocket mmSocket;
//        private final InputStream mmInStream;
//        private final OutputStream mmOutStream;
//
//        public ConnectedThread(BluetoothSocket socket) {
//            Toast.makeText(mContext , "In constructor 1", Toast.LENGTH_SHORT).show();
//            mmSocket = socket;
//            InputStream tmpIn = null;
//            OutputStream tmpOut = null;
//            Toast.makeText(mContext , "In constructor 2", Toast.LENGTH_SHORT).show();
//
//            // Get the input and output streams, using temp objects because
//            // member streams are final
//            try {
//                tmpIn = socket.getInputStream();
//                tmpOut = socket.getOutputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            mmInStream = tmpIn;
//            mmOutStream = tmpOut;
//            Toast.makeText(mContext , "In constructor end", Toast.LENGTH_SHORT).show();
//        }
//
//        public void run() {
//            Toast.makeText(mContext , "In run 1", Toast.LENGTH_SHORT).show();
//            byte[] buffer = new byte[1024];  // buffer store for the stream
//            int bytes = 0; // bytes returned from read()
//            // Keep listening to the InputStream until an exception occurs
//            while (true) {
//                try {
//                    /*
//                    Read from the InputStream from Arduino until termination character is reached.
//                    Then send the whole String message to GUI Handler.
//                     */
//                    if(mmInStream.available()  > 0){
//                        Log.i("mmInStream", "run: mmInStream 1");
//                        buffer[bytes] = (byte) mmInStream.read();
//                        Log.i("mmInStream", "run: mmInStream 2");
//                    }
//                    else{
//                        Log.i("mmInStream", "run: mmInStream");
//                        cancel();
//                    }
//
////                    String readMessage;
//                    if (buffer[bytes] == '\n'){
////                        readMessage = new String(buffer,0,bytes);
////                        Log.e("Arduino Message",readMessage);
//                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
//                        bytes = 0;
//                    } else {
//                        bytes++;
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    break;
//                }
//            }
//        }
//
//        /* Call this from the main activity to send data to the remote device */
//        public void write(String input) {
//
//            byte[] bytes = input.getBytes(); //converts entered String into bytes
//            try {
//                mmOutStream.write(bytes);
//                Toast.makeText(mContext , "In write", Toast.LENGTH_SHORT).show();
//            } catch (IOException e) {
//                Log.e("Send Error","Unable to send message",e);
//            }
//        }
//
//        /* Call this from the main activity to shutdown the connection */
//        public void cancel() {
//            try {
//                mmSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

