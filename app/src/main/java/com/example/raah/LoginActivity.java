package com.example.raah;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private  FirebaseAuth mAuth;
    View progressOverlay;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    private String type="";
    private String email="";
    private String pw="";
    Button submitButtonSignIn;
    EditText emailEditText, passwordEditText;
    TextView headingLogInTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
        mAuth = FirebaseAuth.getInstance();
        type = getIntent().getStringExtra("loginOrSignUp");
        if(type.equals("")){
            Toast.makeText(this, "Some error Occurred. Please try again", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        headingLogInTextView = findViewById(R.id.headingLogInTextView);
        submitButtonSignIn = findViewById(R.id.submitButtonSignIn);
        emailEditText=findViewById(R.id.editTextTextEmailAddress);
        passwordEditText=findViewById(R.id.editTextTextPassword);
        progressOverlay =findViewById(R.id.progress_overlay);
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        if(type.equals("login")){
            headingLogInTextView.setText(R.string.sign_in);
            submitButtonSignIn.setText(R.string.sign_in);
        }else if(type.equals("signup")){
            headingLogInTextView.setText(R.string.sign_up);
            submitButtonSignIn.setText(R.string.sign_up);
        }else if(type.equals("loggedIn")){
            Intent intent= null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }
            finish();
            startActivity(intent);
        }
        submitButtonSignIn.setOnClickListener(view -> {
            email=emailEditText.getText().toString().trim();
            pw = passwordEditText.getText().toString();
            if(email.equals("") || pw.equals("")) {
                Toast.makeText(this, "Email or Password Incorrect", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                emailEditText.setError("Invalid Email");
                emailEditText.requestFocus();
                Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            progressOverlay.setAnimation(inAnimation);
            progressOverlay.setVisibility(View.VISIBLE);
            if(type.equals("signup")){
                mAuth.createUserWithEmailAndPassword(email,pw).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        progressOverlay.setAnimation(outAnimation);
                        progressOverlay.setVisibility(View.GONE);
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user!=null){
                            user.sendEmailVerification().addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    Toast.makeText(LoginActivity.this, "Verification Email Sent. Please verify your email and login.", Toast.LENGTH_LONG).show();
                                    onBackPressed();
                                }
                            });
                        }
                    }else {
                        progressOverlay.setAnimation(outAnimation);
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, "Sign Up Failed. Please try again", Toast.LENGTH_SHORT).show();
                        emailEditText.getText().clear();
                        passwordEditText.getText().clear();
                        emailEditText.requestFocus();
                    }
                });
            }
            else {
                mAuth.signInWithEmailAndPassword(email,pw).addOnCompleteListener(task ->{
                    if(task.isSuccessful()){
                        FirebaseUser user = mAuth.getCurrentUser();
                        progressOverlay.setAnimation(outAnimation);
                        progressOverlay.setVisibility(View.GONE);
                        if(user!=null && user.isEmailVerified()){
                            //code to move ahead
                            Toast.makeText(this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                            Intent intent= null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                intent = new Intent(LoginActivity.this, MainActivity.class);
                            }
                            finish();
                            startActivity(intent);
                        }else{
                            Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        progressOverlay.setAnimation(outAnimation);
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, "Sign Up Failed. Please try again", Toast.LENGTH_SHORT).show();
                        emailEditText.getText().clear();
                        passwordEditText.getText().clear();
                        emailEditText.requestFocus();
                    }
                });
            }
        });
    }
}