package com.example.btn3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Class to handle Bluetooth connectivity
 */
public class BluetoothHandler {

    private static final String TAG = "BluetoothHandler";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SerialPortService ID

    // Interface for Bluetooth callbacks
    public interface BluetoothListener {
        void onDeviceConnected(String deviceName);
        void onDeviceDisconnected();
        void onConnectionFailed(String errorMessage);
        void onDataReceived(String data);
    }

    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothListener listener;
    private final Handler mainHandler;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private boolean isConnected = false;
    private ConnectedThread connectedThread;

    public BluetoothHandler(Context context, BluetoothListener listener) {
        this.listener = listener;
        this.mainHandler = new Handler(Looper.getMainLooper());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Check if Bluetooth is supported on this device
     * @return true if supported, false otherwise
     */
    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    /**
     * Check if Bluetooth is enabled
     * @return true if enabled, false otherwise
     */
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * Get a list of paired devices
     * @return Set of paired BluetoothDevice objects
     */
    public Set<BluetoothDevice> getPairedDevices() {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.getBondedDevices();
        }
        return null;
    }

    /**
     * Connect to a Bluetooth device
     * @param device BluetoothDevice to connect to
     */
    public void connect(final BluetoothDevice device) {
        if (device == null) {
            notifyConnectionFailed("Device is null");
            return;
        }

        // Cancel any existing connections
        disconnect();

        // Start a new connection thread
        new Thread(() -> {
            try {
                // Create a socket connection to the device
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                
                // Get the input and output streams
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
                
                // Start the connected thread to handle communication
                connectedThread = new ConnectedThread();
                connectedThread.start();
                
                isConnected = true;
                notifyDeviceConnected(device.getName());
            } catch (IOException e) {
                Log.e(TAG, "Connection failed", e);
                notifyConnectionFailed("Failed to connect: " + e.getMessage());
                disconnect();
            }
        }).start();
    }

    /**
     * Disconnect from the current device
     */
    public void disconnect() {
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
            isConnected = false;
            notifyDeviceDisconnected();
        } catch (IOException e) {
            Log.e(TAG, "Error during disconnection", e);
        }
    }

    /**
     * Send data to the connected device
     * @param data Data to send
     * @return true if data was sent, false otherwise
     */
    public boolean sendData(String data) {
        if (!isConnected || outputStream == null) {
            return false;
        }

        try {
            outputStream.write(data.getBytes());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error sending data", e);
            disconnect();
            return false;
        }
    }

    /**
     * Check if connected to a device
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return isConnected;
    }

    private void notifyDeviceConnected(final String deviceName) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onDeviceConnected(deviceName);
            }
        });
    }

    private void notifyDeviceDisconnected() {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onDeviceDisconnected();
            }
        });
    }

    private void notifyConnectionFailed(final String errorMessage) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onConnectionFailed(errorMessage);
            }
        });
    }

    private void notifyDataReceived(final String data) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onDataReceived(data);
            }
        });
    }

    /**
     * Thread to handle communication with the connected device
     */
    private class ConnectedThread extends Thread {
        private boolean running = true;
        private final byte[] buffer = new byte[1024];

        @Override
        public void run() {
            while (running) {
                try {
                    if (inputStream != null) {
                        int bytesRead = inputStream.read(buffer);
                        if (bytesRead > 0) {
                            String data = new String(buffer, 0, bytesRead);
                            notifyDataReceived(data);
                        }
                    } else {
                        break;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading data", e);
                    disconnect();
                    break;
                }
            }
        }

        public void cancel() {
            running = false;
        }
    }
}
