package com.example.raah;


import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class GameScreen extends AppCompatActivity{
    private TextView currTextView, prevTextView,nextTextView;
    private LinearLayout aboveLinearLayout;
    private BluetoothService mBluetoothService;
    private boolean mBound = false;
    private MediaPlayer mediaPlayerCorrect,mediaPlayerWrong;
    private int curr,prev,next,totalAttempts,wrongAttempts,diff;
    final int startColor = Color.WHITE;
    final int endColor1 = Color.RED;
    final int endColor2 = Color.GREEN;
    String gameName="";
    String username="";
    private ValueAnimator valueAnimator1,valueAnimator2;
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
    IntentFilter intentFilter;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED) || (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) &&(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF) == BluetoothAdapter.STATE_OFF))) {
                if(!Variables.isMainActivityRestarted){
                    Toast.makeText(GameScreen.this, "Bluetooth disconnected. Please try again.", Toast.LENGTH_SHORT).show();
                    Intent i = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        i = new Intent(GameScreen.this, MainActivity.class);
                        finish();
                        i.putExtra("failedConnection", true);
                        i.putExtra("ConnectionStatus",0);
                        Variables.deviceAddress=null;
                        Variables.deviceName=null;
                    }
                    startActivity(i);
                }
            }
        }
    };

    public void initialize(){
        currTextView= findViewById(R.id.currTextView);
        prevTextView =findViewById(R.id.prevTextView);
        nextTextView=findViewById(R.id.nextTextView);
        aboveLinearLayout=findViewById(R.id.aboveLinearLayout);
        if(Objects.equals(gameName, "game1")){
            diff=1;
            curr=0;
            prev=-1;
            next=1;
        }else if(Objects.equals(gameName, "game2")){
            diff=2;
            curr = 0;
            prev = -2;
            next = 2;
        } else if (Objects.equals(gameName, "game3")) {
            diff=2;
            curr = 1;
            prev = -1;
            next = 3;
        }
        totalAttempts=0;
        wrongAttempts=0;
        currTextView.setText(String.valueOf(curr));
        prevTextView.setText("");
        nextTextView.setText(String.valueOf(next));
    }

    @SuppressLint({"MissingPermission", "SetTextI18n"})
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
        setContentView(R.layout.activity_game_screen);
        mediaPlayerWrong = MediaPlayer.create(this, R.raw.wrong);
        mediaPlayerCorrect = MediaPlayer.create(this, R.raw.correct);
        intentFilter= new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        gameName = getIntent().getStringExtra("gameName");
        username = getIntent().getStringExtra("username");
        initialize();
        valueAnimator1 = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor1);
        valueAnimator1.setDuration(100);
        valueAnimator1.addUpdateListener(animator -> aboveLinearLayout.setBackgroundColor((int) animator.getAnimatedValue()));

        valueAnimator1.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                final ValueAnimator reverseColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), endColor1, startColor);
                reverseColorAnimation.setDuration(400);
                reverseColorAnimation.addUpdateListener(animator -> aboveLinearLayout.setBackgroundColor((int) animator.getAnimatedValue()));
                reverseColorAnimation.start();
            }
        });
        valueAnimator2 = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor2);
        valueAnimator2.setDuration(100);
        valueAnimator2.addUpdateListener(animator -> aboveLinearLayout.setBackgroundColor((int) animator.getAnimatedValue()));
        valueAnimator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                final ValueAnimator reverseColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), endColor2, startColor);
                reverseColorAnimation.setDuration(400);
                reverseColorAnimation.addUpdateListener(animator -> aboveLinearLayout.setBackgroundColor((int) animator.getAnimatedValue()));
                reverseColorAnimation.start();
            }
        });
        // connecting to bound service
        Intent serviceIntent = new Intent(this, BluetoothService.class);
//        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayerCorrect.release();
        mediaPlayerWrong.release();
        mBluetoothService.sendData("0".getBytes());
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }

    @SuppressLint("SetTextI18n")
    public void receivedData(String data){
        Log.i("ArduinoReceived",data);
        if(data.equals(String.valueOf(curr))){
            totalAttempts++;
            prev=curr;
            curr=next;
            valueAnimator2.start();
            if(curr==9 || (curr==8 && gameName.equals("game2"))){
                next=Integer.MAX_VALUE;
                nextTextView.setText("Over");
                currTextView.setText(String.valueOf(curr));
                prevTextView.setText(String.valueOf(prev));
                mediaPlayerCorrect.start();
            }else if(curr<9){
                next+=diff;
                nextTextView.setText(String.valueOf(next));
                currTextView.setText(String.valueOf(curr));
                prevTextView.setText(String.valueOf(prev));
                mediaPlayerCorrect.start();
            }else{
                mBluetoothService.sendData("0".getBytes());
                mediaPlayerCorrect.release();
                mediaPlayerWrong.release();
                Intent i = new Intent(GameScreen.this,ShowScoreActivity.class);
                i.putExtra("TotalAttempts",totalAttempts);
                i.putExtra("CorrectAttempts",totalAttempts-wrongAttempts);
                i.putExtra("username",username);
                i.putExtra("gameName",gameName);
                mBluetoothService.stopReceive();
                finish();
                startActivity(i);
            }
        }else if(isNumeric(data)){
            if(!data.equals(String.valueOf(prev))){
                totalAttempts++;
                wrongAttempts++;
                mediaPlayerWrong.start();
                valueAnimator1.start();
            }
        }
    }
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayerCorrect.release();
        mediaPlayerWrong.release();
        unregisterReceiver(mReceiver);
        finish();
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

