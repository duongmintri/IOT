package com.example.btn3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceScanActivity extends AppCompatActivity implements DeviceAdapter.DeviceClickListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_BLUETOOTH = 2;

    private RecyclerView pairedDevicesRecyclerView;
    private RecyclerView availableDevicesRecyclerView;
    private DeviceAdapter pairedDevicesAdapter;
    private DeviceAdapter availableDevicesAdapter;
    private Button scanButton;
    private ProgressBar scanProgressBar;

    private BluetoothAdapter bluetoothAdapter;
    private boolean isScanning = false;
    private List<BluetoothDevice> pairedDevicesList = new ArrayList<>();
    private List<BluetoothDevice> availableDevicesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.scan_title);
        }

        // Initialize UI components
        initializeUIComponents();

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, R.string.msg_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_BLUETOOTH);
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // Bluetooth is enabled, get paired devices
            getPairedDevices();
        }

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
    }

    private void initializeUIComponents() {
        pairedDevicesRecyclerView = findViewById(R.id.pairedDevicesRecyclerView);
        availableDevicesRecyclerView = findViewById(R.id.availableDevicesRecyclerView);
        scanButton = findViewById(R.id.scanButton);
        scanProgressBar = findViewById(R.id.scanProgressBar);

        // Set up RecyclerViews
        pairedDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        availableDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up adapters
        pairedDevicesAdapter = new DeviceAdapter(pairedDevicesList, this);
        availableDevicesAdapter = new DeviceAdapter(availableDevicesList, this);

        pairedDevicesRecyclerView.setAdapter(pairedDevicesAdapter);
        availableDevicesRecyclerView.setAdapter(availableDevicesAdapter);

        // Set up scan button
        scanButton.setOnClickListener(v -> {
            if (isScanning) {
                stopScan();
            } else {
                startScan();
            }
        });
    }

    private void getPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_BLUETOOTH);
            return;
        }
        
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        pairedDevicesList.clear();
        
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            pairedDevicesList.addAll(pairedDevices);
            pairedDevicesAdapter.notifyDataSetChanged();
        }
    }

    private void startScan() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, REQUEST_PERMISSION_BLUETOOTH);
            return;
        }
        
        // Clear previous results
        availableDevicesList.clear();
        availableDevicesAdapter.notifyDataSetChanged();
        
        // Start discovery
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        
        bluetoothAdapter.startDiscovery();
        isScanning = true;
        scanButton.setText(R.string.scan_stop);
        scanProgressBar.setVisibility(View.VISIBLE);
        
        // Show scanning message
        Toast.makeText(this, R.string.scan_scanning, Toast.LENGTH_SHORT).show();
    }

    private void stopScan() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        
        isScanning = false;
        scanButton.setText(R.string.scan_start);
        scanProgressBar.setVisibility(View.GONE);
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                
                // Add device to list if it's not already there
                if (!availableDevicesList.contains(device) && !pairedDevicesList.contains(device)) {
                    availableDevicesList.add(device);
                    availableDevicesAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // Discovery has finished
                stopScan();
                
                if (availableDevicesList.isEmpty()) {
                    Toast.makeText(context, R.string.scan_no_devices, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public void onDeviceClick(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        // Stop discovery because it's resource intensive
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        
        // Return the selected device to the calling activity
        Intent intent = new Intent();
        intent.putExtra("device_address", device.getAddress());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Unregister broadcast receiver
        unregisterReceiver(receiver);
        
        // Make sure we're not doing discovery anymore
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button in the action bar
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
