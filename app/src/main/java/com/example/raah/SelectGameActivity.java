package com.example.raah;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SelectGameActivity extends AppCompatActivity implements View.OnClickListener {
    TextView game1TextView,game2TextView,game3TextView;
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
        setContentView(R.layout.activity_select_game);
        game1TextView=findViewById(R.id.game1TextView);
        game2TextView=findViewById(R.id.game2TextView);
        game3TextView=findViewById(R.id.game3TextView);
        intentFilter= new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, intentFilter);
        game1TextView.setOnClickListener(this);
        game2TextView.setOnClickListener(this);
        game3TextView.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent = new Intent(SelectGameActivity.this,SelectPlayerActivity.class);
        if(id==R.id.game1TextView){
            intent.putExtra("gameName","game1");
            startActivity(intent);
        }else if(id==R.id.game2TextView){
            intent.putExtra("gameName","game2");
            startActivity(intent);
        }else if (id ==R.id.game3TextView){
            intent.putExtra("gameName","game3");
            startActivity(intent);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        unregisterReceiver(mReceiver);
    }
}