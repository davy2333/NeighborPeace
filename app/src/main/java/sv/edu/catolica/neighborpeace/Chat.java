package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Chat extends AppCompatActivity {

    private LinearLayout messageContainer;
    private EditText messageInput;
    private Button sendButton;
    private Button optionsButton;
    private ImageView backButton;
    private ScrollView messageScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat); // Asegúrate de que este nombre coincide con tu XML

        // Inicializar vistas
        messageContainer = findViewById(R.id.message_container);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        optionsButton = findViewById(R.id.options_button);
        backButton = findViewById(R.id.back_button);
        messageScrollView = findViewById(R.id.message_scroll_view);

        // Configurar el botón de enviar
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensaje();
            }
        });

        // Configurar el botón de opciones
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irAEncuestas(); // Navegar a la pantalla de encuestas
            }
        });

        // Configurar el botón de retroceso
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irAPantallaPrincipal(); // Navegar a la pantalla principal
            }
        });
    }

    // Método para enviar un mensaje
    private void enviarMensaje() {
        String mensaje = messageInput.getText().toString().trim();
        if (!mensaje.isEmpty()) {
            // Crear un nuevo TextView para mostrar el mensaje
            TextView nuevoMensaje = new TextView(this);
            nuevoMensaje.setText(mensaje);
            messageContainer.addView(nuevoMensaje); // Agregar el mensaje al contenedor
            messageInput.setText(""); // Limpiar el campo de entrada
            // Desplazar el ScrollView hacia abajo para mostrar el nuevo mensaje
            messageScrollView.post(() -> messageScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    // Método para ir a la pantalla de encuestas
    private void irAEncuestas() {
        Intent intent = new Intent(Chat.this, Encuestas.class);
        startActivity(intent);
    }

    // Método para ir a la pantalla principal
    private void irAPantallaPrincipal() {
        Intent intent = new Intent(Chat.this, MainActivity.class); // Asegúrate de que MainActivity sea la pantalla principal
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Limpia la pila de actividades
        startActivity(intent);
        finish(); // Finalizar la actividad actual
    }

    // Manejar el gesto de retroceso
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        irAPantallaPrincipal(); // Regresar a la pantalla principal sin necesidad de llamar a super
    }
}
