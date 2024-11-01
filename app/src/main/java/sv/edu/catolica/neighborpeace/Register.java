package sv.edu.catolica.neighborpeace;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Register extends AppCompatActivity {

    private ImageView backArrow;
    private EditText nombreEditText, emailEditText, passwordEditText, confirmPasswordEditText, direccionEditText, telefonoEditText;
    private CheckBox termsCheckBox;
    private Button registrarseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar vistas
        backArrow = findViewById(R.id.backArrow);
        nombreEditText = findViewById(R.id.nombreEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        direccionEditText = findViewById(R.id.direccionEditText);
        telefonoEditText = findViewById(R.id.telefonoEditText);
        termsCheckBox = findViewById(R.id.terminosCheckBox);
        registrarseButton = findViewById(R.id.registrarseButton);

        // Inicialmente ocultar el botón de registro
        registrarseButton.setVisibility(View.GONE);

        // Configurar el CheckBox para mostrar/ocultar el botón
        termsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                registrarseButton.setVisibility(isChecked ? View.VISIBLE : View.GONE);
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

        registrarseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    Intent intent = new Intent(Register.this, login.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
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
                registrarseButton.setVisibility(View.VISIBLE);
            }
        });

        builder.setNegativeButton("Rechazar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                termsCheckBox.setChecked(false);
                registrarseButton.setVisibility(View.GONE);
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                termsCheckBox.setChecked(false);
                registrarseButton.setVisibility(View.GONE);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean validateInputs() {
        if (nombreEditText.getText().toString().trim().isEmpty()) {
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
        if (direccionEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa tu dirección", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (telefonoEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa tu teléfono", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}