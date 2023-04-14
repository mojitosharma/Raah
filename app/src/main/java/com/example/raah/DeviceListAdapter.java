package com.example.raah;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.Context;
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
        final DeviceInfoModel deviceInfoModel = (DeviceInfoModel) deviceList.get(position);
        final BluetoothDevice device = context.devices.get(position);
        itemHolder.textName.setText(deviceInfoModel.getDeviceName());
        itemHolder.textAddress.setText(deviceInfoModel.getDeviceHardwareAddress());

        // When a device is selected
        itemHolder.linearLayout.setOnClickListener(view -> {
//            context.bluetoothAdapter.cancelDiscovery();
            if(!context.pairedDevices.contains(deviceInfoModel.getDeviceHardwareAddress())){
                device.createBond();
            }else{
                Intent i = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    i = new Intent(context, MainActivity.class);
                }
                // Send device details to the MainActivity
                Variables.deviceName = device.getName();
                Variables.deviceAddress = device.getAddress();
                Toast.makeText(context, "Paired to "+device.getName(), Toast.LENGTH_SHORT).show();
                // Call MainActivity
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}