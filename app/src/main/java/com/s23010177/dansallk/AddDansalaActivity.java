package com.s23010177.dansallk;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AddDansalaActivity extends AppCompatActivity {

    private static final int REQ_PICK_LOCATION = 2001;
    private static final int REQ_PICK_GALLERY = 2002;
    private static final int REQ_PICK_CAMERA = 2003;

    private EditText etDescription, etAddress, etOtherCategory;
    private Spinner spCategory;
    private Button btnPickLocation, btnSave, btnAddGallery, btnAddCamera;
    private RecyclerView rvMediaPreview;
    private TextView tvDate, tvStartTime;

    private double selectedLat, selectedLng;
    private List<String> mediaPaths = new ArrayList<>();
    private MediaAdapter mediaAdapter;

    private String selectedDate = "", selectedTime = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dansala);

        // Bind views
        etDescription = findViewById(R.id.etDescription);
        etAddress = findViewById(R.id.etAddress);
        etOtherCategory = findViewById(R.id.etOtherCategory);
        spCategory = findViewById(R.id.spCategory);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        btnSave = findViewById(R.id.btnSave);
        btnAddGallery = findViewById(R.id.btnAddGallery);
        btnAddCamera = findViewById(R.id.btnAddCamera);
        rvMediaPreview = findViewById(R.id.rvMediaPreview);
        tvDate = findViewById(R.id.tvDate);
        tvStartTime = findViewById(R.id.tvStartTime);

        etOtherCategory.setVisibility(View.GONE);

        // Spinner setup
        String[] cats = new String[]{
                "බ්‍රෙඩ්/පාන්", "බත්", "නූඩ්ල්ස්", "අයිස්ක්‍රීම්",
                "බීම", "ෆ්‍රයිඩ් රයිස්", "රොටි", "කඩල", "සව්", "සුප්", "අනෙක්"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Arrays.asList(cats));
        spCategory.setAdapter(adapter);

        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                etOtherCategory.setVisibility("අනෙක්".equals(selected) ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Media RecyclerView
        mediaAdapter = new MediaAdapter(this, mediaPaths);
        rvMediaPreview.setLayoutManager(new GridLayoutManager(this, 3));
        rvMediaPreview.setAdapter(mediaAdapter);

        // Button listeners
        btnPickLocation.setOnClickListener(v -> openPickLocation());
        btnAddGallery.setOnClickListener(v -> openGallery());
        btnAddCamera.setOnClickListener(v -> openCamera());
        btnSave.setOnClickListener(v -> saveDansala());

        // Date picker
        tvDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, d) -> {
                m += 1; // month starts from 0
                selectedDate = String.format("%04d-%02d-%02d", y, m, d);
                tvDate.setText(selectedDate);
            }, year, month, day);
            dpd.show();
        });

        // Time picker
        tvStartTime.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog tpd = new TimePickerDialog(this, (view, h, m) -> {
                selectedTime = String.format("%02d:%02d", h, m);
                tvStartTime.setText(selectedTime);
            }, hour, minute, true);
            tpd.show();
        });
    }

    private void openPickLocation() {
        Intent i = new Intent(this, PickLocationActivity.class);
        startActivityForResult(i, REQ_PICK_LOCATION);
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(i, REQ_PICK_GALLERY);
    }

    private void openCamera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, REQ_PICK_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICK_LOCATION && resultCode == Activity.RESULT_OK && data != null) {
            selectedLat = data.getDoubleExtra("latitude", 0);
            selectedLng = data.getDoubleExtra("longitude", 0);
            String address = data.getStringExtra("address");
            etAddress.setText(address);

        } else if ((requestCode == REQ_PICK_GALLERY || requestCode == REQ_PICK_CAMERA)
                && resultCode == Activity.RESULT_OK && data != null) {

            List<Uri> selectedUris = new ArrayList<>();
            if (requestCode == REQ_PICK_GALLERY) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        selectedUris.add(data.getClipData().getItemAt(i).getUri());
                    }
                } else if (data.getData() != null) selectedUris.add(data.getData());
            } else if (requestCode == REQ_PICK_CAMERA) {
                if (data.getData() != null) selectedUris.add(data.getData());
            }

            for (Uri uri : selectedUris) {
                String internalPath = saveImageToInternalStorage(uri);
                if (internalPath != null) mediaPaths.add(internalPath);
            }
            mediaAdapter.notifyDataSetChanged();
        }
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            String fileName = "img_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            try (InputStream is = getContentResolver().openInputStream(uri);
                 FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Image save failed", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void saveDansala() {
        String category = (String) spCategory.getSelectedItem();
        if ("අනෙක්".equals(category)) category = etOtherCategory.getText().toString().trim();

        String description = etDescription.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (category.isEmpty() || address.isEmpty() || selectedLat == 0 || selectedLng == 0) {
            Toast.makeText(this, "Category සහ Address අවශ්‍යයි", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "දිනය සහ ආරම්භක වේලාව අවශ්‍යයි", Toast.LENGTH_SHORT).show();
            return;
        }

        DBHelper db = new DBHelper(this);
        long id = db.addEvent(category, description, selectedLat, selectedLng, "dansal",
                category, mediaPaths, selectedDate, selectedTime);

        if (id > 0) {
            Toast.makeText(this, "Dansal එකතු කරන ලදි", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "සුරැකුම් දෝශයක් ඇතිවිය", Toast.LENGTH_SHORT).show();
        }
    }
}
