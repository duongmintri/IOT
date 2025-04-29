package com.example.btn3;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Class to handle WiFi connectivity for IoT devices
 */
public class WiFiHandler {

    private static final String TAG = "WiFiHandler";
    private static final int DEFAULT_PORT = 8080;
    private static final int CONNECTION_TIMEOUT = 5000; // 5 seconds

    // Interface for WiFi callbacks
    public interface WiFiListener {
        void onDeviceConnected(String deviceAddress);
        void onDeviceDisconnected();
        void onConnectionFailed(String errorMessage);
        void onDataReceived(String data);
    }

    private final Context context;
    private final WiFiListener listener;
    private final Handler mainHandler;
    private final ConnectivityManager connectivityManager;
    private final WifiManager wifiManager;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = false;
    private ConnectedThread connectedThread;

    public WiFiHandler(Context context, WiFiListener listener) {
        this.context = context;
        this.listener = listener;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * Check if WiFi is enabled
     * @return true if enabled, false otherwise
     */
    public boolean isWiFiEnabled() {
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    /**
     * Check if connected to a WiFi network
     * @return true if connected, false otherwise
     */
    public boolean isConnectedToWiFi() {
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(
                    connectivityManager.getActiveNetwork());
            return capabilities != null && 
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        }
        return false;
    }

    /**
     * Connect to a device via WiFi
     * @param ipAddress IP address of the device
     * @param port Port to connect to (use DEFAULT_PORT if 0)
     */
    public void connect(final String ipAddress, final int port) {
        if (!isConnectedToWiFi()) {
            notifyConnectionFailed("Not connected to WiFi");
            return;
        }

        // Cancel any existing connections
        disconnect();

        final int targetPort = port > 0 ? port : DEFAULT_PORT;

        // Start a new connection thread
        new Thread(() -> {
            try {
                // Create a socket connection to the device
                socket = new Socket();
                socket.connect(new InetSocketAddress(ipAddress, targetPort), CONNECTION_TIMEOUT);
                
                // Get the input and output streams
                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                
                // Start the connected thread to handle communication
                connectedThread = new ConnectedThread();
                connectedThread.start();
                
                isConnected = true;
                notifyDeviceConnected(ipAddress);
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
            if (out != null) {
                out.close();
                out = null;
            }
            if (in != null) {
                in.close();
                in = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
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
        if (!isConnected || out == null) {
            return false;
        }

        try {
            out.println(data);
            return true;
        } catch (Exception e) {
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

    /**
     * Register for network callbacks
     */
    public void registerNetworkCallback() {
        if (connectivityManager != null) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
            
            connectivityManager.registerNetworkCallback(builder.build(), 
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        super.onAvailable(network);
                        // WiFi is available
                    }

                    @Override
                    public void onLost(Network network) {
                        super.onLost(network);
                        // WiFi is lost, disconnect if connected
                        if (isConnected) {
                            disconnect();
                        }
                    }
                });
        }
    }

    /**
     * Unregister network callbacks
     */
    public void unregisterNetworkCallback() {
        // In a real implementation, you would unregister the network callback here
    }

    private void notifyDeviceConnected(final String deviceAddress) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onDeviceConnected(deviceAddress);
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

        @Override
        public void run() {
            while (running) {
                try {
                    if (in != null) {
                        String data = in.readLine();
                        if (data != null) {
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
