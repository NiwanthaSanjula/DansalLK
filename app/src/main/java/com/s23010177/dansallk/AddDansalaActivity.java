package com.s23010177.dansallk;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddDansalaActivity extends AppCompatActivity {

    private EditText etName, etLocation, etDate, etStartTime, etEndTime, etDescription;
    private LinearLayout imagePreviewContainer;
    private static final int IMAGE_PICK_CODE = 101;
    private static final int LOCATION_PERMISSION_CODE = 102;

    private ArrayList<Uri> imageUris = new ArrayList<>();
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dansala);

        // Initialize views
        etName = findViewById(R.id.etName);
        etLocation = findViewById(R.id.etLocation);
        etDate = findViewById(R.id.etDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etDescription = findViewById(R.id.etDescription);
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);

        Button btnUseGPS = findViewById(R.id.btnUseGPS);
        Button btnChooseImage = findViewById(R.id.btnChooseImage);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        // Initialize DB helper
        dbHelper = new DBHelper(this);

        // Set listeners
        //btnUseGPS.setOnClickListener(v -> getGPSLocation());

        btnUseGPS.setOnClickListener(v -> {
            Intent intent = new Intent(AddDansalaActivity.this, PickLocationActivity.class);
            startActivityForResult(intent, 1234);
        });
        btnChooseImage.setOnClickListener(v -> chooseImages());
        btnSubmit.setOnClickListener(v -> submitDansala());
    }

    // Request image picker
    private void chooseImages() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), IMAGE_PICK_CODE);
    }

    // Get GPS coordinates
    private void getGPSLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            GPSTracker gps = new GPSTracker(this);
            if (gps.canGetLocation()) {
                currentLatitude = gps.getLatitude();
                currentLongitude = gps.getLongitude();
                etLocation.setText("Lat: " + currentLatitude + ", Lon: " + currentLongitude);
            } else {
                gps.showSettingsAlert();
            }
        }
    }

    // Save Dansala details to SQLite
    private void submitDansala() {
        String name = etName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String start = etStartTime.getText().toString().trim();
        String end = etEndTime.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "නම සහ ස්ථානය අවශ්‍යයි", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> imagePaths = new ArrayList<>();
        for (Uri uri : imageUris) {
            imagePaths.add(uri.toString());
        }

        boolean success = dbHelper.insertDansala(name, location, date, start, end, description, currentLatitude, currentLongitude, imagePaths);
        if (success) {
            Toast.makeText(this, "දන්සල සුරකිණි!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "දත්ත සුරැකීම අසාර්ථකයි", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle image picking
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUris.clear();
            imagePreviewContainer.removeAllViews();

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);
                    addImageToPreview(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                imageUris.add(imageUri);
                addImageToPreview(imageUri);
            }
        }

        // Handle location selection from PickLocationActivity
        if (requestCode == 1234 && resultCode == RESULT_OK && data != null) {
            currentLatitude = data.getDoubleExtra("latitude", 0.0);
            currentLongitude = data.getDoubleExtra("longitude", 0.0);
            String address = data.getStringExtra("address");
            etLocation.setText(address != null ? address : "Lat: " + currentLatitude + ", Lon: " + currentLongitude);
        }
    }


    // Show selected image thumbnails
    private void addImageToPreview(Uri imageUri) {
        try {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
            params.setMargins(8, 8, 8, 8);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            imageView.setImageBitmap(bitmap);
            imagePreviewContainer.addView(imageView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handle permission result for GPS
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getGPSLocation();
            } else {
                Toast.makeText(this, "GPS අවසරය අවශ්‍යයි", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
