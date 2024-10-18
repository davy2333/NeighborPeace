package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class Problems extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problems); // Asegúrate de que el layout esté correctamente referenciado.

        // Configura el botón de flecha para regresar al MainActivity
        Button backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Problems.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Configura el botón de publicar problema
        Button submitProblemButton = findViewById(R.id.submitProblemButton);
        submitProblemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes agregar la lógica para manejar la publicación del problema
                // Por ahora, simplemente haremos un Toast o redirigir a otra pantalla si es necesario
            }
        });
    }
}
