package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class Notificaciones extends AppCompatActivity {
    private static final String TAG = "Notificaciones";
    private static final String BASE_URL = "http://192.168.0.12:80/WebServicesphp/";

    private ListView notificationsListView;
    private NotificationsAdapter adapter;
    private List<NotificationItem> notificationsList;
    private AsyncHttpClient client;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);
        Log.d(TAG, "onCreate iniciado");

        // Inicializar componentes
        initializeComponents();

        // Obtener ID del usuario
        getUserId();

        // Configurar el manejo de insets
        setupWindowInsets();

        // Configurar navegación inferior
        setupBottomNavigation();

        // Configurar ListView
        setupListView();

        // Cargar notificaciones
        loadNotifications();
    }

    private void initializeComponents() {
        Log.d(TAG, "Inicializando componentes");
        try {
            client = new AsyncHttpClient();
            client.setTimeout(30000); // 30 segundos timeout
            notificationsList = new ArrayList<>();
            adapter = new NotificationsAdapter();
            notificationsListView = findViewById(R.id.notificationsListView);
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando componentes: " + e.getMessage());
        }
    }

    private void getUserId() {
        Log.d(TAG, "Obteniendo ID de usuario");
        try {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            userId = prefs.getInt("userId", -1);
            Log.d(TAG, "UserID obtenido: " + userId);

            if (userId == -1) {
                Log.e(TAG, "Error: Usuario no identificado");
                Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo userId: " + e.getMessage());
            finish();
        }
    }

    private void setupWindowInsets() {
        Log.d(TAG, "Configurando window insets");
        try {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                v.setPadding(
                        insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                        insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                        insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                        insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                );
                return WindowInsetsCompat.CONSUMED;
            });
        } catch (Exception e) {
            Log.e(TAG, "Error configurando window insets: " + e.getMessage());
        }
    }

    private void setupBottomNavigation() {
        Log.d(TAG, "Configurando navegación inferior");
        try {
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                Log.d(TAG, "Elemento de navegación seleccionado: " + id);

                if (id == R.id.navigation_home) {
                    startActivity(new Intent(this, MainActivity.class));
                    return true;
                } else if (id == R.id.navigation_add_problem) {
                    startActivity(new Intent(this, Problems.class));
                    return true;
                } else if (id == R.id.navigation_history) {
                    startActivity(new Intent(this, History.class));
                    return true;
                } else if (id == R.id.navigation_notifications) {
                    Toast.makeText(this, "Ya estás en notificaciones", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            bottomNavigationView.setSelectedItemId(R.id.navigation_notifications);
        } catch (Exception e) {
            Log.e(TAG, "Error configurando navegación: " + e.getMessage());
        }
    }

    private void setupListView() {
        Log.d(TAG, "Configurando ListView");
        try {
            notificationsListView.setAdapter(adapter);
            notificationsListView.setOnItemClickListener((parent, view, position, id) -> {
                NotificationItem item = notificationsList.get(position);
                Log.d(TAG, "Notificación seleccionada: " + item.title);
                // Aquí puedes manejar el clic en una notificación
            });
        } catch (Exception e) {
            Log.e(TAG, "Error configurando ListView: " + e.getMessage());
        }
    }

    private void loadNotifications() {
        String url = BASE_URL + "getNotifications.php?userId=" + userId;
        Log.d(TAG, "Cargando notificaciones desde: " + url);

        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.d(TAG, "Iniciando petición de notificaciones");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "Respuesta recibida: " + response.toString());
                try {
                    if (response.has("exito") && response.getBoolean("exito")) {
                        JSONArray notificacionesArray = response.getJSONArray("notificaciones");
                        Log.d(TAG, "Número de notificaciones: " + notificacionesArray.length());

                        notificationsList.clear();
                        for (int i = 0; i < notificacionesArray.length(); i++) {
                            JSONObject notif = notificacionesArray.getJSONObject(i);
                            notificationsList.add(new NotificationItem(
                                    notif.getInt("id"),
                                    notif.getString("titulo"),
                                    notif.getString("fecha_formateada")
                            ));
                        }

                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            if (notificationsList.isEmpty()) {
                                Toast.makeText(Notificaciones.this,
                                        "No hay notificaciones", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.e(TAG, "La respuesta no indica éxito");
                        showError("Error en la respuesta del servidor");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando notificaciones: " + e.getMessage());
                    showError("Error al procesar las notificaciones");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "Error en la petición: " + statusCode + " Response: " + responseString, throwable);
                String errorMessage = "Error " + statusCode;
                if (statusCode == 404) {
                    errorMessage = "Archivo no encontrado (404)";
                    Log.e(TAG, "URL no encontrada: " + url);
                }
                showError(errorMessage);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "Error con respuesta JSON: " + (errorResponse != null ? errorResponse.toString() : "null"), throwable);
                showError("Error de conexión");
            }

            @Override
            public void onRetry(int retryNo) {
                Log.d(TAG, "Reintentando petición #" + retryNo);
            }
        });
    }

    private void showError(final String message) {
        Log.e(TAG, "Mostrando error: " + message);
        runOnUiThread(() -> Toast.makeText(Notificaciones.this, message, Toast.LENGTH_LONG).show());
    }

    // Clase para representar una notificación
    private static class NotificationItem {
        int id;
        String title;
        String date;

        NotificationItem(int id, String title, String date) {
            this.id = id;
            this.title = title;
            this.date = date;
        }
    }

    // Adapter personalizado para las notificaciones
    private class NotificationsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return notificationsList.size();
        }

        @Override
        public NotificationItem getItem(int position) {
            return notificationsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return notificationsList.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                ViewHolder holder;

                if (convertView == null) {
                    convertView = LayoutInflater.from(Notificaciones.this)
                            .inflate(R.layout.notification_item, parent, false);

                    holder = new ViewHolder();
                    holder.titleText = convertView.findViewById(R.id.notificationTitle);
                    holder.dateText = convertView.findViewById(R.id.notificationDate);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                NotificationItem item = getItem(position);
                holder.titleText.setText(item.title);
                holder.dateText.setText(item.date);

                return convertView;
            } catch (Exception e) {
                Log.e(TAG, "Error en getView: " + e.getMessage());
                return new View(Notificaciones.this);
            }
        }

        private class ViewHolder {
            TextView titleText;
            TextView dateText;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        loadNotifications(); // Recargar notificaciones al volver a la pantalla
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (client != null) {
            client.cancelAllRequests(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed");
        Intent intent = new Intent(Notificaciones.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}