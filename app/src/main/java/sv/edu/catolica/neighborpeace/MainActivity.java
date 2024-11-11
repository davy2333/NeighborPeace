package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PROBLEMS_URL = "http://192.168.67.223:80/WebServicesphp/getProblems.php";

    private ImageButton chatButton;
    private ImageButton profileButton;
    private TextView userNameTextView;
    private LinearLayout problemList;
    private EditText searchEditText;
    private AsyncHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupListeners();
        setupBottomNavigation();

        // Obtener nombre de usuario de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userName", "Usuario");
        userNameTextView.setText(userName);

        // Inicializar cliente HTTP y cargar problemas
        client = new AsyncHttpClient();
        loadProblems();
    }

    private void initializeViews() {
        chatButton = findViewById(R.id.chatButton);
        profileButton = findViewById(R.id.profileButton);
        userNameTextView = findViewById(R.id.userNameTextView);
        problemList = findViewById(R.id.problemList);
        searchEditText = findViewById(R.id.searchEditText);
    }

    private void setupListeners() {
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(MainActivity.this, Chat.class);
                startActivity(chatIntent);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(MainActivity.this, Profile.class);
                startActivity(profileIntent);
            }
        });

        // Opcional: Implementar búsqueda en tiempo real
        /*
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Implementar lógica de búsqueda aquí
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        */
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.navigation_home) {
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

    private void loadProblems() {
        client.get(PROBLEMS_URL, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("exito")) {
                        JSONArray problemas = response.getJSONArray("problemas");
                        displayProblems(problemas);
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

            // Establecer los valores
            titleText.setText(problema.getString("titulo"));
            descriptionText.setText(problema.getString("descripcion"));
            locationText.setText("📍 " + problema.getString("ubicacion"));

            // Configurar el estado con color según el valor
            String estado = problema.getString("estado");
            statusText.setText(estado);
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

            // Configurar el onClick para ver detalles
            final int problemId = problema.getInt("id");
            problemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent detailIntent = new Intent(MainActivity.this, DetalleProblema.class);
                        detailIntent.putExtra("problem_id", problemId);
                        detailIntent.putExtra("problem_title", problema.getString("titulo"));
                        detailIntent.putExtra("problem_description", problema.getString("descripcion"));
                        detailIntent.putExtra("problem_location", problema.getString("ubicacion"));
                        detailIntent.putExtra("problem_status", problema.getString("estado"));
                        startActivity(detailIntent);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error al abrir detalles: " + e.getMessage());
                        Toast.makeText(MainActivity.this,
                                "Error al abrir los detalles",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            problemList.addView(problemView);

            // Agregar un divisor si no es el último elemento
            if (i < problemas.length() - 1) {
                View divider = new View(MainActivity.this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        getResources().getDimensionPixelSize(R.dimen.divider_height)));
                divider.setBackgroundColor(getResources().getColor(R.color.divider_color));
                problemList.addView(divider);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar problemas cuando se vuelve a la actividad
        loadProblems();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}