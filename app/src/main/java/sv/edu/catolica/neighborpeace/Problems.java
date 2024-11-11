package sv.edu.catolica.neighborpeace;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.app.ProgressDialog;

import com.loopj.android.http.*;
import org.json.*;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Problems extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3;

    private static final String BASE_URL = "http://192.168.67.223:80/WebServicesphp/Problema.php";
    private AsyncHttpClient client;

    private ImageView backArrow;
    private ImageView selectedPhotoImageView;
    private Button addPhotoButton;
    private EditText titleEdit, descriptionEdit, locationEdit;
    private String base64Image;
    private int userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_problems);

        // Inicializar el cliente HTTP
        client = new AsyncHttpClient();
        client.setTimeout(30000); // 30 segundos de timeout

        // Manejo de insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.problemsLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        backArrow = findViewById(R.id.backArrowProblems);
        selectedPhotoImageView = findViewById(R.id.selectedPhotoImageView);
        addPhotoButton = findViewById(R.id.addPhotoButton);
        titleEdit = findViewById(R.id.titleEdit);
        descriptionEdit = findViewById(R.id.descriptionEdit);
        locationEdit = findViewById(R.id.locationEdit);

        // Botón de retroceso
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Problems.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Obtener el ID del usuario de las preferencias compartidas
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        // Botón para publicar el problema
        Button submitProblemButton = findViewById(R.id.submitProblemButton);
        submitProblemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    submitProblem();
                }
            }
        });

        // Agregar foto
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSelectionDialog();
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
    private boolean validateInputs() {
        String title = titleEdit.getText().toString().trim();
        String description = descriptionEdit.getText().toString().trim();
        String location = locationEdit.getText().toString().trim();

        if (title.isEmpty()) {
            titleEdit.setError("El título es requerido");
            return false;
        }

        if (description.isEmpty()) {
            descriptionEdit.setError("La descripción es requerida");
            return false;
        }

        if (location.isEmpty()) {
            locationEdit.setError("La ubicación es requerida");
            return false;
        }

        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void submitProblem() {
        // Crear el objeto JSON con los datos del problema
        JSONObject params = new JSONObject();
        try {
            params.put("titulo", titleEdit.getText().toString().trim());
            params.put("descripcion", descriptionEdit.getText().toString().trim());
            params.put("ubicacion", locationEdit.getText().toString().trim());
            params.put("idUsuario", userId);

            // Mostrar diálogo de carga
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Enviando problema...");
            progressDialog.show();

            // Crear StringEntity para el envío
            StringEntity entity = new StringEntity(params.toString());

            // Configurar el cliente para enviar JSON
            client.addHeader("Content-Type", "application/json");

            // Realizar la petición POST
            client.post(this, BASE_URL, entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progressDialog.dismiss();
                    try {
                        if (response.has("success") && response.getBoolean("success")) {
                            Toast.makeText(Problems.this, "Problema creado exitosamente", Toast.LENGTH_LONG).show();
                            // Redirigir a MainActivity
                            Intent intent = new Intent(Problems.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String error = response.has("error") ? response.getString("error") : "Error desconocido";
                            Toast.makeText(Problems.this, error, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(Problems.this, "Error al procesar la respuesta", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    progressDialog.dismiss();
                    Toast.makeText(Problems.this, "Error de conexión: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error al crear la solicitud: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Problems.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showImageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar imagen");
        builder.setMessage("¿Dónde deseas seleccionar la imagen?");
        builder.setPositiveButton("Tomar foto", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                takePicture();
            }
        });
        builder.setNegativeButton("Seleccionar de galería", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pickImage();
            }
        });
        builder.show();
    }

    private void takePicture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void pickImage() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            selectedPhotoImageView.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            selectedPhotoImageView.setImageURI(selectedImageUri);
        }
    }
}