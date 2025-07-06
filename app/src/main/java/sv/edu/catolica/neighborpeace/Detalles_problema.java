package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import org.json.JSONObject;

public class Detalles_problema extends AppCompatActivity {

    private static final String TAG = "Detalles_problema";
    private static final String BASE_URL = "http://192.168.0.12:80/WebServicesphp/";

    private TextView problemTitle, problemDescription, problemLocation, problemStatus;
    private TextView problemDate, reporterName, reporterEmail;
    private TextView engineerName, engineerEmail, supervisorName, supervisorEmail;
    private ImageView backArrow, problemImage;
    private ImageButton chatButton;
    private AsyncHttpClient client;
    private int problemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_problema);

        client = new AsyncHttpClient();
        initializeViews();
        problemId = getIntent().getIntExtra("problem_id", -1);

        if (problemId != -1) {
            loadProblemDetails();
        } else {
            Toast.makeText(this, "Error: No se pudo cargar el problema", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupListeners();

        // Inicializar las vistas
        problemTitle = findViewById(R.id.problemTitle);
        chatButton = findViewById(R.id.chatButton);
        backArrow = findViewById(R.id.backArrow);

        // Obtener datos del intent
        problemId = getIntent().getIntExtra("problem_id", -1);
        String title = getIntent().getStringExtra("problem_title");
        if (title != null) {
            problemTitle.setText(title);
        }

        // Configurar listeners
        setupListeners();
    }

    private void initializeViews() {
        problemTitle = findViewById(R.id.problemTitle);
        problemDescription = findViewById(R.id.problemDescription);
        problemLocation = findViewById(R.id.problemLocation);
        problemStatus = findViewById(R.id.problemStatus);
        problemDate = findViewById(R.id.problemDate);
        reporterName = findViewById(R.id.reporterName);
        reporterEmail = findViewById(R.id.reporterEmail);
        engineerName = findViewById(R.id.engineerName);
        engineerEmail = findViewById(R.id.engineerEmail);
        supervisorName = findViewById(R.id.supervisorName);
        supervisorEmail = findViewById(R.id.supervisorEmail);
        backArrow = findViewById(R.id.backArrow);
        problemImage = findViewById(R.id.problemImage);
        chatButton = findViewById(R.id.chatButton);
    }

    private void loadProblemDetails() {
        String url = BASE_URL + "getProblemDetails.php?problemId=" + problemId;

        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("exito") && response.getBoolean("exito")) {
                        JSONObject problema = response.getJSONObject("problema");

                        runOnUiThread(() -> {
                            try {
                                // Información básica del problema
                                problemTitle.setText(problema.getString("titulo"));
                                problemDescription.setText(problema.getString("descripcion"));
                                problemLocation.setText("📍 " + problema.getString("ubicacion"));
                                problemDate.setText("Reportado el " + problema.getString("fecha_formateada"));

                                // Estado del problema
                                String estado = problema.getString("estado");
                                problemStatus.setText(estado);
                                setStatusBackground(estado);

                                // Información del reportador
                                reporterName.setText(problema.getString("reportador_nombre"));
                                reporterEmail.setText(problema.getString("reportador_email"));

                                // Información del ingeniero
                                if (problema.has("ingeniero") && !problema.isNull("ingeniero")) {
                                    JSONObject ingeniero = problema.getJSONObject("ingeniero");
                                    engineerName.setText(ingeniero.getString("nombre"));
                                    engineerEmail.setText(ingeniero.getString("email"));
                                } else {
                                    engineerName.setText("Pendiente de asignar");
                                    engineerEmail.setText("");
                                }

                                // Información del supervisor
                                if (problema.has("supervisor") && !problema.isNull("supervisor")) {
                                    JSONObject supervisor = problema.getJSONObject("supervisor");
                                    supervisorName.setText(supervisor.getString("nombre"));
                                    supervisorEmail.setText(supervisor.getString("email"));
                                } else {
                                    supervisorName.setText("Pendiente de asignar");
                                    supervisorEmail.setText("");
                                }

                                // Cargar imagen del problema
                                String imgUrl = problema.optString("img", null);
                                if (imgUrl != null && !imgUrl.isEmpty()) {
                                    Glide.with(Detalles_problema.this)
                                            .load(BASE_URL + imgUrl)
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true)
                                            .error(android.R.drawable.ic_menu_gallery)
                                            .into(problemImage);
                                } else {
                                    problemImage.setImageResource(android.R.drawable.ic_menu_gallery);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error setting values", e);
                                showError("Error al mostrar los detalles del problema");
                            }
                        });
                    } else {
                        showError(response.optString("error", "Error desconocido"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                    showError("Error al procesar la respuesta del servidor");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "Request failed: " + statusCode, throwable);
                showError("Error de conexión: " + throwable.getMessage());
            }
        });
    }

    private void setStatusBackground(String status) {
        int backgroundColor;
        switch (status) {
            case "REPORTADO":
                backgroundColor = R.drawable.status_background_reported;
                break;
            case "EN PROCESO":
                backgroundColor = R.drawable.status_background_in_progress;
                break;
            case "FINALIZADO":
                backgroundColor = R.drawable.status_background_completed;
                break;
            default:
                backgroundColor = R.drawable.status_background;
                break;
        }
        problemStatus.setBackgroundResource(backgroundColor);
    }

    private void setupListeners() {
        backArrow.setOnClickListener(v -> finish());

        chatButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String userType = prefs.getString("userType", "");

            Log.d("DetallesProblema", "UserType: " + userType);
            Log.d("DetallesProblema", "ProblemId: " + problemId);
            Log.d("DetallesProblema", "Title: " + (problemTitle != null ? problemTitle.getText() : "null"));

            Intent chatIntent;
            if ("ADMIN".equals(userType)) {
                Log.d("DetallesProblema", "Iniciando ChatAdmin");
                chatIntent = new Intent(Detalles_problema.this, ChatAdmin.class);
            } else {
                Log.d("DetallesProblema", "Iniciando Chat normal");
                chatIntent = new Intent(Detalles_problema.this, Chat.class);
            }

            String titulo = problemTitle != null ? problemTitle.getText().toString() : "";

            chatIntent.putExtra("problem_id", problemId);
            chatIntent.putExtra("problem_title", titulo);
            startActivity(chatIntent);
        });
    }

    private void showError(final String message) {
        runOnUiThread(() ->
                Toast.makeText(Detalles_problema.this, message, Toast.LENGTH_LONG).show()
        );
    }
}