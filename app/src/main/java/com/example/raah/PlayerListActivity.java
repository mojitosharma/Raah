package com.example.raah;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PlayerListActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    View progressOverlay;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    RecyclerView playerListRecyclerView;
    ArrayList<Student> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_list);
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
        progressOverlay.setAnimation(inAnimation);
        progressOverlay.setVisibility(View.VISIBLE);
        String uid = user.getUid();
        playerListRecyclerView = findViewById(R.id.playerListRecyclerView);
        playerListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataList = new ArrayList<>();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Student object = childSnapshot.getValue(Student.class);
                    dataList.add(object);
                }
                MyAdapter adapter = new MyAdapter(dataList);
                playerListRecyclerView.setAdapter(adapter);
                progressOverlay.setAnimation(outAnimation);
                progressOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.i("FetchStudent","failed");
                progressOverlay.setAnimation(outAnimation);
                progressOverlay.setVisibility(View.GONE);

            }
        });
    }
    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private final List<Student> dataList;

        public MyAdapter(List<Student> dataList) {
            this.dataList = dataList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_list_info, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Student data = dataList.get(position);
            //set Text View
            holder.studentNameTextView.setText(data.getName());
            holder.studentUsernameTextView.setText(data.getUsername());
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final LinearLayout linearLayoutPlayerInfo;
        private final TextView studentUsernameTextView;
        private final TextView studentNameTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            studentUsernameTextView = itemView.findViewById(R.id.studentUsernameTextView);
            linearLayoutPlayerInfo = itemView.findViewById(R.id.linearLayoutPlayerInfo);
            studentNameTextView = itemView.findViewById(R.id.studentNameTextView);
            linearLayoutPlayerInfo.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v==linearLayoutPlayerInfo) {
                Toast.makeText(PlayerListActivity.this, studentUsernameTextView.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}