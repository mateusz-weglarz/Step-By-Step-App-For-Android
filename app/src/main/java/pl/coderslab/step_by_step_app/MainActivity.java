package pl.coderslab.step_by_step_app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import static pl.coderslab.step_by_step_app.LoginActivity.CREDENTIALS;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView count_steps;
    private Button startButton;
    private Button pauseButton;
    private Button endButton;
    private int stepCount;
    private Float initialStep;
    private boolean isSensorActive = false;
    private final int ACTIVITY_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        count_steps = findViewById(R.id.count_steps);
        startButton = findViewById(R.id.startButton);
        pauseButton = findViewById(R.id.pauseButton);
        endButton = findViewById(R.id.endButton);


        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestActivityRecognitionPermission();
        }

        startButton.setOnClickListener(v -> {
            Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (countSensor != null) {
                startButton.setEnabled(false);
                pauseButton.setEnabled(true);
                endButton.setEnabled(true);
                sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_FASTEST);
                isSensorActive = true;
            } else {
                Toast.makeText(this, "Nie znaleziono sensora.", Toast.LENGTH_SHORT).show();
            }
        });

        pauseButton.setOnClickListener(v -> {
            startButton.setEnabled(true);
            pauseButton.setEnabled(false);
            sensorManager.unregisterListener(this);
            isSensorActive = false;
        });

        endButton.setOnClickListener(v -> {
            endButton.setEnabled(false);
            try {
                sendRequest();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    private void requestActivityRecognitionPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACTIVITY_RECOGNITION)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed for application to run.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, ACTIVITY_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, ACTIVITY_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ACTIVITY_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float eventStepCount = event.values[0];
        if (initialStep == null) {
            initialStep = eventStepCount;
        } else {
            stepCount = Math.round(eventStepCount - initialStep);
        }
        count_steps.setText(String.valueOf(stepCount));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void sendRequest() throws JSONException {
        StringRequest jsonObjectRequest = new StringRequest(
                Request.Method.POST, MainActivity.this.getString(R.string.create_activity_url), response -> {
            initialStep = null;
            stepCount = 0;
            startButton.setEnabled(true);
            pauseButton.setEnabled(false);
            if (isSensorActive) {
                sensorManager.unregisterListener(MainActivity.this);
            }
        }, error -> {
            Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            endButton.setEnabled(true);
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("numberOfSteps", String.valueOf(stepCount));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences(MainActivity.this.getString(R.string.preferences), MODE_PRIVATE);
                Map<String, String> headers = new HashMap<>();
                String credentials = sharedPreferences.getString(CREDENTIALS, "");
                headers.put("Authorization", "Basic " + credentials);
                return headers;
            }
        };
        HttpRequestSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}