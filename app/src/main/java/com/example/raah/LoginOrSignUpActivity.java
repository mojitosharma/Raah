package com.example.raah;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

public class LoginOrSignUpActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    Button signUpButtonIntro,logInButtonIntro;
    ImageView logoImageViewIntro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_sign_up);
        mAuth=FirebaseAuth.getInstance();
        Intent intent = new Intent(LoginOrSignUpActivity.this,LoginActivity.class);
        if(mAuth.getCurrentUser()!=null){
            intent.putExtra("loginOrSignUp","loggedIn");
            startActivity(intent);
        }
        logoImageViewIntro = findViewById(R.id.logoImageViewIntro);
        logInButtonIntro = findViewById(R.id.logInButtonIntro);
        signUpButtonIntro = findViewById(R.id.signUpButtonIntro);
        logInButtonIntro.setOnClickListener(view -> {
            intent.putExtra("loginOrSignUp","login");
            finish();
            startActivity(intent);
        });
        signUpButtonIntro.setOnClickListener(view -> {
            intent.putExtra("loginOrSignUp","signup");
            finish();
            startActivity(intent);
        });
    }
}