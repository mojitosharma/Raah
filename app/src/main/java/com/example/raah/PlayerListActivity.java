package com.example.raah;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.widget.RelativeLayout;
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
import java.util.Comparator;
import java.util.List;

public class PlayerListActivity extends AppCompatActivity {
    View progressOverlay;
    FirebaseAuth mAuth;
    FirebaseUser user;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    RecyclerView playerListRecyclerView;
    ArrayList<Student> dataList;
    ImageView refreshPlayerListButton;
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
        setContentView(R.layout.activity_player_list);
        progressOverlay =findViewById(R.id.progress_overlay);
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        playerListRecyclerView = findViewById(R.id.playerListRecyclerView);
        playerListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataList = new ArrayList<>();
        refreshPlayerListButton=findViewById(R.id.refreshPlayerListButton);
        refreshPlayerListButton.setOnClickListener(view -> {
            if(isInternetConnected(PlayerListActivity.this)){
                progressOverlay.setAnimation(inAnimation);
                progressOverlay.setVisibility(View.VISIBLE);
                loadData();
            }else{
                Toast.makeText(PlayerListActivity.this, "Please check your internet and try again.", Toast.LENGTH_SHORT).show();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }
        if(isInternetConnected(this)){
            progressOverlay.setAnimation(inAnimation);
            progressOverlay.setVisibility(View.VISIBLE);
            loadData();
        }else{
            Toast.makeText(this, "Please check your internet and try again.", Toast.LENGTH_SHORT).show();
        }
    }
    public void loadData(){
        refreshPlayerListButton.setEnabled(false);
        String uid = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("teachers").child(uid);

        userRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataList.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Student object = childSnapshot.getValue(Student.class);
                    dataList.add(object);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    dataList.sort(Comparator.comparing(Student::getName));
                }
                MyAdapter adapter = new MyAdapter(dataList,userRef);
                playerListRecyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                progressOverlay.setAnimation(outAnimation);
                progressOverlay.setVisibility(View.GONE);
                refreshPlayerListButton.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.i("FetchStudent","failed");
                progressOverlay.setAnimation(outAnimation);
                progressOverlay.setVisibility(View.GONE);
                refreshPlayerListButton.setEnabled(true);
            }
        });
    }
    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private final List<Student> dataList;
        private final DatabaseReference databaseReference;

        public MyAdapter(List<Student> dataList,DatabaseReference databaseReference) {
            this.dataList = dataList;
            this.databaseReference = databaseReference;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_list_info, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            //set Text View
            final int p = holder.getAdapterPosition();
            final Student myObject = dataList.get(p);
            holder.studentNameTextView.setText(myObject.getName());
            holder.studentUsernameTextView.setText(myObject.getUsername());
            holder.relativeLayoutPlayerInfo.setOnClickListener(view -> {
                if(isInternetConnected(view.getContext())){
                    Intent intent = new Intent(PlayerListActivity.this, ShowStudentProfileActivity.class);
                    intent.putExtra("username", myObject.getUsername());
                    intent.putExtra("name",myObject.getName());
                    startActivity(intent);
                }else{
                    Toast.makeText(PlayerListActivity.this, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
                }
            });
            holder.menuOfPlayerImageView.setOnClickListener(view -> {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), holder.menuOfPlayerImageView);
                popupMenu.getMenuInflater().inflate(R.menu.player_item_options, popupMenu.getMenu());

                // Add click listeners for each menu item
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.menu_show_scores_item) {
                        if(isInternetConnected(view.getContext())){
                            Intent intent = new Intent(PlayerListActivity.this, ShowStudentProfileActivity.class);
                            intent.putExtra("username", myObject.getUsername());
                            startActivity(intent);
                        }else{
                            Toast.makeText(PlayerListActivity.this, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }else if(item.getItemId() == R.id.menu_delete_item){
                        if(!isInternetConnected(view.getContext())){
                            Toast.makeText(PlayerListActivity.this, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setTitle("Confirm deletion")
                                .setMessage("Are you sure you want to delete this student?")
                                .setPositiveButton("Delete", (dialog, which) -> {

                                    Query deleteQuery = databaseReference.orderByChild("username").equalTo(myObject.getUsername());
                                    deleteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                                appleSnapshot.getRef().removeValue((error, ref) -> {
                                                    if(error==null){
                                                        Toast.makeText(PlayerListActivity.this, myObject.getUsername()+" deleted", Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Toast.makeText(PlayerListActivity.this, "Could not delete", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                break;
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.e("DeleteStudent", "onCancelled", databaseError.toException());
                                        }
                                    });
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        return true;
                    }else{
                        return false;
                    }
                });

                // Show the PopupMenu
                popupMenu.show();
            });
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout relativeLayoutPlayerInfo;
        private final TextView studentUsernameTextView;
        private final TextView studentNameTextView;
        private final ImageView menuOfPlayerImageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            studentUsernameTextView = itemView.findViewById(R.id.studentUsernameTextView);
            relativeLayoutPlayerInfo = itemView.findViewById(R.id.relativeLayoutPlayerInfo);
            studentNameTextView = itemView.findViewById(R.id.studentNameTextView);
            menuOfPlayerImageView = itemView.findViewById(R.id.menuOfPlayerImageView);
        }
    }
}