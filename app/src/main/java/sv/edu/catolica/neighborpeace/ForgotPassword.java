package sv.edu.catolica.neighborpeace; // Cambia esto por el nombre de tu paquete

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPassword extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private Button backToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Inicializar los elementos de la interfaz
        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);

        // Configurar el evento de clic para el botón de reseteo de contraseña
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptResetPassword();
            }
        });

        // Configurar el evento de clic para el botón de volver al login
        backToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Cierra esta actividad y vuelve a la anterior (Login)
            }
        });
    }

    private void attemptResetPassword() {
        // Resetear errores
        emailEditText.setError(null);

        // Obtener el email ingresado
        String email = emailEditText.getText().toString().trim();

        // Verificar si el email está vacío
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_campo_requerido));
            emailEditText.requestFocus();
            return;
        }

        // Verificar si el email es válido
        if (!isEmailValid(email)) {
            emailEditText.setError(getString(R.string.error_email_invalido));
            emailEditText.requestFocus();
            return;
        }

        // Aquí iría la lógica para enviar el correo de recuperación
        // Por ahora, solo mostraremos un mensaje
        Toast.makeText(this, "Se ha enviado un correo de recuperación a " + email, Toast.LENGTH_LONG).show();

        // Volver a la pantalla de login después de un breve delay
        emailEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000); // 2 segundos de delay
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}