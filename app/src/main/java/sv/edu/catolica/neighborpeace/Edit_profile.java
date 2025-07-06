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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

public class Edit_profile extends AppCompatActivity {

    private static final String TAG = "Edit_profile";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3;
    private static final String BASE_URL = "http://192.168.0.12:80/WebServicesphp/";

    private ImageView backArrow, profileImage;
    private Button changeImageButton, saveChangesButton;
    private EditText emailEdit, nameEdit, locationEdit, phoneEdit;
    private SharedPreferences sharedPreferences;
    private String currentImageBase64 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        initializeViews();
        loadCurrentUserData();
        setupListeners();
    }

    private void initializeViews() {
        backArrow = findViewById(R.id.backArrowEditProfile);
        profileImage = findViewById(R.id.profileImage);
        changeImageButton = findViewById(R.id.changeImageButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        emailEdit = findViewById(R.id.emailEdit);
        nameEdit = findViewById(R.id.nameEdit);
        locationEdit = findViewById(R.id.locationEdit);
        phoneEdit = findViewById(R.id.phoneEdit);
    }

    private void loadCurrentUserData() {
        int userId = sharedPreferences.getInt("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        AsyncHttpClient client = new AsyncHttpClient();
        String url = BASE_URL + "getProfile.php?userId=" + userId;
        Log.d(TAG, "Loading user data from: " + url);

        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d(TAG, "Response received: " + response.toString());
                    if (response.has("exito") && response.getBoolean("exito")) {
                        JSONObject usuario = response.getJSONObject("usuario");

                        emailEdit.setText(usuario.getString("email"));
                        nameEdit.setText(usuario.getString("nombre"));
                        locationEdit.setText(usuario.getString("direccion"));
                        phoneEdit.setText(usuario.getString("telefono"));

                        // Cargar imagen si existe
                        String imgUrl = usuario.optString("imgPersona", null);
                        loadProfileImage(imgUrl);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading user data", e);
                    Toast.makeText(Edit_profile.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "Request failed. Status: " + statusCode, throwable);
                Toast.makeText(Edit_profile.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfileImage(String imgUrl) {
        if (imgUrl != null && !imgUrl.isEmpty()) {
            String fullImageUrl = BASE_URL + imgUrl;
            Log.d(TAG, "Loading profile image from: " + fullImageUrl);

            // Asegurarse de que el borde circular esté visible si lo usas
            // profileImage.setBackground(getResources().getDrawable(R.drawable.circular_border));

            Glide.with(this)
                    .load(fullImageUrl)
                    .transform(new CircleCrop())  // Aplicar transformación circular
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .error(R.drawable.ic_launcher_profile)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_launcher_profile);
        }
    }

    private void setupListeners() {
        backArrow.setOnClickListener(v -> finish());

        changeImageButton.setOnClickListener(v -> showImageSelectionDialog());

        saveChangesButton.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        int userId = sharedPreferences.getInt("userId", -1);
        if (userId == -1) {
            showError("Usuario no identificado");
            return;
        }

        // Crear RequestParams en lugar de JSONObject
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        params.put("email", emailEdit.getText().toString());
        params.put("nombre", nameEdit.getText().toString());
        params.put("direccion", locationEdit.getText().toString());
        params.put("telefono", phoneEdit.getText().toString());

        if (currentImageBase64 != null) {
            params.put("imagen", currentImageBase64);
        }

        AsyncHttpClient client = new AsyncHttpClient();
        String url = BASE_URL + "editProfile.php";
        Log.d(TAG, "Enviando actualización a: " + url);

        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d(TAG, "Respuesta recibida: " + response.toString());
                    if (response.has("exito") && response.getBoolean("exito")) {
                        Toast.makeText(Edit_profile.this,
                                "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String error = response.optString("error", "Error desconocido");
                        showError("Error: " + error);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando respuesta", e);
                    showError("Error al procesar respuesta: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "Error en la petición. Estado: " + statusCode, throwable);
                showError("Error de conexión (" + statusCode + "): " + throwable.getMessage());
            }
        });
    }

    private void showError(final String message) {
        Log.e(TAG, "Error: " + message);
        runOnUiThread(() -> Toast.makeText(Edit_profile.this, message, Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = null;
                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                } else if (requestCode == REQUEST_IMAGE_PICK) {
                    Uri selectedImage = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                }

                if (bitmap != null) {
                    // Redimensionar la imagen si es necesario
                    bitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true);

                    // Mostrar la imagen con Glide para mantener la consistencia visual
                    Glide.with(this)
                            .load(bitmap)
                            .transform(new CircleCrop())
                            .into(profileImage);

                    // Convertir a Base64 para el envío
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    currentImageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing image", e);
                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showImageSelectionDialog() {
        // Crear un diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(Edit_profile.this);
        builder.setTitle("Seleccione una opción");
        builder.setCancelable(false);

        // Inflar el layout personalizado con RadioButtons
        View customDialogView = getLayoutInflater().inflate(R.layout.dialog_image_selection, null);
        builder.setView(customDialogView);

        // Obtener el RadioGroup y sus RadioButtons del layout personalizado
        RadioGroup radioGroup = customDialogView.findViewById(R.id.radioGroupImageSelection);
        RadioButton radioButtonTakePhoto = customDialogView.findViewById(R.id.radioTakePhoto);
        RadioButton radioButtonSelectGallery = customDialogView.findViewById(R.id.radioSelectGallery);

        // Configurar las acciones del diálogo
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId == radioButtonTakePhoto.getId()) {
                    // Verificar permisos de cámara
                    if (ContextCompat.checkSelfPermission(Edit_profile.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Edit_profile.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    } else {
                        // Abrir la cámara para tomar una foto
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                } else if (selectedId == radioButtonSelectGallery.getId()) {
                    // Seleccionar una foto de la galería
                    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
                } else {
                    Toast.makeText(Edit_profile.this, "Seleccione una opción", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", null);

        // Mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Abrir la cámara para tomar una foto
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } else {
                Toast.makeText(Edit_profile.this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}