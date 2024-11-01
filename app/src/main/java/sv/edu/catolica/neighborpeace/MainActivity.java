package sv.edu.catolica.neighborpeace;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main); // Asegúrate de que este archivo XML exista.

        // Manejo de insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configuración del BottomNavigationView
        setupBottomNavigationView();

        // Configuración del botón de perfil
        setupProfileButton();

        // Configuración del botón de Chat
        setupChatButton();
    }

    // Método para configurar el BottomNavigationView
    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation); // Verifica que este ID esté en tu XML.

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                // Manejo de la selección de ítems
                if (id == R.id.navigation_home) {
                    if (!(MainActivity.this instanceof MainActivity)) {
                        Intent pantalla = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(pantalla);
                    } else {
                        Toast.makeText(MainActivity.this, "Ya estás en la pantalla principal", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else if (id == R.id.navigation_add_problem) {
                    Intent pantalla = new Intent(MainActivity.this, Problems.class);
                    startActivity(pantalla);
                    return true;
                } else if (id == R.id.navigation_history) {
                    Intent pantalla = new Intent(MainActivity.this, History.class);
                    startActivity(pantalla);
                    return true;
                } else if (id == R.id.navigation_notifications) {
                    Intent pantalla = new Intent(MainActivity.this, Notificaciones.class);
                    startActivity(pantalla);
                    return true;
                }

                return false;
            }
        });
    }

    // Método para configurar el botón de perfil
    private void setupProfileButton() {
        ImageButton btnPerfil = findViewById(R.id.profileButton); // Verifica que este ID esté en tu XML.
        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Profile.class)); // Asegúrate de que Profile.class esté definida.
            }
        });
    }

    // Método para configurar el botón de Chat
    private void setupChatButton() {
        Button chatButton = findViewById(R.id.chatButton); // Verifica que este ID esté en tu XML.
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Chat.class)); // Asegúrate de que Chat.class esté definida.
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            // Confirmar salida cuando el usuario está en Home y presiona atrás
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar Salida")
                    .setMessage("¿Estás seguro de que quieres salir?")
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish(); // Cierra la aplicación
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            // Retroceso a la actividad anterior sin cerrar la aplicación
            super.onBackPressed();
        }
    }
}
