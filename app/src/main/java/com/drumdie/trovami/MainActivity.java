package com.drumdie.trovami;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {
    private final static int ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE = 0;
    private GoogleApiClient googleApiClient;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private  Location userLocation;
    private ArrayList<Taco> tacoList;
    private static final int RANGE_TO_DISPLAY_TACO_MARKER_IN_METERS = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        googleApiClient= new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        tacoList = new ArrayList<>();
        tacoList.add(new Taco(40.744895,-73.980308,"Arrachera"));
        tacoList.add(new Taco(40.744895,-73.995308,"Cerdo"));
        tacoList.add(new Taco(40.751895,-73.929308,"Carne Asada"));
        tacoList.add(new Taco(40.721895,-73.982008,"Pollo"));
        tacoList.add(new Taco(40.783415,-73.980508,"Champignones"));


    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                Location userLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                getUserLastLocation(userLocation);
            } else {
                final String[] permissions = new String[]{ACCESS_FINE_LOCATION};
                requestPermissions(permissions, ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            Location userLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            getUserLastLocation(userLocation);

        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // compara requestCode dado por el user con el dado por nosotros
        if(requestCode == ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults [0] == PackageManager.PERMISSION_GRANTED){
                googleApiClient.reconnect();
            }
                else if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)){
                    AlertDialog.Builder builder= new AlertDialog.Builder(this);
                    builder.setTitle("Acceder a la ubicación del teléfono");
                    builder.setMessage("Debes aceptar el permiso para poder utilizar la app Trovami");
                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final String[] permissions = new String[]{ACCESS_FINE_LOCATION};
                            requestPermissions(permissions, ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE);
                    }
                });
                builder.show();
            }
        }
    }

    private void getUserLastLocation(Location userLocation) {
        if (userLocation != null) {
          /*  TextView locationTextView = findViewById(R.id.main_activity_location_textview);
            String longitude = String.valueOf(userLocation.getLongitude());
            String latitude = String.valueOf(userLocation.getLatitude());

            locationTextView.setText("Longitud: " + " " + longitude +", " + "Latitud: " + " " + latitude);

           */
            this.userLocation = userLocation;
            this.userLocation.setLatitude(40.741895);
            this.userLocation.setLongitude(-73.989308);

            mapFragment.getMapAsync(this);

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        BitmapDescriptor tacoMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.taco);

        ArrayList<Taco> filteredTacoList = new ArrayList<>();

        for (Taco taco : tacoList){

            Location tacolocation = new Location("");
            tacolocation.setLatitude(taco.getLatitude());
            tacolocation.setLongitude(taco.getLongitude());

            // float distanceToTaco = tacolocation.distanceTo(userLocation);  // seria float pero por posibles errores de comparacion en el
            float distanceToTaco = Math.round(tacolocation.distanceTo(userLocation));  // compilador convertirlo a int redondeando

            if(distanceToTaco < RANGE_TO_DISPLAY_TACO_MARKER_IN_METERS ){ // mts
                filteredTacoList.add(taco);
            }

        }


        LatLng userCoordinates = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(userCoordinates).title("User´s Location"));

        for (Taco taco : filteredTacoList){
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(taco.getLatitude(),taco.getLongitude()))
                    .title(taco.getFlavor())
                    .icon(tacoMarkerIcon));
        }
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(userCoordinates));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userCoordinates,12));
    }
}