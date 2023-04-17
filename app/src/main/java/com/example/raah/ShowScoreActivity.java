package com.example.raah;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ShowScoreActivity extends AppCompatActivity {
    private IntentFilter intentFilter;
    double scoreDouble;
    View progressOverlay;
    String gameName,dateAndTime,username;
    TextView scoreValue;
    AlphaAnimation inAnimation;
    Button goToHomeButton;
    boolean scoreSaved=false;
    Button playAgainButton,saveScoreButton;
    ImageView animatedGif;
    AlphaAnimation outAnimation;
    private FirebaseUser user;
    private int totalAttempts=0;
    private int correctAttempts=0;
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
    public boolean isInternetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onClick(goToHomeButton);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
        setContentView(R.layout.activity_show_score);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user==null){
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            Intent gotToLoginPage = new Intent(ShowScoreActivity.this, LoginOrSignUpActivity.class);
            startActivity(gotToLoginPage);
            finishAffinity();
            finish();
            return;
        }
        intentFilter= new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, intentFilter);
        initialize();
    }
    @SuppressLint("SimpleDateFormat")
    public void initialize(){
        totalAttempts = getIntent().getIntExtra("TotalAttempts", 0);
        correctAttempts = getIntent().getIntExtra("CorrectAttempts", 0);
        gameName = getIntent().getStringExtra("gameName");
        username = getIntent().getStringExtra("username");
        dateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        scoreDouble=  correctAttempts-(0.5*(totalAttempts-correctAttempts));
        if(scoreDouble<0){
            scoreDouble=0;
        }
        scoreValue = findViewById(R.id.scoreValue);
        goToHomeButton = findViewById(R.id.goToHomeButton);
        playAgainButton =findViewById(R.id.playAgainButton);
        progressOverlay =findViewById(R.id.progress_overlay);
        saveScoreButton=findViewById(R.id.saveScoreButton);
        animatedGif=findViewById(R.id.animatedGif);
        animatedGif.setVisibility(View.INVISIBLE);
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        goToHomeButton.setOnClickListener(this::onClick);
        playAgainButton.setOnClickListener(this::onClick);
        saveScoreButton.setOnClickListener(this::onClick);
        saveScoreButton.setEnabled(false);
        saveScoreButton.setVisibility(View.INVISIBLE);
        mediaPlayer = MediaPlayer.create(this, R.raw.win);
        scoreValue.setText(String.valueOf(scoreDouble));
        mediaPlayer.start();
        animatedGif.setVisibility(View.VISIBLE);
        if(isInternetConnected(this)){
            saveScore();
        }else{
            Toast.makeText(this, "Score could not be saved. Please check your connection.", Toast.LENGTH_SHORT).show();
            saveScoreButton.setEnabled(true);
            saveScoreButton.setVisibility(View.VISIBLE);
            progressOverlay.setAnimation(outAnimation);
            progressOverlay.setVisibility(View.GONE);
        }
    }
    public void saveScore(){
        progressOverlay.setAnimation(inAnimation);
        progressOverlay.setVisibility(View.VISIBLE);
        saveScoreButton.setEnabled(false);
        saveScoreButton.setVisibility(View.INVISIBLE);
        Score score = new Score(gameName, dateAndTime,totalAttempts,correctAttempts);
        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("teachers").child(userId);
        Query query = userRef.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot snapshot1: snapshot.getChildren()){
                        snapshot1.getRef().child("Scores").push().setValue(score).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                Toast.makeText(ShowScoreActivity.this, "Score Saved", Toast.LENGTH_SHORT).show();
                                saveScoreButton.setEnabled(false);
                                saveScoreButton.setVisibility(View.INVISIBLE);
                                progressOverlay.setAnimation(outAnimation);
                                progressOverlay.setVisibility(View.GONE);
                                scoreSaved=true;
//                                onClick(goToHomeButton);
                            }else{
                                Toast.makeText(ShowScoreActivity.this, "Score not saved", Toast.LENGTH_SHORT).show();
                                saveScoreButton.setEnabled(true);
                                saveScoreButton.setVisibility(View.VISIBLE);
                                progressOverlay.setAnimation(outAnimation);
                                progressOverlay.setVisibility(View.GONE);
                            }
                        });
                        break;
                    }
                }else{
                    Log.i("Snapshot","not found");
                    saveScoreButton.setEnabled(true);
                    saveScoreButton.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("Database Error",error.toString());
                progressOverlay.setAnimation(outAnimation);
                progressOverlay.setVisibility(View.GONE);
                saveScoreButton.setEnabled(true);
                saveScoreButton.setVisibility(View.VISIBLE);
            }
        });
    }
    public void onClick(View view){
        if (view.getId() == R.id.goToHomeButton) {
            Intent i = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                i = new Intent(ShowScoreActivity.this, MainActivity.class);
                i.putExtra("failedConnection", true); //This is just to make sure it does not reconnects
                i.putExtra("ConnectionStatus",1);
            }
//            mediaPlayer.release();
            startActivity(i);
        }else if(view.getId()==R.id.playAgainButton){
            Intent i = new Intent(ShowScoreActivity.this, SelectGameActivity.class);
//            mediaPlayer.release();
            startActivity(i);
        }else if(view.getId()==R.id.saveScoreButton){
            progressOverlay.setAnimation(inAnimation);
            progressOverlay.setVisibility(View.VISIBLE);
            if (isInternetConnected(this)){
                saveScore();
            }else{
                Toast.makeText(this, "Check your connection first.", Toast.LENGTH_SHORT).show();
                progressOverlay.setAnimation(outAnimation);
                progressOverlay.setVisibility(View.INVISIBLE);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
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
        finishAffinity();
        finish();
    }
}