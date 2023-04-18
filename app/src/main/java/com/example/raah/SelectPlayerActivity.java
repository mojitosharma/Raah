package com.example.raah;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SelectPlayerActivity extends AppCompatActivity {
    String gameName="";
    View progressOverlay;
    AlphaAnimation inAnimation;
    FirebaseUser user;
    FirebaseAuth mAuth;
    AlphaAnimation outAnimation;
    ImageView refreshSelectPlayerButton;
    int pos=-1;
    String username="";
    RecyclerView playerListRecyclerView;
    Button selectPlayerBackButton,selectPlayerStartButton;
    ArrayList<Student> dataList;
    IntentFilter intentFilter;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED) || (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) &&(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF) == BluetoothAdapter.STATE_OFF))) {
                if(!Variables.isMainActivityRestarted){
                    Toast.makeText(SelectPlayerActivity.this, "Bluetooth disconnected. Please try again.", Toast.LENGTH_SHORT).show();
                    Intent i = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        i = new Intent(SelectPlayerActivity.this, MainActivity.class);
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
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
        setContentView(R.layout.activity_select_player);

        gameName = getIntent().getStringExtra("gameName");
        intentFilter= new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, intentFilter);
        selectPlayerStartButton=findViewById(R.id.selectPlayerStartButton);
        selectPlayerBackButton=findViewById(R.id.selectPlayerBackButton);
        refreshSelectPlayerButton=findViewById(R.id.refreshSelectPlayerButton);
        progressOverlay =findViewById(R.id.progress_overlay);
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        selectPlayerStartButton.setEnabled(false);
        selectPlayerBackButton.setOnClickListener(view -> onBackPressed());
        selectPlayerStartButton.setOnClickListener(view -> {
            if(!username.equals("")){
                Intent intent = new Intent(SelectPlayerActivity.this, GameScreen.class);
                Toast.makeText(this, username, Toast.LENGTH_SHORT).show();
                intent.putExtra("username", username);
                intent.putExtra("gameName", gameName);
                startActivity(intent);
            }
        });
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user==null){
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isInternetConnected(this)){
            Toast.makeText(this, "Please connect to internet to proceed", Toast.LENGTH_SHORT).show();
        }else{
            progressOverlay.setAnimation(inAnimation);
            progressOverlay.setVisibility(View.VISIBLE);
            loadData();
        }
        refreshSelectPlayerButton.setOnClickListener(view -> {
            if(isInternetConnected(SelectPlayerActivity.this)){
                progressOverlay.setAnimation(inAnimation);
                progressOverlay.setVisibility(View.VISIBLE);
                loadData();
            }else{
                Toast.makeText(SelectPlayerActivity.this, "Please check your internet and try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void loadData(){
        refreshSelectPlayerButton.setEnabled(false);
        String uid = user.getUid();
        playerListRecyclerView = findViewById(R.id.playerListRecyclerView1);
        playerListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataList = new ArrayList<>();
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
                MyAdapter adapter = new MyAdapter(dataList);
                playerListRecyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                progressOverlay.setAnimation(outAnimation);
                progressOverlay.setVisibility(View.GONE);
                refreshSelectPlayerButton.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.i("FetchStudent","failed");
                progressOverlay.setAnimation(outAnimation);
                progressOverlay.setVisibility(View.GONE);
                refreshSelectPlayerButton.setEnabled(true);
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
            //set Text View
            final int p = holder.getAdapterPosition();
            final Student myObject = dataList.get(p);
            if(pos==-1){
                holder.relativeLayoutPlayerInfo.setBackgroundColor(Color.WHITE);
//                holder.studentNameTextView.setTextColor(Color.BLACK);
//                holder.studentUsernameTextView.setTextColor(Color.BLACK);
            }else {
                if(pos==p){
                    holder.relativeLayoutPlayerInfo.setBackgroundColor(getResources().getColor(R.color.selectedPlayer));
//                    holder.studentNameTextView.setTextColor(Color.WHITE);
//                    holder.studentUsernameTextView.setTextColor(Color.WHITE);
                }else {
                    holder.relativeLayoutPlayerInfo.setBackgroundColor(Color.WHITE);
//                    holder.studentNameTextView.setTextColor(Color.BLACK);
//                    holder.studentUsernameTextView.setTextColor(Color.BLACK);
                }
            }
            holder.studentNameTextView.setText(myObject.getName());
            holder.studentUsernameTextView.setText(myObject.getUsername());
            holder.relativeLayoutPlayerInfo.setOnClickListener(view -> {
                selectPlayerStartButton.setEnabled(true);
                if(pos!=p){
                    notifyItemChanged(pos);
                }
                pos = p;
                holder.relativeLayoutPlayerInfo.setBackgroundColor(getResources().getColor(R.color.selectedPlayer));
//                holder.studentNameTextView.setTextColor(Color.WHITE);
//                holder.studentUsernameTextView.setTextColor(Color.WHITE);
                pos=p;
                username= myObject.getUsername();

            });
            holder.menuOfPlayerImageView.setEnabled(false);
            holder.menuOfPlayerImageView.setVisibility(View.INVISIBLE);
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