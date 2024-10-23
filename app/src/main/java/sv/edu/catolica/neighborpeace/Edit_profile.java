package sv.edu.catolica.neighborpeace;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import sv.edu.catolica.neighborpeace.MainActivity;

public class Edit_profile extends AppCompatActivity {

    private ImageView backArrow, profileImage;
    private Button changeImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Inicializar la flecha de retroceso
        backArrow = findViewById(R.id.backArrowEditProfile);

        // Configurar el listener para la flecha de retroceso
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la actividad MainActivity
                Intent intent = new Intent(Edit_profile.this, Profile.class);
                startActivity(intent);
            }
        });

        Button btnGuardarCambios = findViewById(R.id.saveChangesButton);

        btnGuardarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent objVentana = new Intent(Edit_profile.this, Profile.class);
                startActivity(objVentana);

            }
        });

        profileImage = findViewById(R.id.profileImage);
        changeImageButton = findViewById(R.id.changeImageButton);

        changeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSelectionDialog();
            }
        });
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
                    // Abrir la cámara para tomar una foto
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, 1);
                    }
                } else if (selectedId == radioButtonSelectGallery.getId()) {
                    // Seleccionar una foto de la galería
                    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhotoIntent, 2);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap imgCapturado = (Bitmap) data.getExtras().get("data");
            profileImage.setImageBitmap(imgCapturado);
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri imgSeleccionada = data.getData();
            profileImage.setImageURI(imgSeleccionada);
        }
    }
}