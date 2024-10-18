package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
        backArrow = findViewById(R.id.backArrow);

        // Configurar el listener para la flecha de retroceso
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la actividad MainActivity
                Intent intent = new Intent(Edit_profile.this, MainActivity.class);
                startActivity(intent);
                // Finalizar la actividad actual para que no quede en la pila de actividades
                finish();
            }
        });
    }
}
