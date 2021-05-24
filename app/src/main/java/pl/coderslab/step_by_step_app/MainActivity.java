package pl.coderslab.step_by_step_app;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView count_steps;
    private Button startButton;
    private Button endButton;
    private float stepCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        count_steps = findViewById(R.id.count_steps);
        startButton = findViewById(R.id.startButton);
        endButton = findViewById(R.id.endButton);
        startButton.setOnClickListener(v -> {
            Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (countSensor != null) {
                startButton.setEnabled(false);
                endButton.setEnabled(true);
                sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_FASTEST);
            } else {
                Toast.makeText(this, "Nie znaleziono sensora.", Toast.LENGTH_SHORT).show();
            }
        });
        endButton.setOnClickListener(v -> {
            startButton.setEnabled(true);
            endButton.setEnabled(false);
            sensorManager.unregisterListener(this);
            try {
                sendRequest();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        stepCount = event.values[0];
        count_steps.setText(String.valueOf(event.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void sendRequest() throws JSONException {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.PUT, "https://mateuszweglarz.free.beeceptor.com/test", new JSONObject().put("stepCount",stepCount), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(MainActivity.this, "Odpowiedź.", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}