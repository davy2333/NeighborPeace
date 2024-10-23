package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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
                // Lógica para manejar la publicación del problema
                Toast.makeText(Problems.this, "Problema publicado", Toast.LENGTH_SHORT).show();
            }
        });

        // Agregando el menú de navegación inferior
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.navigation_home) {
                    Intent pantalla = new Intent(Problems.this, MainActivity.class);
                    startActivity(pantalla);
                } else if (id == R.id.navigation_add_problem) {
                    Toast.makeText(Problems.this, "Estás en la pantalla de Problemas", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.navigation_history) {
                    Intent pantalla = new Intent(Problems.this, History.class);
                    startActivity(pantalla);
                } else if (id == R.id.navigation_notifications) {
                    Intent pantalla = new Intent(Problems.this, Notificaciones.class);
                    startActivity(pantalla);
                }

                return true;
            }
        });
    }
}
