package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ImageButton chatButton;
    private ImageButton profileButton; // Botón de perfil
    private TextView userNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        chatButton = findViewById(R.id.chatButton);
        profileButton = findViewById(R.id.profileButton); // Inicializar el botón de perfil
        userNameTextView = findViewById(R.id.userNameTextView);

        // Configurar el botón de chat
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar la actividad de chat
                Intent chatIntent = new Intent(MainActivity.this, Chat.class);
                startActivity(chatIntent);
            }
        });

        // Configurar el botón de perfil
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar la actividad de perfil
                Intent profileIntent = new Intent(MainActivity.this, Profile.class);
                startActivity(profileIntent);
            }
        });

        // Configurar el menú de navegación inferior
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.navigation_home) {
                    // Ya estamos en la pantalla principal
                    return true;
                } else if (id == R.id.navigation_add_problem) {
                    Intent addProblemIntent = new Intent(MainActivity.this, Problems.class);
                    startActivity(addProblemIntent);
                    return true;
                } else if (id == R.id.navigation_history) {
                    Intent historyIntent = new Intent(MainActivity.this, History.class);
                    startActivity(historyIntent);
                    return true;
                } else if (id == R.id.navigation_notifications) {
                    Intent notificationsIntent = new Intent(MainActivity.this, Notificaciones.class);
                    startActivity(notificationsIntent);
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(); // Cierra la actividad actual
    }
}