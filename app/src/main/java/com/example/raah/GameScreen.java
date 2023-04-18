package com.example.raah;


import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class GameScreen extends AppCompatActivity implements View.OnClickListener {
    private TextView currTextView, prevTextView,nextTextView,gameNameTextView;
    Button gameScreenNextButton,gameScreenResetButton,passButton;
    private ConstraintLayout constrainLayoutGameScreen;
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
        constrainLayoutGameScreen = findViewById(R.id.constrainLayoutGameScreen);
        gameScreenResetButton= findViewById(R.id.gameScreenResetButton);
        gameScreenNextButton = findViewById(R.id.gameScreenNextButton);
        gameNameTextView = findViewById(R.id.gameNameTextView);
        passButton = findViewById(R.id.passButton);
        gameNameTextView.setText(gameName);
        passButton.setOnClickListener(this);
        gameScreenResetButton.setOnClickListener(this);
        gameScreenNextButton.setOnClickListener(this);
        onClick(gameScreenResetButton);
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
//        mediaPlayerWrong = MediaPlayer.create(this, R.raw.wrong);
        mediaPlayerCorrect = MediaPlayer.create(this, R.raw.correct);
        intentFilter= new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        gameName = getIntent().getStringExtra("gameName");
        username = getIntent().getStringExtra("username");
        initialize();
        valueAnimator1 = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor1);
        valueAnimator1.setDuration(100);
        valueAnimator1.addUpdateListener(animator -> constrainLayoutGameScreen.setBackgroundColor((int) animator.getAnimatedValue()));

        valueAnimator1.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                final ValueAnimator reverseColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), endColor1, startColor);
                reverseColorAnimation.setDuration(400);
                reverseColorAnimation.addUpdateListener(animator -> constrainLayoutGameScreen.setBackgroundColor((int) animator.getAnimatedValue()));
                reverseColorAnimation.start();
            }
        });
        valueAnimator2 = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor2);
        valueAnimator2.setDuration(100);
        valueAnimator2.addUpdateListener(animator -> constrainLayoutGameScreen.setBackgroundColor((int) animator.getAnimatedValue()));
        valueAnimator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                final ValueAnimator reverseColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), endColor2, startColor);
                reverseColorAnimation.setDuration(400);
                reverseColorAnimation.addUpdateListener(animator -> constrainLayoutGameScreen.setBackgroundColor((int) animator.getAnimatedValue()));
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
//        mediaPlayerWrong.release();
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
//                mediaPlayerCorrect.start();
            }else if(curr<9){
                next+=diff;
                nextTextView.setText(String.valueOf(next));
                currTextView.setText(String.valueOf(curr));
                prevTextView.setText(String.valueOf(prev));
                mediaPlayerCorrect.start();
            }else{
                mBluetoothService.sendData("0".getBytes());
                mediaPlayerCorrect.release();
//                mediaPlayerWrong.release();
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
//                mediaPlayerWrong.start();
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
//        mediaPlayerWrong.release();
        unregisterReceiver(mReceiver);
        finish();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.gameScreenResetButton){
            if(Objects.equals(gameName, "Hop 0 to 9")){
                diff=1;
                curr=0;
                prev=-1;
                next=1;
            }else if(Objects.equals(gameName, "Hop Even Numbers")){
                diff=2;
                curr = 0;
                prev = -2;
                next = 2;
            } else if (Objects.equals(gameName, "Hop Odd Numbers")) {
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
        } else if (id == R.id.gameScreenNextButton) {
            mBluetoothService.sendData("0".getBytes());
            mediaPlayerCorrect.release();
//            mediaPlayerWrong.release();
            Intent i = new Intent(GameScreen.this,ShowScoreActivity.class);
            i.putExtra("TotalAttempts",totalAttempts);
            i.putExtra("CorrectAttempts",totalAttempts-wrongAttempts);
            i.putExtra("username",username);
            i.putExtra("gameName",gameName);
            mBluetoothService.stopReceive();
            finish();
            startActivity(i);
        }else if (view.getId()==R.id.passButton){
            receivedData(String.valueOf(curr));
        }
    }
}

