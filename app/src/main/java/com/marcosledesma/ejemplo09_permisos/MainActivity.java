package com.marcosledesma.ejemplo09_permisos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final int CAMERA_PERMISSION = 99;
    private final int CAMARA_ACTION = 101;
    private final int CALL_PERMISSION = 100;
    private EditText txtNumTel;
    private ImageButton btnLlamar;
    private ImageView imgCamara;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtNumTel = findViewById(R.id.txtNumTelefono);
        btnLlamar = findViewById(R.id.btnLlamar);
        imgCamara = findViewById(R.id.imgCamara);

        // click sobre Image View Cámara
        imgCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. COMPROBAR QUÉ VERSIÓN DE ANDROID USO (PEDIR PERMISO EXPLÍCITO PARA POSTERIORES A ANDROID 6)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){ // API 23 (Android 6)
                    camaraAction();
                }else{
                    // Si tengo los permisos llamo a la función, y si no -> Pedir permisos "manualmente"
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                        camaraAction();
                    }else{  // Lanzamos alerta emergente (Permitir a la App acceder a las llamadas?)
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
                if (!txtNumTel.getText().toString().isEmpty()){
                    // 1. COMPROBAR QUÉ VERSIÓN DE ANDROID USO (PEDIR PERMISO EXPLÍCITO PARA POSTERIORES A ANDROID 6)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){ // API 23 (Android 6)
                        llamadaAction();
                    }else{
                        // Si tengo los permisos llamo a la función, y si no -> Pedir permisos "manualmente"
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                            llamadaAction();
                        }else{  // Lanzamos alerta emergente (Permitir a la App acceder a las llamadas?)
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION);
                        }
                    }

                }
            }
        });

    }

    private void camaraAction() {
        // Abrir cámara
        Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // startActivityForResult porque ésta nos devolverá una imagen
        startActivityForResult(intentCamara, CAMARA_ACTION);
    }


    // Método asociado a requestPermissions() -> (como onActivityResult con startActivityForResult)
    /**
     * Se ejecuta justo después de que el usuario conteste a los permisos
     * @param requestCode -> código de la petición de los permisos
     * @param permissions -> String[] con los permisos que se han solicitado
     * @param grantResults ->  int[] con los resultados de las peticiones de cada permiso
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Comprobar si has dado permisos o no (en qué posición de los array de String e int)
        if (requestCode == CALL_PERMISSION){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                llamadaAction();
            }else {
                Toast.makeText(this, "No puedo llamar sin permisos", Toast.LENGTH_SHORT).show();
            }
        }
        // Prácticamente lo mismo para la cámara
        if (requestCode == CAMERA_PERMISSION){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                camaraAction();
            }else {
                Toast.makeText(this, "No puedo usar la cámara sin permisos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Para mostrar la foto realizada en el ImageView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // imageBitmap
        if (requestCode == CAMARA_ACTION && resultCode == RESULT_OK && data != null){
            Bundle bundle = new Bundle();
            Bitmap imageBitmap = (Bitmap) bundle.get("data");
            imgCamara.setImageBitmap(imageBitmap);
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