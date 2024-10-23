package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Problems extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_problems); // Asegúrate de que el layout esté correctamente referenciado.

        // Manejo de insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.problemsLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Botón de retroceso
        ImageView backArrow = findViewById(R.id.backArrowProblems);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Problems.this, MainActivity.class);
                startActivity(intent);
                finish(); // Terminar la actividad actual si lo deseas
            }
        });

        // Botón para publicar el problema
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
