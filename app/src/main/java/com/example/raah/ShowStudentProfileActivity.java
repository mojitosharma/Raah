package com.example.raah;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
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

import java.util.ArrayList;
import java.util.List;

public class ShowStudentProfileActivity extends AppCompatActivity {
    View progressOverlay;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    RecyclerView scoreListRecyclerView;
    ArrayList<Score> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_student_profile);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user==null){
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }
        String username = getIntent().getStringExtra("username");
        progressOverlay =findViewById(R.id.progress_overlay);
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        progressOverlay.setAnimation(inAnimation);
        progressOverlay.setVisibility(View.VISIBLE);
        String uid = user.getUid();
        scoreListRecyclerView = findViewById(R.id.scoreListRecyclerView);
        scoreListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataList = new ArrayList<>();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("teachers").child(uid);
        Query query = userRef.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    dataSnapshot.getRef().child("Scores").addValueEventListener(new ValueEventListener() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            dataList.clear();
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                Score object = childSnapshot.getValue(Score.class);
                                dataList.add(object);
                            }
                            MyAdapter adapter = new MyAdapter(dataList);
                            scoreListRecyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
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
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressOverlay.setAnimation(outAnimation);
                progressOverlay.setVisibility(View.GONE);
                Log.i("DatabaseError", error +" "+"ShowScoreActivity");
            }
        });
    }
    private static class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private final List<Score> dataList;

        public MyAdapter(List<Score> dataList) {
            this.dataList = dataList;
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_score_of_player_list, parent, false);
            return new MyViewHolder(itemView);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Score data = dataList.get(position);
            //set Text View
            holder.scoreTextView1.setText("Correct: "+data.getCorrectAttempts()+"\n"+"Total Attempts: "+data.getTotalAttempts());
            holder.dateAndTimeTextView.setText("Date and Time: "+data.getDateAndTime());
            holder.gameNameTextView.setText("Game Name: "+data.getGameName());

        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView scoreTextView1;
        private final TextView gameNameTextView;
        private final TextView dateAndTimeTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            scoreTextView1 = itemView.findViewById(R.id.scoreTextView1);
            gameNameTextView = itemView.findViewById(R.id.gameNameTextView);
            dateAndTimeTextView = itemView.findViewById(R.id.dateAndTimeTextView);
        }
    }
}