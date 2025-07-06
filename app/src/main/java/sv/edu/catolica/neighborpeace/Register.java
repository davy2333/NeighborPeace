package sv.edu.catolica.neighborpeace;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Register extends AppCompatActivity {

    private ImageView backArrow;
    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText, addressEditText, phoneEditText;
    private CheckBox termsCheckBox;
    private Button registerButton;
    private static final String TAG = "Register";
    private static final String REGISTER_URL = "http://192.168.0.12:80/WebServicesphp/registro.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar vistas
        backArrow = findViewById(R.id.backArrow);
        nameEditText = findViewById(R.id.nombreEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        addressEditText = findViewById(R.id.direccionEditText);
        phoneEditText = findViewById(R.id.telefonoEditText);
        termsCheckBox = findViewById(R.id.terminosCheckBox);
        registerButton = findViewById(R.id.registrarseButton);

        // Inicialmente ocultar el botón de registro
        registerButton.setVisibility(View.GONE);

        // Configurar el CheckBox para mostrar/ocultar el botón
        termsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                registerButton.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        termsCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTermsDialog();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    registerUser();
                }
            }
        });
    }

    private boolean validateInputs() {
        if (nameEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa tu nombre", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (emailEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa tu email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (passwordEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una contraseña", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (addressEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa tu dirección", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (phoneEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa tu teléfono", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        RequestParams params = new RequestParams();
        params.put("nombre", name);
        params.put("email", email);
        params.put("contrasena", password);
        params.put("confirmar_contrasena", password);
        params.put("direccion", address);
        params.put("telefono", phone);

        Log.d(TAG, "Iniciando registro en: " + REGISTER_URL);
        Log.d(TAG, "Parámetros: " + params.toString());

        AsyncHttpClient client = new AsyncHttpClient();
        client.setConnectTimeout(5000);
        client.setResponseTimeout(10000);
        client.addHeader("Accept", "application/json");

        client.post(REGISTER_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String response = new String(responseBody, "UTF-8");
                    Log.d(TAG, "Respuesta del servidor: " + response);

                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.has("exito")) {
                        Toast.makeText(Register.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                        navigateToLoginScreen();
                    } else if (jsonResponse.has("error")) {
                        String errorMessage = jsonResponse.getString("error");
                        Toast.makeText(Register.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error de JSON: " + e.getMessage());
                    Log.e(TAG, "Respuesta que causó el error: " + new String(responseBody));
                    Toast.makeText(Register.this, "Error procesando la respuesta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error general: " + e.getMessage());
                    Toast.makeText(Register.this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(Register.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToLoginScreen() {
        Intent intent = new Intent(Register.this, login.class);
        startActivity(intent);
        finish();
    }

    private void showTermsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Términos y Condiciones");
        builder.setMessage(
                "Bienvenido a NeighborPeace\n\n" +
                        "Al usar esta aplicación, aceptas los siguientes términos:\n\n" +
                        "1. Uso Responsable\n" +
                        "- Te comprometes a usar la aplicación de manera responsable\n" +
                        "- No publicarás contenido falso o malicioso\n\n" +
                        "2. Privacidad\n" +
                        "- Respetaremos tu privacidad según nuestra política\n" +
                        "- Tus datos serán tratados de forma segura\n\n" +
                        "3. Contenido\n" +
                        "- El contenido debe ser apropiado y relevante\n" +
                        "- No se permite contenido ofensivo o ilegal\n\n" +
                        "4. Responsabilidad\n" +
                        "- No nos hacemos responsables del mal uso de la aplicación\n" +
                        "- Los usuarios son responsables de sus acciones"
        );

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                termsCheckBox.setChecked(true);
                registerButton.setVisibility(View.VISIBLE);
            }
        });

        builder.setNegativeButton("Rechazar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                termsCheckBox.setChecked(false);
                registerButton.setVisibility(View.GONE);
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                termsCheckBox.setChecked(false);
                registerButton.setVisibility(View.GONE);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}