package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Encuestas extends AppCompatActivity {

    private LinearLayout layoutOpciones;
    private Button btnAddOpcion;
    private Button btnHacerEncuesta;
    private EditText etPregunta;
    private int opcionContador = 2; // Comenzamos con 2 opciones visibles

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuestas); // Asegúrate que este nombre coincide con tu XML

        // Inicialización de vistas
        layoutOpciones = findViewById(R.id.layout_opciones);
        btnAddOpcion = findViewById(R.id.btn_add_opcion);
        btnHacerEncuesta = findViewById(R.id.btn_hacer_encuesta);
        etPregunta = findViewById(R.id.et_pregunta);
        ImageButton btnBack = findViewById(R.id.btn_back); // Botón de retroceso

        // Agregar acción al botón de añadir opción
        btnAddOpcion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarNuevaOpcion();
            }
        });

        // Agregar acción al botón de crear encuesta
        btnHacerEncuesta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearEncuesta();
            }
        });

        // Configurar el botón de retroceso
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Regresar a la actividad anterior
            }
        });
    }

    // Método para agregar una nueva opción
    private void agregarNuevaOpcion() {
        opcionContador++;
        final int opcionId = opcionContador;

        // Crear un nuevo layout para la opción
        LinearLayout nuevaOpcionLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_encuestas, null);

        // Asignar ID a los elementos dentro del layout inflado
        EditText nuevaOpcionEditText = nuevaOpcionLayout.findViewById(R.id.et_opcion1);
        ImageButton btnEliminarOpcion = nuevaOpcionLayout.findViewById(R.id.btn_eliminar_opcion1);

        // Configurar el botón de eliminar
        btnEliminarOpcion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutOpciones.removeView(nuevaOpcionLayout); // Eliminar la opción
                opcionContador--; // Decrementar el contador
            }
        });

        layoutOpciones.addView(nuevaOpcionLayout); // Agregar la nueva opción al layout
    }

    // Método para crear la encuesta
    private void crearEncuesta() {
        String pregunta = etPregunta.getText().toString().trim();

        if (TextUtils.isEmpty(pregunta)) {
            Toast.makeText(this, "Por favor, ingresa una pregunta.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aquí puedes agregar la lógica para guardar la encuesta en tu base de datos o backend

        Toast.makeText(this, "Encuesta creada exitosamente.", Toast.LENGTH_SHORT).show();

        // Regresar a la pantalla de chat
        Intent intent = new Intent(Encuestas.this, Chat.class);
        startActivity(intent);
        finish(); // Finalizar la actividad actual
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Regresar a la actividad anterior
        Intent intent = new Intent(Encuestas.this, Chat.class);
        startActivity(intent);
        finish(); // Finalizar la actividad actual
    }
}