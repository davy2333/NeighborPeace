package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ImageButton chatButton;
    private ImageButton profileButton;
    private TextView userNameTextView;
    private LinearLayout problemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        chatButton = findViewById(R.id.chatButton);
        profileButton = findViewById(R.id.profileButton);
        userNameTextView = findViewById(R.id.userNameTextView);
        problemList = findViewById(R.id.problemList);

        // Configurar el botón de chat
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
                // Iniciar la actividad de perfil
                Intent profileIntent = new Intent(MainActivity.this, Profile.class);
                startActivity(profileIntent);
            }
        });

        // Configurar el menú de navegación inferior
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.navigation_home) {
                    // Ya estamos en la pantalla principal
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

        // Llamar al método que obtiene los problemas de la base de datos
        new GetProblemsTask().execute("http://192.168.56.1:80/WebServicesphp/getProblems.php");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(); // Cierra la actividad actual
    }

    private class GetProblemsTask extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return new JSONArray(response.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray problems) {
            if (problems != null) {
                // Limpiar la lista
                problemList.removeAllViews();

                // Agregar los problemas a la lista
                for (int i = 0; i < problems.length(); i++) {
                    try {
                        JSONObject problem = problems.getJSONObject(i);
                        String titulo = problem.getString("titulo");
                        String descripcion = problem.getString("descripcion");
                        String ubicacion = problem.getString("ubicacion");
                        String estado = problem.getString("estado");

                        // Crear una nueva vista para cada problema y agregarla a la lista
                        View problemView = LayoutInflater.from(MainActivity.this).inflate(R.layout.problem_item, problemList, false);
                        TextView tituloTextView = problemView.findViewById(R.id.tituloTextView);
                        TextView descripcionTextView = problemView.findViewById(R.id.descripcionTextView);
                        TextView ubicacionTextView = problemView.findViewById(R.id.ubicacionTextView);
                        TextView estadoTextView = problemView.findViewById(R.id.estadoTextView);

                        tituloTextView.setText(titulo);
                        descripcionTextView.setText(descripcion);
                        ubicacionTextView.setText(ubicacion);
                        estadoTextView.setText(estado);

                        // Establecer el color de fondo según el estado del problema
                        switch (estado) {
                            case "REPORTADO":
                                problemView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.amarillo));
                                break;
                            case "EN PROCESO":
                                problemView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.naranja));
                                break;
                            case "FINALIZADO":
                                problemView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.verde));
                                break;
                        }

                        problemList.addView(problemView);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}