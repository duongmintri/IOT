package com.example.btn3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Class to handle sensor data processing and gesture detection
 */
public class SensorHandler implements SensorEventListener {

    // Interface for gesture callbacks
    public interface GestureListener {
        void onWaveGestureDetected();
        void onTiltGestureDetected(float tiltValue);
        void onRotationGestureDetected(boolean isClockwise);
    }

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor gyroscope;
    private final GestureListener gestureListener;

    // Sensor data
    private final float[] accelerometerValues = new float[3];
    private final float[] gyroscopeValues = new float[3];
    private long lastGestureTime = 0;

    // Gesture detection thresholds
    private static final float WAVE_THRESHOLD = 12.0f;
    private static final float TILT_THRESHOLD = 3.0f;
    private static final float ROTATION_THRESHOLD = 2.5f;
    private static final long MIN_TIME_BETWEEN_GESTURES = 800; // milliseconds

    public SensorHandler(Context context, GestureListener listener) {
        this.gestureListener = listener;
        
        // Initialize sensors
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public void startListening() {
        if (sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stopListening() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerValues, 0, 3);
            detectWaveGesture();
            detectTiltGesture();
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(event.values, 0, gyroscopeValues, 0, 3);
            detectRotationGesture();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this implementation
    }

    private void detectWaveGesture() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastGestureTime > MIN_TIME_BETWEEN_GESTURES) {
            float x = accelerometerValues[0];
            float y = accelerometerValues[1];
            float z = accelerometerValues[2];

            // Calculate total acceleration magnitude
            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
            
            // Check if acceleration exceeds threshold (wave gesture)
            if (acceleration > WAVE_THRESHOLD) {
                lastGestureTime = currentTime;
                if (gestureListener != null) {
                    gestureListener.onWaveGestureDetected();
                }
            }
        }
    }

    private void detectTiltGesture() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastGestureTime > MIN_TIME_BETWEEN_GESTURES) {
            float y = accelerometerValues[1]; // Y-axis tilt (forward/backward)
            
            // Check if tilt exceeds threshold
            if (Math.abs(y) > TILT_THRESHOLD) {
                lastGestureTime = currentTime;
                if (gestureListener != null) {
                    gestureListener.onTiltGestureDetected(y);
                }
            }
        }
    }

    private void detectRotationGesture() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastGestureTime > MIN_TIME_BETWEEN_GESTURES) {
            float z = gyroscopeValues[2]; // Z-axis rotation (clockwise/counterclockwise)
            
            // Check if rotation exceeds threshold
            if (Math.abs(z) > ROTATION_THRESHOLD) {
                lastGestureTime = currentTime;
                if (gestureListener != null) {
                    gestureListener.onRotationGestureDetected(z > 0);
                }
            }
        }
    }

    // Getter methods for sensor values
    public float[] getAccelerometerValues() {
        return accelerometerValues;
    }

    public float[] getGyroscopeValues() {
        return gyroscopeValues;
    }
}
