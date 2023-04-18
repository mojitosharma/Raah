package com.example.raah;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
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

import java.util.ArrayList;
import java.util.List;

public class ShowStudentProfileActivity extends AppCompatActivity {
    View progressOverlay;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    String username = "";
    String name="";
    Toolbar toolbarStudentProfile;
    RecyclerView scoreListRecyclerView;
    ArrayList<Score> dataList;
    ImageView refreshProfileButton;
    FirebaseUser user;
    FirebaseAuth mAuth;
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
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
        setContentView(R.layout.activity_show_student_profile);
        username = getIntent().getStringExtra("username");
        name =getIntent().getStringExtra("name");
        progressOverlay =findViewById(R.id.progress_overlay);
        toolbarStudentProfile = findViewById(R.id.toolbarStudentProfile);
        scoreListRecyclerView = findViewById(R.id.scoreListRecyclerView);
        refreshProfileButton=findViewById(R.id.refreshProfileButton);
        scoreListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            toolbarStudentProfile.setSubtitle(username);
            toolbarStudentProfile.setTitle(name);
        }
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        if(isInternetConnected(this)){
            mAuth = FirebaseAuth.getInstance();
            user = mAuth.getCurrentUser();
            if(user==null){
                Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
                return;
            }
            progressOverlay.setAnimation(inAnimation);
            progressOverlay.setVisibility(View.VISIBLE);
            loadData();
        }
        refreshProfileButton.setOnClickListener(view -> {
            if(isInternetConnected(ShowStudentProfileActivity.this)){
                progressOverlay.setAnimation(inAnimation);
                progressOverlay.setVisibility(View.VISIBLE);
                loadData();
            }else{
                Toast.makeText(ShowStudentProfileActivity.this, "Please check your internet and try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void loadData(){
        refreshProfileButton.setEnabled(false);
        String uid = user.getUid();
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
                            refreshProfileButton.setEnabled(true);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                            Log.i("FetchStudent","failed");
                            progressOverlay.setAnimation(outAnimation);
                            progressOverlay.setVisibility(View.GONE);
                            refreshProfileButton.setEnabled(true);

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
            holder.scoreTextView1.setText("Score: "+ (data.getCorrectAttempts() - (0.5 * (data.getTotalAttempts() - data.getCorrectAttempts()))));
            holder.dateAndTimeTextView.setText("Date and Time: "+data.getDateAndTime());
            holder.gameNameTextView.setText("Game Name: "+data.getGameName());
            holder.totalAttemptsTv.setText("Total Attempts: "+data.getTotalAttempts());
            holder.correctAttemptsTv.setText("Correct Attempts: "+data.getCorrectAttempts());
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
        private final TextView totalAttemptsTv;
        private final TextView correctAttemptsTv;

        public MyViewHolder(View itemView) {
            super(itemView);
            scoreTextView1 = itemView.findViewById(R.id.scoreTextView1);
            gameNameTextView = itemView.findViewById(R.id.gameNameTextView);
            dateAndTimeTextView = itemView.findViewById(R.id.dateAndTimeTextView);
            correctAttemptsTv = itemView.findViewById((R.id.correctAttemptsTv));
            totalAttemptsTv = itemView.findViewById(R.id.totalAttemptsTv);
        }
    }
}