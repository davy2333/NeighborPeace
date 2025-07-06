package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class History extends AppCompatActivity {
    private static final String TAG = "History";
    private static final String BASE_URL = "http://192.168.0.12:80/WebServicesphp/";

    private ListView historyListView;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyList;
    private AsyncHttpClient client;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initializeComponents();
        getUserId();
        setupWindowInsets();
        setupBottomNavigation();
        setupListView();
        loadHistory();
    }

    private void initializeComponents() {
        client = new AsyncHttpClient();
        historyList = new ArrayList<>();
        adapter = new HistoryAdapter();
        historyListView = findViewById(R.id.historyListView);
    }

    private void getUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.navigation_add_problem) {
                startActivity(new Intent(this, Problems.class));
            } else if (id == R.id.navigation_history) {
                Toast.makeText(this, "Ya estás en el historial", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.navigation_notifications) {
                startActivity(new Intent(this, Notificaciones.class));
            }

            return true;
        });
    }

    private void setupListView() {
        historyListView.setAdapter(adapter);
    }

    private void loadHistory() {
        String url = BASE_URL + "getHistory.php?userId=" + userId;
        Log.d(TAG, "Cargando historial desde: " + url);

        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("exito") && response.getBoolean("exito")) {
                        JSONArray historialArray = response.getJSONArray("historial");
                        historyList.clear();

                        for (int i = 0; i < historialArray.length(); i++) {
                            JSONObject item = historialArray.getJSONObject(i);
                            historyList.add(new HistoryItem(
                                    item.getInt("id"),
                                    item.getString("titulo"),
                                    item.getString("fecha_formateada")
                            ));
                        }

                        adapter.notifyDataSetChanged();

                        if (historyList.isEmpty()) {
                            Toast.makeText(History.this,
                                    "No hay registros en el historial", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing history", e);
                    showError("Error al procesar el historial");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "Request failed: " + statusCode, throwable);
                String errorMessage = "Error de conexión";
                if (statusCode == 404) {
                    errorMessage = "Archivo no encontrado (404)";
                }
                showError(errorMessage);
            }
        });
    }

    private void showError(final String message) {
        runOnUiThread(() -> Toast.makeText(History.this, message, Toast.LENGTH_LONG).show());
    }

    private static class HistoryItem {
        int id;
        String title;
        String date;

        HistoryItem(int id, String title, String date) {
            this.id = id;
            this.title = title;
            this.date = date;
        }
    }

    private class HistoryAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return historyList.size();
        }

        @Override
        public HistoryItem getItem(int position) {
            return historyList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return historyList.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(History.this)
                        .inflate(R.layout.history_item, parent, false);

                holder = new ViewHolder();
                holder.titleText = convertView.findViewById(R.id.historyTitle);
                holder.dateText = convertView.findViewById(R.id.historyDate);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            HistoryItem item = getItem(position);
            holder.titleText.setText(item.title);
            holder.dateText.setText(item.date);

            return convertView;
        }

        private class ViewHolder {
            TextView titleText;
            TextView dateText;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory(); // Recargar historial al volver a la pantalla
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(History.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}