package pl.coderslab.step_by_step_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    public static final String CREDENTIALS = "credentials";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        SharedPreferences sharedPreferences = LoginActivity.this.getSharedPreferences(LoginActivity.this.getString(R.string.preferences), MODE_PRIVATE);
        if (sharedPreferences.contains(CREDENTIALS)) {
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_login);
            final EditText usernameEditText = findViewById(R.id.username);
            final EditText passwordEditText = findViewById(R.id.password);
            final Button loginButton = findViewById(R.id.login);


            loginButton.setOnClickListener(v -> {
                SharedPreferences.Editor edit = sharedPreferences.edit();
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                edit.putString(CREDENTIALS, Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));

                edit.apply();
                startActivity(intent);
            });
        }
    }
}