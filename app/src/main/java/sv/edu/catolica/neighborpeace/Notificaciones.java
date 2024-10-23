package sv.edu.catolica.neighborpeace;

import static sv.edu.catolica.neighborpeace.R.id.navigation_home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Notificaciones extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notificaciones);

        // Manejo de los Insets para evitar superposición con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Agregando el menú de navegación inferior
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.navigation_home) {
                    Intent pantalla = new Intent(Notificaciones.this, MainActivity.class);
                    startActivity(pantalla);
                } else if (id == R.id.navigation_add_problem) {
                    Intent pantalla = new Intent(Notificaciones.this, Problems.class);
                    startActivity(pantalla);
                } else if (id == R.id.navigation_history) {
                    Intent pantalla = new Intent(Notificaciones.this, History.class);
                    startActivity(pantalla);
                } else if (id == R.id.navigation_notifications) {
                    Toast toast1 = Toast.makeText(getApplicationContext(),
                            "Estás en la pantalla notificaciones", Toast.LENGTH_SHORT);
                    toast1.show();
                }

                return true;
            }
        });

        // Configuración para la lista de notificaciones
        ListView notificationsListView = findViewById(R.id.notificationsListView);
        // Aquí puedes configurar tu adaptador y los datos para la lista de notificaciones
    }

    // Sobrescribir el comportamiento del botón de retroceso para ir a la pantalla principal (Home)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Notificaciones.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
