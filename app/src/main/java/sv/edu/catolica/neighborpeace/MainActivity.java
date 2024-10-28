package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;  // Importación del botón
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Asegúrate de que esté cargando el layout correcto
        // Aquí está el ID 'main' del ConstraintLayout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Listener para manejar los clics en los ítems del menú
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.navigation_home) {
                    Toast toast1 = Toast.makeText(getApplicationContext(),
                            "Estás en la pantalla principal", Toast.LENGTH_SHORT);
                    toast1.show();

                } else if (id == R.id.navigation_add_problem) {
                    Intent pantalla = new Intent(MainActivity.this, Problems.class);
                    startActivity(pantalla);
                } else if (id == R.id.navigation_history) {
                    Intent pantalla = new Intent(MainActivity.this, History.class);
                    startActivity(pantalla);
                } else if (id == R.id.navigation_notifications) {
                    Intent pantalla = new Intent(MainActivity.this, Notificaciones.class);
                    startActivity(pantalla);
                }

                return true;
            }
        });

        // Configuración del botón de perfil
        ImageButton btnPerfil = findViewById(R.id.profileButton);

        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent objVentana = new Intent(MainActivity.this, Profile.class);
                startActivity(objVentana);
            }
        });

        // Configuración del botón de Chat
        Button chatButton = findViewById(R.id.chatButton);  // Encuentra el botón de chat por su ID
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inicia la actividad de Chat cuando se presiona el botón
                Intent intent = new Intent(MainActivity.this, Chat.class); // Asegúrate de que Chat.class exista
                startActivity(intent);
            }
        });
    }
}