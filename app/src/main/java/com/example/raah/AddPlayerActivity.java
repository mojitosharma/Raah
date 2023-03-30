package com.example.raah;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AddPlayerActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText usernameEditText,nameEditText;
    Button submitButtonAddPlayer;
    String name="";
    String  username="";
    View progressOverlay;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_player);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user==null){
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }
        progressOverlay =findViewById(R.id.progress_overlay);
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        nameEditText = findViewById(R.id.nameEditText);
        usernameEditText=findViewById(R.id.usernameEditText);
        submitButtonAddPlayer =findViewById(R.id.submitButtonAddPlayer);
        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        submitButtonAddPlayer.setOnClickListener(view -> {
            progressOverlay.setAnimation(inAnimation);
            progressOverlay.setVisibility(View.VISIBLE);
            name=nameEditText.getText().toString().trim();
            username=usernameEditText.getText().toString().trim();
            Student student= new Student(name, username);
            // Query the user's node for objects with the same username
            Query query = userRef.orderByChild("username").equalTo(student.getUsername());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Check if there is already an object with the same username
                    if (dataSnapshot.exists()) {
                        // Update the existing object with the new information
                        Toast.makeText(AddPlayerActivity.this, "Username already exists. Please enter new username.", Toast.LENGTH_SHORT).show();
                        usernameEditText.requestFocus();
                    } else {
                        // Add the new object under the user's node
                        String objectId = userRef.push().getKey();
                        if(objectId!=null){
                            userRef.child(objectId).setValue(student).addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    Toast.makeText(AddPlayerActivity.this, "Player added", Toast.LENGTH_SHORT).show();
                                    usernameEditText.getText().clear();
                                    nameEditText.getText().clear();
                                }
                            });
                        }else{
                            Log.i("ObjectId","Null");
                        }
                    }
                    progressOverlay.setAnimation(outAnimation);
                    progressOverlay.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddPlayerActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                    progressOverlay.setAnimation(outAnimation);
                    progressOverlay.setVisibility(View.GONE);
                }
            });
        });
    }
}