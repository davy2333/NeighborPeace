package sv.edu.catolica.neighborpeace;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class IntroSplashScreen extends AppCompatActivity {

    boolean botonBackPresionado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Codigo para crear un SplashActivity Automaticamente -- controlando el reloj `temporizador`
        //LIBRERIA HANDLER
        Handler manejador = new Handler();

        //Metodo `postDelay -- Runneable + Tiempo en milisegundos`
        manejador.postDelayed(new Runnable() {
            @Override
            public void run() {
                //1Todo lo que coloque de este `run`, se colocara lo que sucede despues de los 3s

                //Si el boton back no ha sido seleccionado, entonces:
                if (!botonBackPresionado){
                    //Cambiar de ventana con la funcion `intent`
                    Intent ventana = new Intent( IntroSplashScreen.this, login.class);
                    startActivity(ventana);
                    finish();   //Permito Bloquear el back para que vuelva al splash
                }
            }
        }, 3000);
    }

    //Metodo que controla el boton back

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Programando el boton back
        botonBackPresionado = true;
    }
}