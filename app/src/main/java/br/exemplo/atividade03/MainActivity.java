package br.exemplo.atividade3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.Manifest;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static String rotaImagens = "/data/user/0/br.exemplo.atividade3/app_imageDir";
    private LocationManager locationManager;
    private LocationListener locationListener;
    Double latitude;
    Double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissao();
        findViewById(R.id.buttonCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
    }



    private void permissao() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else
            configService();
    }




    public void configService() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    latitude = location.getLatitude();
                    System.out.println();
                    longitude = location.getLongitude();
                    System.out.println(longitude);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 1,
                    locationListener);
        } catch (SecurityException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String
            permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    configService();
                } else {
                    Toast.makeText(this, "Sem permissão do gps", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }



    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ImageView imageView = findViewById(R.id.imageViewLeo);
            imageView.setImageBitmap(imageBitmap);

            String base64Image = encodeImageToBase64(imageBitmap);
            requestFalsa(latitude,longitude, base64Image);

            System.err.println("base64Image: " + base64Image);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();


                requestFalsa(latitude, longitude, base64Image);
            }
        }
    }

    private String encodeImageToBase64(Bitmap imageBitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void requestFalsa(double latitude, double longitude, String base64Image) {

        String json = String.format("{\"latitude\": %f, \"longitude\": %f, \"image\": \"%s\"}",
                latitude, longitude, base64Image);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder().url("https://exemplo.com/requisicao-fake").post(body).build();

        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {

                Log.d("HTTP", "sucesso");
            } else {

                Log.d("HTTP", "erro");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}