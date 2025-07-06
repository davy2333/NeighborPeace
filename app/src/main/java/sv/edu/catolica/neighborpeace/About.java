package sv.edu.catolica.neighborpeace;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

public class About extends AppCompatActivity {

    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Ocultar la barra de acción
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Inicializar el botón de regreso
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());
    }
}