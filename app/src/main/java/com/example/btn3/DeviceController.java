package com.example.btn3;

/**
 * Class to handle communication with smart devices
 */
public class DeviceController {

    // Interface for device control callbacks
    public interface DeviceControlListener {
        void onCommandSent(String command);
        void onCommandSuccess(String response);
        void onCommandFailure(String error);
    }

    // Device types
    public enum DeviceType {
        LIGHT,
        SPEAKER,
        TV,
        FAN
    }

    // Connection types
    public enum ConnectionType {
        BLUETOOTH,
        WIFI
    }

    private final DeviceControlListener listener;
    private ConnectionType connectionType = ConnectionType.BLUETOOTH;
    private boolean isConnected = false;
    private String deviceName = "";

    public DeviceController(DeviceControlListener listener) {
        this.listener = listener;
    }

    /**
     * Connect to a device
     * @param deviceName Name of the device to connect to
     * @param connectionType Type of connection (Bluetooth or WiFi)
     * @return true if connection successful, false otherwise
     */
    public boolean connect(String deviceName, ConnectionType connectionType) {
        // In a real implementation, this would establish a connection to the device
        // For this example, we'll simulate a successful connection
        this.deviceName = deviceName;
        this.connectionType = connectionType;
        this.isConnected = true;
        return true;
    }

    /**
     * Disconnect from the current device
     */
    public void disconnect() {
        // In a real implementation, this would close the connection to the device
        isConnected = false;
        deviceName = "";
    }

    /**
     * Check if connected to a device
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Get the name of the connected device
     * @return Device name or empty string if not connected
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Toggle a device on/off
     * @param deviceType Type of device to toggle
     * @param state true for on, false for off
     */
    public void toggleDevice(DeviceType deviceType, boolean state) {
        if (!isConnected) {
            if (listener != null) {
                listener.onCommandFailure("Not connected to any device");
            }
            return;
        }

        String command = buildCommand(deviceType, state ? "ON" : "OFF");
        sendCommand(command);
    }

    /**
     * Adjust device parameter (volume, brightness, etc.)
     * @param deviceType Type of device to adjust
     * @param parameter Parameter to adjust (VOLUME, BRIGHTNESS, etc.)
     * @param value Value to set (0-100)
     */
    public void adjustParameter(DeviceType deviceType, String parameter, int value) {
        if (!isConnected) {
            if (listener != null) {
                listener.onCommandFailure("Not connected to any device");
            }
            return;
        }

        String command = buildCommand(deviceType, parameter + ":" + value);
        sendCommand(command);
    }

    /**
     * Control media playback
     * @param action Action to perform (PLAY, PAUSE, NEXT, PREVIOUS)
     */
    public void controlMedia(String action) {
        if (!isConnected) {
            if (listener != null) {
                listener.onCommandFailure("Not connected to any device");
            }
            return;
        }

        String command = "MEDIA:" + action;
        sendCommand(command);
    }

    /**
     * Build a command string for the device
     * @param deviceType Type of device
     * @param action Action to perform
     * @return Formatted command string
     */
    private String buildCommand(DeviceType deviceType, String action) {
        String devicePrefix;
        switch (deviceType) {
            case LIGHT:
                devicePrefix = "LIGHT";
                break;
            case SPEAKER:
                devicePrefix = "SPEAKER";
                break;
            case TV:
                devicePrefix = "TV";
                break;
            case FAN:
                devicePrefix = "FAN";
                break;
            default:
                devicePrefix = "DEVICE";
                break;
        }
        return devicePrefix + ":" + action;
    }

    /**
     * Send a command to the connected device
     * @param command Command to send
     */
    private void sendCommand(String command) {
        if (listener != null) {
            listener.onCommandSent(command);
        }

        // In a real implementation, this would send the command to the device
        // and handle the response
        
        // Simulate a successful response
        if (listener != null) {
            listener.onCommandSuccess("OK");
        }
    }
}
