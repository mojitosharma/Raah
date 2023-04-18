package com.example.raah;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final SelectDeviceActivity context;
    private final List<Object> deviceList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textAddress;
        LinearLayout linearLayout;

        public ViewHolder(View v) {
            super(v);
            textName = v.findViewById(R.id.textViewDeviceName);
            textAddress = v.findViewById(R.id.textViewDeviceAddress);
            linearLayout = v.findViewById(R.id.linearLayoutDeviceInfo);
        }
    }

    public DeviceListAdapter(SelectDeviceActivity context, List<Object> deviceList) {
        this.context = context;
        this.deviceList = deviceList;

    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_info_layout, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        ViewHolder itemHolder = (ViewHolder) holder;
        int p = itemHolder.getAdapterPosition();
        final DeviceInfoModel deviceInfoModel = (DeviceInfoModel) deviceList.get(position);
        if(context.pos==-1){
            ((ViewHolder) holder).linearLayout.setBackgroundColor(Color.WHITE);
        }else {
            if(context.pos==p){
                ((ViewHolder) holder).linearLayout.setBackgroundColor(context.getResources().getColor(R.color.selectedPlayer));
            }else {
                ((ViewHolder) holder).linearLayout.setBackgroundColor(Color.WHITE);
            }
        }
        itemHolder.textName.setText(deviceInfoModel.getDeviceName());
        itemHolder.textAddress.setText(deviceInfoModel.getDeviceHardwareAddress());

        // When a device is selected
        itemHolder.linearLayout.setOnClickListener(view -> {
//            context.bluetoothAdapter.cancelDiscovery();
            context.selectDeviceStartButton.setEnabled(true);
            if(context.pos!=p){
                notifyItemChanged(context.pos);
            }
            context.pos = p;
            ((ViewHolder) holder).linearLayout.setBackgroundColor(context.getResources().getColor(R.color.selectedPlayer));
//                holder.studentNameTextView.setTextColor(Color.WHITE);
//                holder.studentUsernameTextView.setTextColor(Color.WHITE);
            context.pos=p;
            context.deviceAddress= deviceInfoModel.getDeviceHardwareAddress();
            context.deviceName = deviceInfoModel.getDeviceName();
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}