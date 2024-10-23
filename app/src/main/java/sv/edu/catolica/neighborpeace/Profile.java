package sv.edu.catolica.neighborpeace;import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import sv.edu.catolica.neighborpeace.Edit_profile;

public class Profile extends AppCompatActivity {

    private ImageView backArrow, profileImage;
    private TextView homeText, titleProfile, emailValue, nameValue, locationValue, phoneValue;
    private Button editProfileButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inicializar vistas
        backArrow = findViewById(R.id.backArrow);

        titleProfile = findViewById(R.id.titleProfile);
        profileImage = findViewById(R.id.profileImage);
        emailValue = findViewById(R.id.emailValue);
        nameValue = findViewById(R.id.nameValue);
        locationValue = findViewById(R.id.locationValue);
        phoneValue = findViewById(R.id.phoneValue);
        editProfileButton = findViewById(R.id.editProfileButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Configurar los datos del perfil (puedes cambiar estos valores por los obtenidos de tu backend o base de datos)
        emailValue.setText("walterflores@gmail.com");
        nameValue.setText("Walter Flores");
        locationValue.setText("Santa Ana");
        phoneValue.setText("7089 5467");

        // Acción de regresar al pulsar la flecha
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();  // Regresa a la actividad anterior
            }
        });

        // Configurar el botón para editar el perfil
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para abrir la pantalla de edición de perfil
                    Intent intent = new Intent(Profile.this, Edit_profile.class);
                startActivity(intent);
            }
        });

        // Configurar el botón de cerrar sesión
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para cerrar sesión (puedes agregar la lógica necesaria aquí)
                finish();  // Termina la actividad actual
            }
        });
    }
}
