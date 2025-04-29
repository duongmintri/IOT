package com.example.btn3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

public class MainActivity extends AppCompatActivity implements
        SensorHandler.GestureListener,
        DeviceController.DeviceControlListener,
        BluetoothHandler.BluetoothListener,
        WiFiHandler.WiFiListener,
        GestureDetector.GestureDetectionListener {

    // UI Components
    private TextView accelerometerDataTextView;
    private TextView gyroscopeDataTextView;
    private TextView gestureDetectedTextView;
    private TextView lightStatusTextView;
    private TextView speakerStatusTextView;
    private TextView connectionStatusTextView;
    private Button toggleLightButton;
    private Button toggleSpeakerButton;
    private Button previousTrackButton;
    private Button nextTrackButton;
    private Button bluetoothButton;
    private Button wifiButton;
    private SeekBar lightBrightnessSeekBar;
    private SeekBar speakerVolumeSeekBar;

    // Handler classes
    private SensorHandler sensorHandler;
    private GestureDetector gestureDetector;
    private DeviceController deviceController;
    private BluetoothHandler bluetoothHandler;
    private WiFiHandler wifiHandler;

    // Device state variables
    private boolean isLightOn = false;
    private boolean isSpeakerOn = false;
    private int lightBrightness = 50;
    private int speakerVolume = 50;

    // Request codes
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_ENABLE_BT = 101;
    private static final int REQUEST_DEVICE_SCAN = 102;
    private static final int REQUEST_SETTINGS = 103;

    // Shared preferences
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "GestureControlPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize UI components
        initializeUIComponents();

        // Check and request permissions
        checkAndRequestPermissions();

        // Initialize handlers
        initializeHandlers();

        // Set up button click listeners
        setupButtonListeners();

        // Set up seekbar listeners
        setupSeekBarListeners();

        // Apply saved sensitivity settings
        applySensitivitySettings();
    }

    private void initializeUIComponents() {
        accelerometerDataTextView = findViewById(R.id.accelerometerDataTextView);
        gyroscopeDataTextView = findViewById(R.id.gyroscopeDataTextView);
        gestureDetectedTextView = findViewById(R.id.gestureDetectedTextView);
        lightStatusTextView = findViewById(R.id.lightStatusTextView);
        speakerStatusTextView = findViewById(R.id.speakerStatusTextView);
        connectionStatusTextView = findViewById(R.id.connectionStatusTextView);
        toggleLightButton = findViewById(R.id.toggleLightButton);
        toggleSpeakerButton = findViewById(R.id.toggleSpeakerButton);
        previousTrackButton = findViewById(R.id.previousTrackButton);
        nextTrackButton = findViewById(R.id.nextTrackButton);
        bluetoothButton = findViewById(R.id.bluetoothButton);
        wifiButton = findViewById(R.id.wifiButton);
        lightBrightnessSeekBar = findViewById(R.id.lightBrightnessSeekBar);
        speakerVolumeSeekBar = findViewById(R.id.speakerVolumeSeekBar);
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void initializeHandlers() {
        // Initialize gesture detector
        gestureDetector = new GestureDetector(this);

        // Initialize sensor handler
        sensorHandler = new SensorHandler(this, this);

        // Initialize device controller
        deviceController = new DeviceController(this);

        // Initialize connectivity handlers
        bluetoothHandler = new BluetoothHandler(this, this);
        wifiHandler = new WiFiHandler(this, this);
    }

    private void setupButtonListeners() {
        toggleLightButton.setOnClickListener(v -> toggleLight());
        toggleSpeakerButton.setOnClickListener(v -> toggleSpeaker());
        previousTrackButton.setOnClickListener(v -> previousTrack());
        nextTrackButton.setOnClickListener(v -> nextTrack());
        bluetoothButton.setOnClickListener(v -> connectBluetooth());
        wifiButton.setOnClickListener(v -> connectWifi());
    }

    private void setupSeekBarListeners() {
        lightBrightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lightBrightness = progress;
                if (fromUser) {
                    adjustLightBrightness(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        speakerVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speakerVolume = progress;
                if (fromUser) {
                    adjustSpeakerVolume(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start sensor listening
        if (sensorHandler != null) {
            sensorHandler.startListening();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop sensor listening
        if (sensorHandler != null) {
            sensorHandler.stopListening();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Disconnect from devices
        if (bluetoothHandler != null && bluetoothHandler.isConnected()) {
            bluetoothHandler.disconnect();
        }
        if (wifiHandler != null && wifiHandler.isConnected()) {
            wifiHandler.disconnect();
        }
    }

    // Update UI with sensor data
    private void updateSensorDataUI(float[] accelerometerData, float[] gyroscopeData) {
        String accelText = String.format("Accelerometer: X=%.2f, Y=%.2f, Z=%.2f",
                accelerometerData[0], accelerometerData[1], accelerometerData[2]);
        accelerometerDataTextView.setText(accelText);

        String gyroText = String.format("Gyroscope: X=%.2f, Y=%.2f, Z=%.2f",
                gyroscopeData[0], gyroscopeData[1], gyroscopeData[2]);
        gyroscopeDataTextView.setText(gyroText);
    }

    private void toggleLight() {
        isLightOn = !isLightOn;
        lightStatusTextView.setText(isLightOn ? R.string.control_on : R.string.control_off);

        // Send command to the connected device
        if (deviceController.isConnected()) {
            deviceController.toggleDevice(DeviceController.DeviceType.LIGHT, isLightOn);
        } else {
            showToast(getString(R.string.msg_no_device));
        }
    }

    private void toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn;
        speakerStatusTextView.setText(isSpeakerOn ? R.string.control_on : R.string.control_off);

        // Send command to the connected device
        if (deviceController.isConnected()) {
            deviceController.toggleDevice(DeviceController.DeviceType.SPEAKER, isSpeakerOn);
        } else {
            showToast(getString(R.string.msg_no_device));
        }
    }

    private void adjustLightBrightness(int brightness) {
        lightBrightness = brightness;

        // Send command to the connected device
        if (deviceController.isConnected()) {
            deviceController.adjustParameter(DeviceController.DeviceType.LIGHT, "BRIGHTNESS", brightness);
        } else {
            showToast(getString(R.string.msg_no_device));
        }
    }

    private void adjustSpeakerVolume(int volume) {
        speakerVolume = volume;

        // Send command to the connected device
        if (deviceController.isConnected()) {
            deviceController.adjustParameter(DeviceController.DeviceType.SPEAKER, "VOLUME", volume);
        } else {
            showToast(getString(R.string.msg_no_device));
        }
    }

    private void previousTrack() {
        // Send command to the connected device
        if (deviceController.isConnected()) {
            deviceController.controlMedia("PREVIOUS");
        } else {
            showToast(getString(R.string.msg_no_device));
        }
    }

    private void nextTrack() {
        // Send command to the connected device
        if (deviceController.isConnected()) {
            deviceController.controlMedia("NEXT");
        } else {
            showToast(getString(R.string.msg_no_device));
        }
    }

    private void connectBluetooth() {
        if (!bluetoothHandler.isBluetoothSupported()) {
            showToast(getString(R.string.msg_bluetooth_not_supported));
            return;
        }

        if (!bluetoothHandler.isBluetoothEnabled()) {
            showToast(getString(R.string.msg_bluetooth_not_enabled));
            // Request to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
            }
            return;
        }

        // Launch device scan activity
        Intent scanIntent = new Intent(this, DeviceScanActivity.class);
        startActivityForResult(scanIntent, REQUEST_DEVICE_SCAN);
    }

    private void connectWifi() {
        if (!wifiHandler.isWiFiEnabled()) {
            showToast(getString(R.string.msg_wifi_not_enabled));
            // Open WiFi settings
            startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
            return;
        }

        if (!wifiHandler.isConnectedToWiFi()) {
            showToast(getString(R.string.msg_wifi_not_connected));
            // Open WiFi settings
            startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
            return;
        }

        // In a real app, this would show a list of available devices on the network
        // For this example, we'll just simulate a connection
        showToast("Connecting to WiFi device...");
        connectionStatusTextView.setText("Connecting...");

        // Simulate successful connection
        deviceController.connect("WiFi Device", DeviceController.ConnectionType.WIFI);
        connectionStatusTextView.setText(R.string.connection_status_connected);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // SensorHandler.GestureListener implementation
    @Override
    public void onWaveGestureDetected() {
        gestureDetectedTextView.setText("Gesture: Wave");
        toggleLight();
    }

    @Override
    public void onTiltGestureDetected(float tiltValue) {
        gestureDetectedTextView.setText("Gesture: Tilt");

        // Map tilt to volume (tilt forward to decrease, backward to increase)
        int newVolume = speakerVolume;
        if (tiltValue > 0) { // Forward tilt
            newVolume = Math.max(0, speakerVolume - 5);
        } else { // Backward tilt
            newVolume = Math.min(100, speakerVolume + 5);
        }

        speakerVolumeSeekBar.setProgress(newVolume);
        adjustSpeakerVolume(newVolume);
    }

    @Override
    public void onRotationGestureDetected(boolean isClockwise) {
        if (isClockwise) {
            gestureDetectedTextView.setText("Gesture: Rotate Right");
            nextTrack();
        } else {
            gestureDetectedTextView.setText("Gesture: Rotate Left");
            previousTrack();
        }
    }

    // DeviceController.DeviceControlListener implementation
    @Override
    public void onCommandSent(String command) {
        showToast(getString(R.string.msg_command_sent, command));
    }

    @Override
    public void onCommandSuccess(String response) {
        // In a real app, this would handle the response from the device
    }

    @Override
    public void onCommandFailure(String error) {
        showToast("Command failed: " + error);
    }

    // BluetoothHandler.BluetoothListener implementation
    @Override
    public void onDeviceConnected(String deviceName) {
        connectionStatusTextView.setText(R.string.connection_status_connected);
        showToast(getString(R.string.msg_device_connected, deviceName));
    }

    @Override
    public void onDeviceDisconnected() {
        connectionStatusTextView.setText(R.string.connection_status_disconnected);
        showToast(getString(R.string.msg_device_disconnected, deviceController.getDeviceName()));
    }

    @Override
    public void onConnectionFailed(String errorMessage) {
        connectionStatusTextView.setText(R.string.connection_status_disconnected);
        showToast("Connection failed: " + errorMessage);
    }

    @Override
    public void onDataReceived(String data) {
        // In a real app, this would handle data received from the device
    }

    // WiFiHandler.WiFiListener implementation (same as Bluetooth for this example)
    // These methods are required by the interface but have the same implementation

    // GestureDetector.GestureDetectionListener implementation
    @Override
    public void onGestureDetected(GestureDetector.GestureType gestureType, float intensity) {
        switch (gestureType) {
            case WAVE:
                gestureDetectedTextView.setText("Gesture: Wave");
                toggleLight();
                break;
            case TILT_FORWARD:
                gestureDetectedTextView.setText("Gesture: Tilt Forward");
                int newVolDown = Math.max(0, speakerVolume - 5);
                speakerVolumeSeekBar.setProgress(newVolDown);
                adjustSpeakerVolume(newVolDown);
                break;
            case TILT_BACKWARD:
                gestureDetectedTextView.setText("Gesture: Tilt Backward");
                int newVolUp = Math.min(100, speakerVolume + 5);
                speakerVolumeSeekBar.setProgress(newVolUp);
                adjustSpeakerVolume(newVolUp);
                break;
            case ROTATE_LEFT:
                gestureDetectedTextView.setText("Gesture: Rotate Left");
                previousTrack();
                break;
            case ROTATE_RIGHT:
                gestureDetectedTextView.setText("Gesture: Rotate Right");
                nextTrack();
                break;
            case SHAKE:
                gestureDetectedTextView.setText("Gesture: Shake");
                toggleSpeaker();
                break;
            case DOUBLE_TAP:
                gestureDetectedTextView.setText("Gesture: Double Tap");
                // Implement play/pause functionality here
                break;
        }
    }

    /**
     * Apply sensitivity settings from shared preferences
     */
    private void applySensitivitySettings() {
        if (gestureDetector != null) {
            float waveSensitivity = SettingsActivity.progressToSensitivity(
                    sharedPreferences.getInt("wave_sensitivity", 50));
            float tiltSensitivity = SettingsActivity.progressToSensitivity(
                    sharedPreferences.getInt("tilt_sensitivity", 50));
            float rotateSensitivity = SettingsActivity.progressToSensitivity(
                    sharedPreferences.getInt("rotate_sensitivity", 50));
            float shakeSensitivity = SettingsActivity.progressToSensitivity(
                    sharedPreferences.getInt("shake_sensitivity", 50));

            gestureDetector.setSensitivity(waveSensitivity, tiltSensitivity,
                    rotateSensitivity, shakeSensitivity);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Launch settings activity
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(settingsIntent, REQUEST_SETTINGS);
            return true;
        } else if (id == R.id.action_scan) {
            // Launch device scan activity
            connectBluetooth();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth was enabled, launch scan activity
                Intent scanIntent = new Intent(this, DeviceScanActivity.class);
                startActivityForResult(scanIntent, REQUEST_DEVICE_SCAN);
            } else {
                showToast(getString(R.string.msg_bluetooth_not_enabled));
            }
        } else if (requestCode == REQUEST_DEVICE_SCAN && resultCode == RESULT_OK && data != null) {
            // Get the selected device address
            String deviceAddress = data.getStringExtra("device_address");
            if (deviceAddress != null) {
                // Connect to the selected device
                showToast("Connecting to device: " + deviceAddress);
                connectionStatusTextView.setText("Connecting...");

                // In a real app, this would connect to the actual device
                // For this example, we'll just simulate a connection
                deviceController.connect(deviceAddress, DeviceController.ConnectionType.BLUETOOTH);
                connectionStatusTextView.setText(R.string.connection_status_connected);
            }
        } else if (requestCode == REQUEST_SETTINGS) {
            // Apply updated settings
            applySensitivitySettings();
        }
    }
}
