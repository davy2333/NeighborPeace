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
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Toast;

import android.app.ProgressDialog;

import com.bumptech.glide.Glide;
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

import java.io.ByteArrayOutputStream;

public class Problems extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3;

    private static final String BASE_URL = "http://192.168.1.125:80/WebServicesphp/Problema.php";
    private AsyncHttpClient client;

    private ImageView backArrow;
    private ImageView selectedPhotoImageView;
    private ImageView problemIconPreview;
    private Button addPhotoButton, selectIconButton, submitProblemButton;
    private EditText titleEdit, descriptionEdit, locationEdit;
    private String base64Image;
    private String selectedIcon = "default"; // Valor por defecto para el icono
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_problems);

        initializeViews();
        setupClient();
        setupListeners();
        setupBottomNavigation();
        getUserId();
    }

    private void initializeViews() {
        backArrow = findViewById(R.id.backArrowProblems);
        selectedPhotoImageView = findViewById(R.id.selectedPhotoImageView);
        problemIconPreview = findViewById(R.id.problemIconPreview);
        addPhotoButton = findViewById(R.id.addPhotoButton);
        selectIconButton = findViewById(R.id.addIconButton);
        submitProblemButton = findViewById(R.id.submitProblemButton);
        titleEdit = findViewById(R.id.titleEdit);
        descriptionEdit = findViewById(R.id.descriptionEdit);
        locationEdit = findViewById(R.id.locationEdit);

        // Configurar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.problemsLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupClient() {
        client = new AsyncHttpClient();
        client.setTimeout(30000);
    }

    private void getUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);
    }

    private void setupListeners() {
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(Problems.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        submitProblemButton.setOnClickListener(v -> {
            if (validateInputs()) {
                submitProblem();
            }
        });

        addPhotoButton.setOnClickListener(v -> showImageSelectionDialog());

        selectIconButton.setOnClickListener(v -> showIconSelector());
    }

    private void showIconSelector() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.layout_icon_selector, null);
        builder.setView(view);
        builder.setTitle("Seleccionar ícono para el problema");

        AlertDialog dialog = builder.create();

        // Configurar listeners para cada icono
        view.findViewById(R.id.problemas_electricos).setOnClickListener(v -> {
            selectedIcon = "ic_water";
            problemIconPreview.setImageResource(R.drawable.problemas_electricos);
            dialog.dismiss();
        });

        view.findViewById(R.id.problemas_tuberias).setOnClickListener(v -> {
            selectedIcon = "ic_electricity";
            problemIconPreview.setImageResource(R.drawable.problemas_en_tuberias);
            dialog.dismiss();
        });

        view.findViewById(R.id.problemas_viales).setOnClickListener(v -> {
            selectedIcon = "ic_road";
            problemIconPreview.setImageResource(R.drawable.problemas_viales);
            dialog.dismiss();
        });

        // Puedes agregar más iconos aquí siguiendo el mismo patrón

        dialog.show();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                startActivity(new Intent(Problems.this, MainActivity.class));
                return true;
            } else if (id == R.id.navigation_add_problem) {
                Toast.makeText(Problems.this, "Estás en la pantalla de Problemas", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.navigation_history) {
                startActivity(new Intent(Problems.this, History.class));
                return true;
            } else if (id == R.id.navigation_notifications) {
                startActivity(new Intent(Problems.this, Notificaciones.class));
                return true;
            }

            return false;
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
        JSONObject params = new JSONObject();
        try {
            params.put("titulo", titleEdit.getText().toString().trim());
            params.put("descripcion", descriptionEdit.getText().toString().trim());
            params.put("ubicacion", locationEdit.getText().toString().trim());
            params.put("idUsuario", userId);
            params.put("icono", selectedIcon);

            if (base64Image != null && !base64Image.isEmpty()) {
                params.put("imagen", base64Image);
            }

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Enviando problema...");
            progressDialog.show();

            StringEntity entity = new StringEntity(params.toString(), "UTF-8");
            client.addHeader("Content-Type", "application/json");

            client.post(this, BASE_URL, entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progressDialog.dismiss();
                    try {
                        if (response.has("success") && response.getBoolean("success")) {
                            Toast.makeText(Problems.this, "Problema creado exitosamente", Toast.LENGTH_LONG).show();
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

    private void showImageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar imagen");
        builder.setMessage("¿Dónde deseas seleccionar la imagen?");
        builder.setPositiveButton("Tomar foto", (dialog, which) -> takePicture());
        builder.setNegativeButton("Seleccionar de galería", (dialog, which) -> pickImage());
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

        if (resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = null;

                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    Bundle extras = data.getExtras();
                    bitmap = (Bitmap) extras.get("data");
                } else if (requestCode == REQUEST_IMAGE_PICK) {
                    Uri selectedImageUri = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                }

                if (bitmap != null) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, 800, 800 * bitmap.getHeight() / bitmap.getWidth(), true);

                    Glide.with(this)
                            .load(bitmap)
                            .centerCrop()
                            .into(selectedPhotoImageView);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    base64Image = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error al procesar la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
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
}