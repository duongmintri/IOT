package com.example.btn3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    // UI Components
    private SeekBar waveSensitivitySeekBar;
    private SeekBar tiltSensitivitySeekBar;
    private SeekBar rotateSensitivitySeekBar;
    private SeekBar shakeSensitivitySeekBar;
    private Spinner waveActionSpinner;
    private Spinner tiltActionSpinner;
    private Spinner rotateActionSpinner;
    private Spinner shakeActionSpinner;
    private Button saveSettingsButton;

    // Shared preferences for storing settings
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "GestureControlPrefs";
    
    // Keys for shared preferences
    private static final String KEY_WAVE_SENSITIVITY = "wave_sensitivity";
    private static final String KEY_TILT_SENSITIVITY = "tilt_sensitivity";
    private static final String KEY_ROTATE_SENSITIVITY = "rotate_sensitivity";
    private static final String KEY_SHAKE_SENSITIVITY = "shake_sensitivity";
    private static final String KEY_WAVE_ACTION = "wave_action";
    private static final String KEY_TILT_ACTION = "tilt_action";
    private static final String KEY_ROTATE_ACTION = "rotate_action";
    private static final String KEY_SHAKE_ACTION = "shake_action";
    
    // Default values
    private static final int DEFAULT_SENSITIVITY = 50;
    private static final int DEFAULT_ACTION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings_title);
        }
        
        // Initialize shared preferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Initialize UI components
        initializeUIComponents();
        
        // Load saved settings
        loadSettings();
        
        // Set up button click listeners
        setupButtonListeners();
    }
    
    private void initializeUIComponents() {
        waveSensitivitySeekBar = findViewById(R.id.waveSensitivitySeekBar);
        tiltSensitivitySeekBar = findViewById(R.id.tiltSensitivitySeekBar);
        rotateSensitivitySeekBar = findViewById(R.id.rotateSensitivitySeekBar);
        shakeSensitivitySeekBar = findViewById(R.id.shakeSensitivitySeekBar);
        waveActionSpinner = findViewById(R.id.waveActionSpinner);
        tiltActionSpinner = findViewById(R.id.tiltActionSpinner);
        rotateActionSpinner = findViewById(R.id.rotateActionSpinner);
        shakeActionSpinner = findViewById(R.id.shakeActionSpinner);
        saveSettingsButton = findViewById(R.id.saveSettingsButton);
        
        // Set up spinners with action options
        setupSpinners();
    }
    
    private void setupSpinners() {
        // Create an array adapter for the action spinners
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, 
                R.array.action_options, 
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        // Set the adapter for each spinner
        waveActionSpinner.setAdapter(adapter);
        tiltActionSpinner.setAdapter(adapter);
        rotateActionSpinner.setAdapter(adapter);
        shakeActionSpinner.setAdapter(adapter);
    }
    
    private void setupButtonListeners() {
        saveSettingsButton.setOnClickListener(v -> saveSettings());
    }
    
    private void loadSettings() {
        // Load sensitivity settings
        waveSensitivitySeekBar.setProgress(
                sharedPreferences.getInt(KEY_WAVE_SENSITIVITY, DEFAULT_SENSITIVITY));
        tiltSensitivitySeekBar.setProgress(
                sharedPreferences.getInt(KEY_TILT_SENSITIVITY, DEFAULT_SENSITIVITY));
        rotateSensitivitySeekBar.setProgress(
                sharedPreferences.getInt(KEY_ROTATE_SENSITIVITY, DEFAULT_SENSITIVITY));
        shakeSensitivitySeekBar.setProgress(
                sharedPreferences.getInt(KEY_SHAKE_SENSITIVITY, DEFAULT_SENSITIVITY));
        
        // Load action settings
        waveActionSpinner.setSelection(
                sharedPreferences.getInt(KEY_WAVE_ACTION, DEFAULT_ACTION));
        tiltActionSpinner.setSelection(
                sharedPreferences.getInt(KEY_TILT_ACTION, DEFAULT_ACTION));
        rotateActionSpinner.setSelection(
                sharedPreferences.getInt(KEY_ROTATE_ACTION, DEFAULT_ACTION));
        shakeActionSpinner.setSelection(
                sharedPreferences.getInt(KEY_SHAKE_ACTION, DEFAULT_ACTION));
    }
    
    private void saveSettings() {
        // Get the editor for shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        // Save sensitivity settings
        editor.putInt(KEY_WAVE_SENSITIVITY, waveSensitivitySeekBar.getProgress());
        editor.putInt(KEY_TILT_SENSITIVITY, tiltSensitivitySeekBar.getProgress());
        editor.putInt(KEY_ROTATE_SENSITIVITY, rotateSensitivitySeekBar.getProgress());
        editor.putInt(KEY_SHAKE_SENSITIVITY, shakeSensitivitySeekBar.getProgress());
        
        // Save action settings
        editor.putInt(KEY_WAVE_ACTION, waveActionSpinner.getSelectedItemPosition());
        editor.putInt(KEY_TILT_ACTION, tiltActionSpinner.getSelectedItemPosition());
        editor.putInt(KEY_ROTATE_ACTION, rotateActionSpinner.getSelectedItemPosition());
        editor.putInt(KEY_SHAKE_ACTION, shakeActionSpinner.getSelectedItemPosition());
        
        // Apply changes
        editor.apply();
        
        // Show success message
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
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
    
    /**
     * Convert seekbar progress (0-100) to sensitivity value (0.5-1.5)
     * Lower value = more sensitive
     */
    public static float progressToSensitivity(int progress) {
        // Map 0-100 to 0.5-1.5 (inverted, so lower progress = higher sensitivity)
        return 1.5f - (progress / 100.0f);
    }
    
    /**
     * Get the action for a gesture from shared preferences
     */
    public static int getActionForGesture(SharedPreferences prefs, String gestureKey) {
        return prefs.getInt(gestureKey, DEFAULT_ACTION);
    }
}
