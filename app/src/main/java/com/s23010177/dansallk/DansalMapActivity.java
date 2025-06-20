package com.s23010177.dansallk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.util.List;

public class DansalMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dansal_map);

        dbHelper = new DBHelper(this);

        ImageButton btnMapType = findViewById(R.id.btnMapType);
        btnMapType.setOnClickListener(v -> showMapTypeDialog());

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        Button btnAddDansala = findViewById(R.id.btnAddDansala);
        btnAddDansala.setOnClickListener(v -> {
            Intent intent = new Intent(DansalMapActivity.this, AddDansalaActivity.class);
            startActivity(intent);
        });

        Button btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            // TODO: Implement filter dialog
            Toast.makeText(this, "Filter clicked (not implemented)", Toast.LENGTH_SHORT).show();
        });

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(6.9271, 79.8612), 7)); // Sri Lanka default

        mMap.getUiSettings().setZoomControlsEnabled(true);
        enableMyLocation();

        // Load Dansala markers from DB
        loadDansalaMarkers();

        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof String) {
                Intent intent = new Intent(DansalMapActivity.this, DansalaDetailsActivity.class);
                intent.putExtra("dansalaId", (String) tag);  // Pass ID as String extra
                startActivity(intent);
            }
            return true;
        });
    }

    private void loadDansalaMarkers() {
        mMap.clear();
        List<DBHelper.Dansala> dansalaList = dbHelper.getAllDansalas();
        if (dansalaList == null || dansalaList.isEmpty()) {
            LatLng defaultLocation = new LatLng(6.9271, 79.8612);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(defaultLocation)
                    .title("No Dansalas found"));
            if (marker != null) marker.setTag(null);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 7.5f));
            return;
        }

        for (DBHelper.Dansala dansala : dansalaList) {
            LatLng position = new LatLng(dansala.latitude, dansala.longitude);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(dansala.name));
            if (marker != null) marker.setTag(String.valueOf(dansala.id));
        }

        DBHelper.Dansala first = dansalaList.get(0);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(first.latitude, first.longitude), 8f));
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    // Handle location permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMapTypeDialog() {
        final String[] mapTypes = {"Default", "Satellite", "Terrain"};
        final int[] mapTypeValues = {
                GoogleMap.MAP_TYPE_NORMAL,
                GoogleMap.MAP_TYPE_HYBRID,
                GoogleMap.MAP_TYPE_TERRAIN,
        };

        new android.app.AlertDialog.Builder(this)
                .setTitle("Select Map Type")
                .setItems(mapTypes, (dialog, which) -> mMap.setMapType(mapTypeValues[which]))
                .show();
    }
}
