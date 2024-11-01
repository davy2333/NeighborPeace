package sv.edu.catolica.neighborpeace;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Edit_profile extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3;

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
                // Navegar a la actividad Profile
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
                    // Verificar permisos de cámara
                    if (ContextCompat.checkSelfPermission(Edit_profile.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Edit_profile.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    } else {
                        // Abrir la cámara para tomar una foto
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                } else if (selectedId == radioButtonSelectGallery.getId()) {
                    // Seleccionar una foto de la galería
                    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Obtener la imagen capturada
            Bundle extras = data.getExtras();
            Bitmap imgCapturado = (Bitmap ) extras.get("data");
            profileImage.setImageBitmap(imgCapturado);
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            // Obtener la imagen seleccionada de la galería
            Uri selectedImageUri = data.getData();
            profileImage.setImageURI(selectedImageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Abrir la cámara para tomar una foto
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } else {
                Toast.makeText(Edit_profile.this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}