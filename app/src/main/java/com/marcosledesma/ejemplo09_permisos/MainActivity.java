package com.marcosledesma.ejemplo09_permisos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // Request para devolver info
    private final int CAMARA_ACTION = 1;
    private final int TAKE_SAVE_ACTION = 2;
    private final int OPEN_GALLERY_ACTION = 3;

    // Request Code de los permisos
    private final int CAMERA_PERMISSION = 100;
    private final int CALL_PERMISSION = 101;
    private final int TAKE_SAVE_PERMISSION = 102;
    private final int OPEN_GALLERY_PERMISSION = 103;
    private final int LOCATION_PERMISSION = 104;

    // Vista
    private EditText txtNumTel;
    private ImageButton btnLlamar;
    private ImageView imgCamara;
    private Button btnTakeSave;
    private Button btnOpenGallery;
    private Button btnLocation;
    private TextView txtDireccion;
    private TextView txtCoordenadas;
    // String de la ruta de la imágen para mostrarla
    private String currentPhotoPath;    // Uri al archivo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtNumTel = findViewById(R.id.txtNumTelefono);
        btnLlamar = findViewById(R.id.btnLlamar);
        imgCamara = findViewById(R.id.imgCamara);
        btnTakeSave = findViewById(R.id.btnTakeSave);
        btnOpenGallery = findViewById(R.id.btnOpenGalleryAction);
        btnLocation = findViewById(R.id.btnGetLocation);
        txtCoordenadas = findViewById(R.id.txtCoordenadas);
        txtDireccion = findViewById(R.id.txtDireccion);

        // click sobre Image View Cámara
        imgCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. COMPROBAR QUÉ VERSIÓN DE ANDROID USO (PEDIR PERMISO EXPLÍCITO PARA POSTERIORES A ANDROID 6)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // API 23 (Android 6)
                    camaraAction();
                } else {
                    // Si tengo los permisos llamo a la función, y si no -> Pedir permisos "manualmente"
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        camaraAction();
                    } else {  // Lanzamos alerta emergente (Permitir a la App acceder a las llamadas?)
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                    }
                }
            }
        });

        // Añadir permisos al AndroidManifest.xml
        btnLlamar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Si el txt no está vacío, llamamremos cuando pulsemos el botón
                if (!txtNumTel.getText().toString().isEmpty()) {
                    // 1. COMPROBAR QUÉ VERSIÓN DE ANDROID USO (PEDIR PERMISO EXPLÍCITO PARA POSTERIORES A ANDROID 6)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // API 23 (Android 6)
                        llamadaAction();
                    } else {
                        // Si tengo los permisos llamo a la función, y si no -> Pedir permisos "manualmente"
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            llamadaAction();
                        } else {  // Lanzamos alerta emergente (Permitir a la App acceder a las llamadas?)
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION);
                        }
                    }

                }
            }
        });


        btnTakeSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Comprueba versión de Android
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    takeSaveAction();
                } else {
                    // Comprueba si tengo permisos ya concedidos
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED) {
                        takeSaveAction();
                    } else { // Pide los permisos
                        String[] permisos = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(MainActivity.this, permisos, TAKE_SAVE_PERMISSION);

                    }
                }

            }
        });

        // BOTÓN ABRIR GALERÍA
        btnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Comprueba versión de Android
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    openGalleryAction();
                } else {
                    // Comprueba si tengo permisos ya concedidos (LEER ALMACENAMIENTO EXTERNO)
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        openGalleryAction();
                    } else { // Pide los permisos
                        String[] permisos = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(MainActivity.this, permisos, OPEN_GALLERY_PERMISSION);
                    }
                }
            }
        });

        // Botón LOCALIZACIÓN
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Comprueba versión de Android
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    takeSaveAction();
                } else {
                    // Comprueba si tengo permisos ya concedidos
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {
                        getLocationAction();
                    } else { // Pide los permisos
                        String[] permisos = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                        ActivityCompat.requestPermissions(MainActivity.this, permisos, LOCATION_PERMISSION);

                    }
                }
            }
        });

    }

    // Método para permitir geolocalización
    private void getLocationAction() {

    }

    private void camaraAction() {
        // Abrir cámara
        Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // startActivityForResult porque ésta nos devolverá una imagen
        startActivityForResult(intentCamara, CAMARA_ACTION);
    }

    // CREAR FICHERO VACÍO (DESPUÉS LA CÁMARA LO RELLENARÁ CON LA FOTO TOMADA)
    private File crearFichero() throws IOException {
        // Momento en que presionamos botón (guardado en timeStamp)
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        // Nombre completo de la imágen
        String imageFileName = "JPEG_" + timeStamp + "_";

        File directoryPictures = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, // Nombre de la imágen
                ".jpg", // Extensión
                directoryPictures); // Ruta donde almacenar
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // RECOGE EL FICHERO VACÍO Y GUARDA LA FOTO TOMADA EN LA URI
    private void takeSaveAction() {
        try {
            // 1. Crear un Fichero Vacío
            File photoFile = crearFichero();
            // Si photoFile es distinto de null obtendré url interna de la imágen (uri)
            if (photoFile != null) {
                Uri uriPhotoFile = FileProvider.getUriForFile(
                        this,
                        "com.marcosledesma.ejemplo09_permisos",
                        photoFile);
                // Intent
                Intent intentTakeSave = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Le pasamos parámetro (enlace) donde queremos que se guarde
                intentTakeSave.putExtra(MediaStore.EXTRA_OUTPUT, uriPhotoFile);
                // Así la cámara tendrá la ruta donde guardar esa imágen
                //
                startActivityForResult(intentTakeSave, TAKE_SAVE_ACTION);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ABRIR GALERÍA (botón)
    private void openGalleryAction() {
        Intent intentOpenGallery = new Intent(Intent.ACTION_GET_CONTENT);
        // Filtro para abrir cualquier imagen en cualquier extensión
        intentOpenGallery.setType("image/*");

        startActivityForResult(intentOpenGallery, OPEN_GALLERY_ACTION);
    }


    // Método asociado a requestPermissions() -> (como onActivityResult con startActivityForResult)

    /**
     * Se ejecuta justo después de que el usuario conteste a los permisos
     *
     * @param requestCode  -> código de la petición de los permisos
     * @param permissions  -> String[] con los permisos que se han solicitado
     * @param grantResults ->  int[] con los resultados de las peticiones de cada permiso
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Comprobar si has dado permisos o no (en qué posición de los array de String e int)
        if (requestCode == CALL_PERMISSION) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                llamadaAction();
            } else {
                Toast.makeText(this, "No puedo llamar sin permisos", Toast.LENGTH_SHORT).show();
            }
        }
        // Prácticamente lo mismo para la cámara
        if (requestCode == CAMERA_PERMISSION) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                camaraAction();
            } else {
                Toast.makeText(this, "No puedo usar la cámara sin permisos", Toast.LENGTH_SHORT).show();
            }
        }

        // Lo mismo para guardar la foto tomada (Este debe comprobar 2 permisos -> Cámara y escritura)
        if (requestCode == TAKE_SAVE_PERMISSION) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takeSaveAction();
            } else {
                Toast.makeText(this, "No puedo hacer nada sin permisos", Toast.LENGTH_SHORT).show();
            }
        }

        // Lo mismo para Leer Almacenamiento Externo
        if (requestCode == OPEN_GALLERY_PERMISSION) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGalleryAction();
            } else {
                Toast.makeText(this, "No puedo leer el almacenamiento externo sin permisos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Para mostrar la foto realizada en el ImageView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // camaraAction (imageBitmap)
        if (requestCode == CAMARA_ACTION && resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            Bitmap imageBitmap = (Bitmap) bundle.get("data");
            imgCamara.setImageBitmap(imageBitmap);
        }

        // takeSaveAction (currentPhotoPath -> Uri con la img)
        if (requestCode == TAKE_SAVE_ACTION && resultCode == RESULT_OK) {
            imgCamara.setImageURI(Uri.parse(currentPhotoPath));
            /*
            // Si quiero guardar la foto en la galería tengo que notificarla
            Intent intentMediaScan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(currentPhotoPath);
            // Meter file en el intent
            intentMediaScan.setData(Uri.fromFile(f));
            this.sendBroadcast(intentMediaScan);
            */
        }

        // startActivityForResult para Abrir Galería
        if (requestCode == OPEN_GALLERY_ACTION && resultCode == RESULT_OK && data != null) {
            Uri uriFile = data.getData();
            imgCamara.setImageURI(uriFile);
        }
    }

    private void llamadaAction() {
        // Hacer la llamada (Intent con actividad pre-hecha(de Android) -> ACTION_CALL)
        Intent intentTelefono = new Intent(Intent.ACTION_CALL);
        // Pasarle el número al que llamaremos
        intentTelefono.setData(Uri.parse("tel: " + txtNumTel.getText().toString()));
        // Lanzar activity del intent
        startActivity(intentTelefono);
    }
}