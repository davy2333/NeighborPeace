package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PROBLEMS_URL = "http://192.168.0.12:80/WebServicesphp/getProblems.php";
    private static final String BASE_URL = "http://192.168.0.12:80/WebServicesphp/";

    private ImageButton chatButton;
    private ImageButton profileButton;
    private TextView userNameTextView;
    private LinearLayout problemList;
    private EditText searchEditText;
    private AsyncHttpClient client;
    private JSONArray allProblems;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new AsyncHttpClient();
        initializeViews();
        setupListeners();
        setupBottomNavigation();

        // Añadir el OnClickListener para logoApp
        ImageView logoApp = findViewById(R.id.logoApp);
        logoApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aboutIntent = new Intent(MainActivity.this, About.class);
                startActivity(aboutIntent);
            }
        });

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userName", "Usuario");
        userNameTextView.setText(userName);

        loadProblems();
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.navigation_home) {
                    return true;
                } else if (id == R.id.navigation_add_problem) {
                    Intent problemIntent = new Intent(MainActivity.this, Problems.class);
                    startActivity(problemIntent);
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

        bottomNavigation.setSelectedItemId(R.id.navigation_home);
    }

    private void setupListeners() {
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(MainActivity.this, Profile.class);
                startActivity(profileIntent);
            }
        });

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(MainActivity.this, Chat.class);
                startActivity(chatIntent);
            }
        });
    }

    private void initializeViews() {
        chatButton = findViewById(R.id.chatButton);
        profileButton = findViewById(R.id.profileButton);
        userNameTextView = findViewById(R.id.userNameTextView);
        problemList = findViewById(R.id.problemList);
        searchEditText = findViewById(R.id.searchEditText);

        profileButton.setBackground(null);
        profileButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        profileButton.setPadding(0, 0, 0, 0);

        loadProfileImage();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    filterProblems(s.toString());
                } catch (JSONException e) {
                    Log.e(TAG, "Error filtering problems", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        if (userId != -1) {
            String url = BASE_URL + "getProfile.php?userId=" + userId;
            Log.d(TAG, "Loading profile image for user: " + userId);

            client.get(url, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Log.d(TAG, "Profile response: " + response.toString());
                        if (response.has("exito") && response.getBoolean("exito")) {
                            JSONObject usuario = response.getJSONObject("usuario");
                            String imgUrl = usuario.optString("imgPersona", null);

                            if (imgUrl != null && !imgUrl.isEmpty()) {
                                String fullImageUrl = BASE_URL + imgUrl;
                                Log.d(TAG, "Loading image from: " + fullImageUrl);

                                runOnUiThread(() -> {
                                    Glide.with(MainActivity.this)
                                            .load(fullImageUrl)
                                            .transform(new CircleCrop())
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true)
                                            .error(R.drawable.ic_launcher_profile)
                                            .into(profileButton);
                                });
                            } else {
                                Log.d(TAG, "No profile image URL found");
                                runOnUiThread(() -> {
                                    Glide.with(MainActivity.this)
                                            .load(R.drawable.ic_launcher_profile)
                                            .transform(new CircleCrop())
                                            .into(profileButton);
                                });
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading profile image", e);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.e(TAG, "Failed to load profile. Status: " + statusCode, throwable);
                    runOnUiThread(() -> {
                        Glide.with(MainActivity.this)
                                .load(R.drawable.ic_launcher_profile)
                                .transform(new CircleCrop())
                                .into(profileButton);
                    });
                }
            });
        }
    }

    private void filterProblems(String searchText) throws JSONException {
        if (allProblems == null) return;

        searchText = searchText.toLowerCase().trim();
        JSONArray filteredProblems = new JSONArray();

        for (int i = 0; i < allProblems.length(); i++) {
            JSONObject problema = allProblems.getJSONObject(i);

            boolean matchesTitulo = problema.getString("titulo").toLowerCase().contains(searchText);
            boolean matchesDescripcion = problema.getString("descripcion").toLowerCase().contains(searchText);
            boolean matchesUbicacion = problema.getString("ubicacion").toLowerCase().contains(searchText);
            boolean matchesUsuario = problema.getString("nombre_usuario").toLowerCase().contains(searchText);
            boolean matchesEstado = problema.getString("estado").toLowerCase().contains(searchText);

            if (matchesTitulo || matchesDescripcion || matchesUbicacion || matchesUsuario || matchesEstado) {
                filteredProblems.put(problema);
            }
        }

        displayProblems(filteredProblems);
    }

    private void displayProblems(JSONArray problemas) throws JSONException {
        problemList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < problemas.length(); i++) {
            JSONObject problema = problemas.getJSONObject(i);

            View problemView = inflater.inflate(R.layout.problem_item, problemList, false);

            TextView titleText = problemView.findViewById(R.id.problemTitle);
            TextView descriptionText = problemView.findViewById(R.id.problemDescription);
            TextView locationText = problemView.findViewById(R.id.problemLocation);
            TextView statusText = problemView.findViewById(R.id.problemStatus);
            TextView dateText = problemView.findViewById(R.id.problemDate);
            TextView userText = problemView.findViewById(R.id.problemUser);
            ImageView problemImage = problemView.findViewById(R.id.problemImage);
            ImageView problemIcon = problemView.findViewById(R.id.problemIcon);

            titleText.setText(problema.getString("titulo"));
            descriptionText.setText(problema.getString("descripcion"));
            locationText.setText("📍 " + problema.getString("ubicacion"));

            String iconName = problema.optString("icono", "default");
            int iconResource = getIconResource(iconName);
            if (iconResource != 0) {
                problemIcon.setImageResource(iconResource);
                problemIcon.setVisibility(View.VISIBLE);
            } else {
                problemIcon.setVisibility(View.GONE);
            }


            String estado = problema.getString("estado");
            statusText.setText(estado);

            // Configurar el color del estado
            switch (estado) {
                case "REPORTADO":
                    statusText.setBackgroundResource(R.drawable.status_background_reported);
                    break;
                case "EN PROCESO":
                    statusText.setBackgroundResource(R.drawable.status_background_in_progress);
                    break;
                case "FINALIZADO":
                    statusText.setBackgroundResource(R.drawable.status_background_completed);
                    break;
            }

            dateText.setText(problema.getString("fecha_formateada"));
            userText.setText("Reportado por: " + problema.getString("nombre_usuario"));

            // Cargar la imagen del problema
            String imageUrl = problema.optString("imagen", null);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(BASE_URL + imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(problemImage);
            } else {
                problemImage.setVisibility(View.GONE);
            }

            // Configurar el clic para ver detalles
            final int problemId = problema.getInt("id");
            problemView.setOnClickListener(v -> {
                try {
                    Intent detailIntent = new Intent(MainActivity.this, Detalles_problema.class);
                    detailIntent.putExtra("problem_id", problemId);
                    detailIntent.putExtra("problem_title", problema.getString("titulo"));
                    detailIntent.putExtra("problem_description", problema.getString("descripcion"));
                    detailIntent.putExtra("problem_location", problema.getString("ubicacion"));
                    detailIntent.putExtra("problem_status", problema.getString("estado"));
                    detailIntent.putExtra("problem_image", problema.optString("imagen", ""));
                    startActivity(detailIntent);
                } catch (JSONException e) {
                    Log.e(TAG, "Error al abrir detalles: " + e.getMessage());
                    Toast.makeText(MainActivity.this,
                            "Error al abrir los detalles",
                            Toast.LENGTH_SHORT).show();
                }
            });

            problemList.addView(problemView);

            // Agregar divisor si no es el último elemento
            /*if (i < problemas.length() - 1) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        getResources().getDimensionPixelSize(R.dimen.divider_height)));
                divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color));
                problemList.addView(divider);
            }*/
        }
    }

    private void loadProblems() {
        client.get(PROBLEMS_URL, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("exito")) {
                        allProblems = response.getJSONArray("problemas");
                        displayProblems(allProblems);
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Error al cargar los problemas",
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error en la respuesta: " + response.toString());
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON: " + e.getMessage());
                    Toast.makeText(MainActivity.this,
                            "Error al procesar los datos",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "Error de conexión: " + throwable.getMessage());
                Toast.makeText(MainActivity.this,
                        "Error de conexión al servidor",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "Error: " + throwable.getMessage());
                Toast.makeText(MainActivity.this,
                        "Error al obtener los datos",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (client == null) {
            client = new AsyncHttpClient();
        }
        loadProblems();
        loadProfileImage();

        if (searchEditText != null) {
            searchEditText.setText("");
        }

        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }
    }
    private int getIconResource(String iconName) {
        switch (iconName) {
            case "ic_water":
                return R.drawable.problemas_electricos;
            case "ic_electricity":
                return R.drawable.problemas_en_tuberias;
            case "ic_road":
                return R.drawable.problemas_viales;
            case "default":
            default:
                return R.drawable.ic_default;
        }
    }
}