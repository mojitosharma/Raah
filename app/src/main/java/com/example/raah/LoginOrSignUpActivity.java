package com.example.raah;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginOrSignUpActivity extends AppCompatActivity {
    private  FirebaseAuth mAuth;
    Button signUpButtonIntro,logInButtonIntro;
    ImageView logoImageViewIntro;
    public boolean isInternetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_sign_up);
        mAuth=FirebaseAuth.getInstance();
        Intent intent = new Intent(LoginOrSignUpActivity.this,LoginActivity.class);
        if(mAuth.getCurrentUser()!=null){
            intent.putExtra("loginOrSignUp","loggedIn");
            finish();
            startActivity(intent);
        }
        if(!isInternetConnected(this)){
            Toast.makeText(this, "Please connect to internet.", Toast.LENGTH_SHORT).show();
            return;
        }
        logoImageViewIntro = findViewById(R.id.logoImageViewIntro);
        logInButtonIntro = findViewById(R.id.logInButtonIntro);
        signUpButtonIntro = findViewById(R.id.signUpButtonIntro);
        logoImageViewIntro.setImageResource(R.drawable.raah);
        logInButtonIntro.setOnClickListener(view -> {
            if(isInternetConnected(this)){
                intent.putExtra("loginOrSignUp", "login");
                startActivity(intent);
            }else{
                Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
            }
        });
        signUpButtonIntro.setOnClickListener(view -> {
            if(isInternetConnected(this)){
                intent.putExtra("loginOrSignUp", "signup");
                startActivity(intent);
            }else{
                Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}