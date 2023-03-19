package com.example.raah;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ShowScoreActivity extends AppCompatActivity {
    private IntentFilter intentFilter;
    private MediaPlayer mediaPlayer;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED) || (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) &&(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF) == BluetoothAdapter.STATE_OFF))) {
                if(!Variables.isMainActivityRestarted){
                    Toast.makeText(ShowScoreActivity.this, "Bluetooth disconnected. Please try again.", Toast.LENGTH_SHORT).show();
                    Intent i = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        i = new Intent(ShowScoreActivity.this, MainActivity.class);
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_score);
        intentFilter= new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, intentFilter);
        int totalAttempts = getIntent().getIntExtra("TotalAttempts", 0);
        int wrongAttempts = getIntent().getIntExtra("WrongAttempts", 0);
        TextView scoreValue = findViewById(R.id.scoreValue);
        Button goToHomeButton = findViewById(R.id.goToHomeButton);
        scoreValue.setText(String.valueOf(totalAttempts -(2* wrongAttempts)));
        goToHomeButton.setOnClickListener(this::onClick);
        mediaPlayer = MediaPlayer.create(this, R.raw.win);
        mediaPlayer.start();
    }
    public void onClick(View view){
        if (view.getId() == R.id.goToHomeButton) {
            Intent i = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                i = new Intent(ShowScoreActivity.this, MainActivity.class);
                i.putExtra("failedConnection", true); //This is just to make sure it does not reconnects
                i.putExtra("ConnectionStatus",1);
            }
            mediaPlayer.release();
            startActivity(i);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.release();
        unregisterReceiver(mReceiver);
    }
}