package com.s23010177.dansallk;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Arrays;
import java.util.List;

public class DansalMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final LatLng SRI_LANKA = new LatLng(7.8731, 80.7718);
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Use FusedLocationProviderClient instead of LocationManager for better accuracy
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dansal_map);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageButton btnMapType = findViewById(R.id.btnMapType);
        btnMapType.setOnClickListener(v -> showMapTypeDialog());

        ImageButton btnMyLocation = findViewById(R.id.btnMyLocation);
        btnMyLocation.setOnClickListener(v -> moveToCurrentLocation());

        Button btnAdd = findViewById(R.id.btnAddDansala);
        Button btnFilter = findViewById(R.id.btnFilter);

        btnAdd.setOnClickListener(v -> startActivityForResult(new Intent(this, AddDansalaActivity.class), 101));
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        centerOnSriLanka();
        loadMarkers(null);

        mMap.setOnMarkerClickListener(marker -> {
            DBHelper db = new DBHelper(this);
            DBHelper.Event event = db.getEventById(getEventIdFromMarker(marker));
            if (event != null) showEventPopup(event);
            return true; // consume click
        });
    }

    private void centerOnSriLanka() {
        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SRI_LANKA, 7.0f));
        }
    }

    private void moveToCurrentLocation() {
        if (mMap == null) return;

        // Check if location permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Get last known location using FusedLocationProviderClient
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null && mMap != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));

                            // Optional: Add a marker at current location
                            // mMap.addMarker(new MarkerOptions()
                            //         .position(currentLocation)
                            //         .title("My Location")
                            //         .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                            Toast.makeText(DansalMapActivity.this, "Located successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DansalMapActivity.this, "Unable to get current location. Try again.", Toast.LENGTH_SHORT).show();

                            // Fallback: Try with LocationManager
                            tryLocationManagerFallback();
                        }
                    }
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Fallback: Try with LocationManager
                    tryLocationManagerFallback();
                });
    }

    private void tryLocationManagerFallback() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager != null) {
                Location location = null;

                // Try GPS first, then Network
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }

                if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                if (location != null && mMap != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                    Toast.makeText(this, "Located using fallback method!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Location not available. Please check GPS/Location services.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try to get location again
                moveToCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required to use this feature", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (mMap != null) {
                loadMarkers(null);
            }
        }
    }

    private void loadMarkers(String category) {
        if (mMap == null) return;

        mMap.clear();
        DBHelper db = new DBHelper(this);
        List<DBHelper.Event> events;

        if (category == null || category.equals("සියල්ල")) {
            events = db.getEventsByType("dansal");
        } else if (category.equals("අනෙක්")) {
            List<String> predefinedCats = Arrays.asList(
                    "බ්‍රෙඩ්/පාන්", "බත්", "නූඩ්ල්ස්", "අයිස්ක්‍රීම්", "බීම", "ෆ්‍රයිඩ් රයිස්",
                    "රොටි", "කඩල", "සව්", "සුප්"
            );
            events = db.getDansalOtherCategories(predefinedCats);
        } else {
            events = db.getDansalByCategory(category);
        }

        for (DBHelper.Event e : events) {
            LatLng pos = new LatLng(e.lat, e.lng);

            float color;
            switch (e.category != null ? e.category : "") {
                case "බ්‍රෙඩ්/පාන්": color = BitmapDescriptorFactory.HUE_ORANGE; break;
                case "බත්": color = BitmapDescriptorFactory.HUE_RED; break;
                case "නූඩ්ල්ස්": color = BitmapDescriptorFactory.HUE_YELLOW; break;
                case "අයිස්ක්‍රීම්": color = BitmapDescriptorFactory.HUE_CYAN; break;
                case "බීම": color = BitmapDescriptorFactory.HUE_BLUE; break;
                case "ෆ්‍රයිඩ් රයිස්": color = BitmapDescriptorFactory.HUE_GREEN; break;
                case "රොටි": color = BitmapDescriptorFactory.HUE_ROSE; break;
                case "කඩල": color = BitmapDescriptorFactory.HUE_MAGENTA; break;
                case "සව්": color = BitmapDescriptorFactory.HUE_AZURE; break;
                case "සුප්": color = BitmapDescriptorFactory.HUE_VIOLET; break;
                default: color = BitmapDescriptorFactory.HUE_VIOLET; // "අනෙක්" or null
            }

            // Add marker and store event ID in tag
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(e.name + (e.category != null ? " (" + e.category + ")" : ""))
                    .snippet(e.description != null ? e.description : "")
                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
            if (marker != null) marker.setTag(e.id);
        }

        // Set a marker click listener to show popup
        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag != null) {
                int eventId = (int) tag;
                DBHelper.Event event = db.getEventById(eventId);
                if (event != null) showEventPopup(event);
            }
            return true; // consume click
        });

        if (!events.isEmpty()) {
            DBHelper.Event last = events.get(0);
            // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(last.lat, last.lng), 14f));
        }
    }

    private int getEventIdFromMarker(Marker marker) {
        Object tag = marker.getTag();
        return tag != null ? (int) tag : -1;
    }

    private void showFilterDialog() {
        final String[] cats = new String[]{
                "සියල්ල", "බ්‍රෙඩ්/පාන්", "බත්", "නූඩ්ල්ස්", "අයිස්ක්‍රීම්",
                "බීම", "ෆ්‍රයිඩ් රයිස්", "රොටි", "කඩල", "සව්", "සුප්", "අනෙක්"
        };
        new android.app.AlertDialog.Builder(this)
                .setTitle("ප්‍රවර්ගය තෝරන්න")
                .setItems(cats, (d, which) -> loadMarkers(cats[which]))
                .show();
    }

    private void showMapTypeDialog() {
        if (mMap == null) return;
        final String[] mapTypes = {"Normal", "Satellite", "Terrain", "Hybrid"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Select Map Type")
                .setItems(mapTypes, (dialog, which) -> {
                    if (mMap == null) return;
                    switch (which) {
                        case 0: mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); break;
                        case 1: mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); break;
                        case 2: mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN); break;
                        case 3: mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); break;
                    }
                }).show();
    }

    private void showEventPopup(DBHelper.Event event) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_dansal_detail, null);
        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();

        // Value fields
        TextView tvCategory = view.findViewById(R.id.tvCategory);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvStartTime = view.findViewById(R.id.tvStartTime);
        TextView tvNoImages = view.findViewById(R.id.tvNoImages);
        RecyclerView rvImages = view.findViewById(R.id.rvImages);

        // Close X button
        ImageButton btnClose = view.findViewById(R.id.btnClosePopup);

        // Set values
        tvCategory.setText(event.category != null ? event.category : "No category");
        tvDescription.setText(event.description != null && !event.description.isEmpty() ? event.description : "No description");
        tvDate.setText(event.createdAt > 0 ? android.text.format.DateFormat.format("yyyy-MM-dd", event.createdAt) : "No date");
        tvStartTime.setText(event.startTime != null && !event.startTime.isEmpty() ? event.startTime : "No start time");

        // Images
        if (event.images == null || event.images.isEmpty()) {
            tvNoImages.setVisibility(View.VISIBLE);
            rvImages.setVisibility(View.GONE);
        } else {
            tvNoImages.setVisibility(View.GONE);
            rvImages.setVisibility(View.VISIBLE);
            MediaAdapter adapter = new MediaAdapter(this, event.images);
            rvImages.setLayoutManager(new GridLayoutManager(this, 3));
            rvImages.setAdapter(adapter);
        }

        // Close X click listener
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}