package com.example.raah;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SelectGameActivity extends AppCompatActivity implements View.OnClickListener {
    String gameName="";
    TextView game1TextView,game2TextView,game3TextView;
    Button selectGameBackButton,selectGameNextButton;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED) || (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) &&(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF) == BluetoothAdapter.STATE_OFF))) {
                if(!Variables.isMainActivityRestarted){
                    Toast.makeText(SelectGameActivity.this, "Bluetooth disconnected. Please try again.", Toast.LENGTH_SHORT).show();
                    Intent i = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        i = new Intent(SelectGameActivity.this, MainActivity.class);
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
    IntentFilter intentFilter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent i = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    i = new Intent(SelectGameActivity.this, MainActivity.class);
                    i.putExtra("failedConnection", true); //This is just to make sure it does not reconnects
                    i.putExtra("ConnectionStatus",1);
                }
                finish();
                startActivity(i);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
        setContentView(R.layout.activity_select_game);
        game1TextView=findViewById(R.id.game1TextView);
        game2TextView=findViewById(R.id.game2TextView);
        game3TextView=findViewById(R.id.game3TextView);
        selectGameBackButton=findViewById(R.id.selectGameBackButton);
        selectGameNextButton=findViewById(R.id.selectGameNextButton);
        selectGameNextButton.setEnabled(false);
        intentFilter= new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, intentFilter);
        game1TextView.setOnClickListener(this);
        game2TextView.setOnClickListener(this);
        game3TextView.setOnClickListener(this);
        selectGameNextButton.setOnClickListener(this);
        selectGameBackButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id==R.id.game1TextView){
            selectGameNextButton.setEnabled(true);
            game1TextView.setBackgroundColor(Color.YELLOW);
            game2TextView.setBackgroundColor(Color.WHITE);
            game3TextView.setBackgroundColor(Color.WHITE);
            gameName="game1";
        }else if(id==R.id.game2TextView){
            selectGameNextButton.setEnabled(true);
            game1TextView.setBackgroundColor(Color.WHITE);
            game2TextView.setBackgroundColor(Color.YELLOW);
            game3TextView.setBackgroundColor(Color.WHITE);
            gameName="game2";
        }else if (id ==R.id.game3TextView){
            selectGameNextButton.setEnabled(true);
            game1TextView.setBackgroundColor(Color.WHITE);
            game2TextView.setBackgroundColor(Color.WHITE);
            game3TextView.setBackgroundColor(Color.YELLOW);
            gameName="game3";
        }else if(id==R.id.selectGameBackButton){
            onBackPressed();
        }else if (id==R.id.selectGameNextButton){
            Intent intent = new Intent(SelectGameActivity.this,SelectPlayerActivity.class);
            Toast.makeText(this, gameName, Toast.LENGTH_SHORT).show();
            intent.putExtra("gameName",gameName);
            startActivity(intent);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}