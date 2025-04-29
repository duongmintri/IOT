package com.example.btn3;

import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private final List<BluetoothDevice> devices;
    private final DeviceClickListener listener;

    public interface DeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }

    public DeviceAdapter(List<BluetoothDevice> devices, DeviceClickListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        holder.bind(device, listener);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final TextView deviceNameTextView;
        private final TextView deviceAddressTextView;
        private final Button connectButton;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameTextView = itemView.findViewById(R.id.deviceNameTextView);
            deviceAddressTextView = itemView.findViewById(R.id.deviceAddressTextView);
            connectButton = itemView.findViewById(R.id.connectButton);
        }

        public void bind(BluetoothDevice device, DeviceClickListener listener) {
            if (ActivityCompat.checkSelfPermission(itemView.getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                deviceNameTextView.setText("Unknown Device");
                deviceAddressTextView.setText(device.getAddress());
            } else {
                String deviceName = device.getName();
                if (deviceName == null || deviceName.isEmpty()) {
                    deviceName = "Unknown Device";
                }
                deviceNameTextView.setText(deviceName);
                deviceAddressTextView.setText(device.getAddress());
            }

            connectButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeviceClick(device);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeviceClick(device);
                }
            });
        }
    }
}
