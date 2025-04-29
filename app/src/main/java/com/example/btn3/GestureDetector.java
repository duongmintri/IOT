package com.example.btn3;

/**
 * Class to detect and classify gestures based on sensor data
 */
public class GestureDetector {

    // Gesture types
    public enum GestureType {
        WAVE,
        TILT_FORWARD,
        TILT_BACKWARD,
        ROTATE_LEFT,
        ROTATE_RIGHT,
        SHAKE,
        DOUBLE_TAP,
        NONE
    }

    // Interface for gesture detection callbacks
    public interface GestureDetectionListener {
        void onGestureDetected(GestureType gestureType, float intensity);
    }

    // Thresholds for gesture detection
    private static final float WAVE_THRESHOLD = 12.0f;
    private static final float TILT_THRESHOLD = 3.0f;
    private static final float ROTATION_THRESHOLD = 2.5f;
    private static final float SHAKE_THRESHOLD = 18.0f;

    // Minimum time between gesture detections (to prevent multiple detections)
    private static final long MIN_TIME_BETWEEN_GESTURES = 800; // milliseconds

    private final GestureDetectionListener listener;
    private long lastGestureTime = 0;
    private GestureType lastGestureType = GestureType.NONE;

    // Sensitivity settings (1.0 = normal, lower = more sensitive)
    private float waveSensitivity = 1.0f;
    private float tiltSensitivity = 1.0f;
    private float rotationSensitivity = 1.0f;
    private float shakeSensitivity = 1.0f;

    public GestureDetector(GestureDetectionListener listener) {
        this.listener = listener;
    }

    /**
     * Process accelerometer data to detect gestures
     * @param x X-axis acceleration
     * @param y Y-axis acceleration
     * @param z Z-axis acceleration
     */
    public void processAccelerometerData(float x, float y, float z) {
        long currentTime = System.currentTimeMillis();

        // Check if enough time has passed since the last gesture
        if (currentTime - lastGestureTime < MIN_TIME_BETWEEN_GESTURES) {
            return;
        }

        // Calculate total acceleration magnitude
        float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

        // Check for shake gesture (very high acceleration)
        if (acceleration > SHAKE_THRESHOLD * shakeSensitivity) {
            lastGestureTime = currentTime;
            lastGestureType = GestureType.SHAKE;
            if (listener != null) {
                listener.onGestureDetected(GestureType.SHAKE, acceleration);
            }
            return;
        }

        // Check for wave gesture (high acceleration)
        if (acceleration > WAVE_THRESHOLD * waveSensitivity) {
            lastGestureTime = currentTime;
            lastGestureType = GestureType.WAVE;
            if (listener != null) {
                listener.onGestureDetected(GestureType.WAVE, acceleration);
            }
            return;
        }

        // Check for tilt gestures
        if (Math.abs(y) > TILT_THRESHOLD * tiltSensitivity) {
            lastGestureTime = currentTime;
            if (y > 0) {
                lastGestureType = GestureType.TILT_FORWARD;
                if (listener != null) {
                    listener.onGestureDetected(GestureType.TILT_FORWARD, y);
                }
            } else {
                lastGestureType = GestureType.TILT_BACKWARD;
                if (listener != null) {
                    listener.onGestureDetected(GestureType.TILT_BACKWARD, -y);
                }
            }
        }
    }

    /**
     * Process gyroscope data to detect gestures
     * @param x X-axis rotation
     * @param y Y-axis rotation
     * @param z Z-axis rotation
     */
    public void processGyroscopeData(float x, float y, float z) {
        long currentTime = System.currentTimeMillis();

        // Check if enough time has passed since the last gesture
        if (currentTime - lastGestureTime < MIN_TIME_BETWEEN_GESTURES) {
            return;
        }

        // Check for rotation gestures
        if (Math.abs(z) > ROTATION_THRESHOLD * rotationSensitivity) {
            lastGestureTime = currentTime;
            if (z > 0) {
                lastGestureType = GestureType.ROTATE_RIGHT;
                if (listener != null) {
                    listener.onGestureDetected(GestureType.ROTATE_RIGHT, z);
                }
            } else {
                lastGestureType = GestureType.ROTATE_LEFT;
                if (listener != null) {
                    listener.onGestureDetected(GestureType.ROTATE_LEFT, -z);
                }
            }
        }
    }

    /**
     * Get the last detected gesture type
     * @return The last detected gesture type
     */
    public GestureType getLastGestureType() {
        return lastGestureType;
    }

    /**
     * Reset the gesture detector
     */
    public void reset() {
        lastGestureType = GestureType.NONE;
        lastGestureTime = 0;
    }

    /**
     * Process a double tap event
     */
    public void processDoubleTap() {
        long currentTime = System.currentTimeMillis();

        // Check if enough time has passed since the last gesture
        if (currentTime - lastGestureTime < MIN_TIME_BETWEEN_GESTURES) {
            return;
        }

        lastGestureTime = currentTime;
        lastGestureType = GestureType.DOUBLE_TAP;
        if (listener != null) {
            listener.onGestureDetected(GestureType.DOUBLE_TAP, 1.0f);
        }
    }

    /**
     * Set the sensitivity for gesture detection
     * @param waveSensitivity Sensitivity for wave detection (lower value = more sensitive)
     * @param tiltSensitivity Sensitivity for tilt detection (lower value = more sensitive)
     * @param rotationSensitivity Sensitivity for rotation detection (lower value = more sensitive)
     * @param shakeSensitivity Sensitivity for shake detection (lower value = more sensitive)
     */
    public void setSensitivity(float waveSensitivity, float tiltSensitivity,
                              float rotationSensitivity, float shakeSensitivity) {
        this.waveSensitivity = waveSensitivity;
        this.tiltSensitivity = tiltSensitivity;
        this.rotationSensitivity = rotationSensitivity;
        this.shakeSensitivity = shakeSensitivity;
    }
}
