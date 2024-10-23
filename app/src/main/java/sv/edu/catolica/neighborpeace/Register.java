package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity implements View.OnClickListener {

    EditText  nombre, Email, Contrasena, Direccion, Telefono;
    Button BotonCrear;

    RequestQueue requestQueueu;

    private static final String URL1 = "http://localhost/neigbourdb/save.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        requestQueueu = Volley.newRequestQueue(this);
        initUi();

        BotonCrear.setOnClickListener(this);


        ImageView backArrow = findViewById(R.id.backArrow);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btn = findViewById(R.id.registrarseButton);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent objVentana = new Intent(Register.this, login.class);
                startActivity(objVentana);
            }
        });

    }
    private void initUi(){
         nombre = findViewById(R.id.nombreEditText);
         Email = findViewById(R.id.emailEditText);
         Contrasena = findViewById(R.id.passwordEditText);
         Direccion = findViewById(R.id.direccionEditText);
         Telefono = findViewById(R.id.telefonoEditText);

         BotonCrear = findViewById(R.id.registerButton);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        //switch (id){
            //case R.id.registerButton:
                //break;
        //}
        if (id == R.id.registerButton){
            String name = nombre.getText().toString().trim();
            String mail = Email.getText().toString().trim();
            String contra = Contrasena.getText().toString().trim();
            String direcc = Direccion.getText().toString().trim();
            String Tel = Telefono.getText().toString().trim();

            createUser(name, mail, contra, direcc, Tel);
        }
    }

    private void createUser(String name, String mail, String contra, String direcc, String Tel) {
        StringRequest StringRequest = new StringRequest(
                Request.Method.POST,
                URL1,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(Register.this, "corecta", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String > params = new HashMap<>();
                params.put("nombre", name);
                params.put("email", mail);
                params.put("contrasena", contra);
                params.put("direccion", direcc);
                params.put("telefono", Tel);


                return params;
            }
        };

        requestQueueu.add(StringRequest);

    }
}