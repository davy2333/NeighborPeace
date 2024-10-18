package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import sv.edu.catolica.neighborpeace.MainActivity;

public class Edit_profile extends AppCompatActivity {

    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Inicializar la flecha de retroceso
        backArrow = findViewById(R.id.backArrowEditProfile);

        // Configurar el listener para la flecha de retroceso
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la actividad MainActivity
                Intent intent = new Intent(Edit_profile.this, Profile.class);
                startActivity(intent);
            }
        });

        Button btnGuardarCambios = findViewById(R.id.saveChangesButton);

        btnGuardarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent objVentana = new Intent(Edit_profile.this, Profile.class);
                startActivity(objVentana);

            }
        });
    }
}
