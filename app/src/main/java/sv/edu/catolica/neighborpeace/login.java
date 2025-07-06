package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView, forgotPasswordTextView;
    private static final String TAG = "LoginActivity";
    private static final String LOGIN_URL = "http://192.168.0.12:80/WebServicesphp/login.php";
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordText);

        // Botón de "Registrarse" para ir a la actividad RegisterActivity
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, Register.class);
                startActivity(intent);
            }
        });

        // Botón de "Olvidé mi Contraseña" para ir a la actividad ForgotPassword
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

        // Botón de "Iniciar Sesión" para validar el usuario y contraseña
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    loginUser();
                }
            }
        });
    }

    private boolean validateInputs() {
        if (emailEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa tu email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (passwordEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa tu contraseña", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("password", password);

        Log.d(TAG, "Iniciando login en: " + LOGIN_URL);
        Log.d(TAG, "Parámetros: " + params.toString());

        AsyncHttpClient client = new AsyncHttpClient();
        client.setConnectTimeout(5000);
        client.setResponseTimeout(10000);
        client.addHeader("Accept", "application/json");

        client.post(LOGIN_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String response = new String(responseBody, "UTF-8");
                    Log.d(TAG, "Respuesta del servidor: " + response);

                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.has("exito")) {
                        // Obtener todos los datos necesarios
                        String username = jsonResponse.getString("usuario");
                        String userType = jsonResponse.getString("tipo");
                        int userTypeId = jsonResponse.getInt("id_tipo");
                        int userId = jsonResponse.getInt("id");

                        // Guardar datos en SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("userId", userId);
                        editor.putString("userName", username);
                        editor.putString("userType", userType);
                        editor.putInt("userTypeId", userTypeId);
                        editor.apply();

                        // Log para depuración de tipo de usuario
                        Log.d(TAG, "Tipo de usuario desde el servidor: " + userType);

                        // Crear intent y redirigir según el tipo de usuario
                        Intent intent;
                        if ("ADMIN".equals(userType)) {
                            intent = new Intent(login.this, MainActivityAdmin.class);
                        } else {
                            intent = new Intent(login.this, MainActivity.class);
                        }
                        intent.putExtra("nombre_usuario", username);
                        intent.putExtra("tipo_usuario", userType);
                        intent.putExtra("id_tipo", userTypeId);
                        intent.putExtra("user_id", userId);

                        startActivity(intent);
                        finish();
                    } else if (jsonResponse.has("error")) {
                        String errorMessage = jsonResponse.getString("error");
                        Toast.makeText(login.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error de JSON: " + e.getMessage());
                    Log.e(TAG, "Respuesta que causó el error: " + new String(responseBody));
                    Toast.makeText(login.this, "Error procesando la respuesta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error general: " + e.getMessage());
                    Toast.makeText(login.this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String errorMessage = "Error de conexión - Status: " + statusCode;
                if (responseBody != null) {
                    errorMessage += "\nRespuesta: " + new String(responseBody);
                }
                Log.e(TAG, errorMessage);
                Log.e(TAG, "Error detallado: ", error);
                Toast.makeText(login.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        new AlertDialog.Builder(this)
                .setMessage("¿Quieres salir de la aplicación?")
                .setCancelable(false)
                .setPositiveButton("Sí", (dialog, id) -> login.super.onBackPressed())
                .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
                .show();

        new android.os.Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}
