package sv.edu.catolica.neighborpeace;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import org.json.JSONObject;

public class Profile extends AppCompatActivity {

    private static final String TAG = "Profile";
    private ImageView backArrow, profileImage;
    private TextView homeText, titleProfile, emailValue, nameValue, locationValue, phoneValue;
    private Button editProfileButton, logoutButton;
    private SharedPreferences sharedPreferences;
    private static final String BASE_URL = "http://192.168.1.125:80/WebServicesphp/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        initializeViews();
        loadUserProfile();
        setupClickListeners();
    }

    private void initializeViews() {
        backArrow = findViewById(R.id.backArrow);
        titleProfile = findViewById(R.id.titleProfile);
        profileImage = findViewById(R.id.profileImage);
        emailValue = findViewById(R.id.emailValue);
        nameValue = findViewById(R.id.nameValue);
        locationValue = findViewById(R.id.locationValue);
        phoneValue = findViewById(R.id.phoneValue);
        editProfileButton = findViewById(R.id.editProfileButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void loadProfileImage(String imgUrl) {
        if (imgUrl != null && !imgUrl.isEmpty()) {
            String fullImageUrl = BASE_URL + imgUrl;
            Log.d(TAG, "Loading profile image from: " + fullImageUrl);

            // Asegurarse de que el borde circular esté visible
            profileImage.setBackground(getResources().getDrawable(R.drawable.circular_border));

            Glide.with(this)
                    .load(fullImageUrl)
                    .transform(new CircleCrop())  // Aplicar transformación circular
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .error(R.drawable.ic_launcher_profile)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Error loading image: " + fullImageUrl, e);
                            profileImage.setBackground(getResources().getDrawable(R.drawable.circular_border));
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            Log.d(TAG, "Image loaded successfully");
                            profileImage.setBackground(getResources().getDrawable(R.drawable.circular_border));
                            return false;
                        }
                    })
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_launcher_profile);
            profileImage.setBackground(getResources().getDrawable(R.drawable.circular_border));
        }
    }

    private void loadUserProfile() {
        int userId = sharedPreferences.getInt("userId", -1);
        Log.d(TAG, "Loading profile for userId: " + userId);

        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        AsyncHttpClient client = new AsyncHttpClient();
        String url = BASE_URL + "getProfile.php?userId=" + userId;
        Log.d(TAG, "Request URL: " + url);

        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "Response received: " + response.toString());
                try {
                    if (response.has("exito") && response.getBoolean("exito")) {
                        JSONObject usuario = response.getJSONObject("usuario");

                        runOnUiThread(() -> {
                            try {
                                emailValue.setText(usuario.getString("email"));
                                nameValue.setText(usuario.getString("nombre"));
                                locationValue.setText(usuario.getString("direccion"));
                                phoneValue.setText(usuario.getString("telefono"));

                                // Usar el nuevo método para cargar la imagen
                                loadProfileImage(usuario.optString("imgPersona", null));
                            } catch (Exception e) {
                                Log.e(TAG, "Error setting UI values", e);
                                showError("Error al mostrar los datos: " + e.getMessage());
                            }
                        });
                    } else {
                        String errorMsg = response.has("error") ?
                                response.getString("error") : "Error desconocido";
                        showError(errorMsg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                    showError("Error al procesar los datos: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "Request failed. Status code: " + statusCode, throwable);
                String errorMessage = "Error de conexión (" + statusCode + "): ";
                errorMessage += (statusCode == 404) ? "URL no encontrada" : throwable.getMessage();
                showError(errorMessage);
            }
        });
    }

    private void showError(final String message) {
        Log.e(TAG, "Error: " + message);
        runOnUiThread(() -> Toast.makeText(Profile.this, message, Toast.LENGTH_LONG).show());
    }

    private void setupClickListeners() {
        backArrow.setOnClickListener(v -> finish());

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, Edit_profile.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(Profile.this)
                .setTitle("Confirmar Cierre de Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Limpiar SharedPreferences
                    sharedPreferences.edit().clear().apply();
                    navigateToLogin();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(Profile.this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }
}