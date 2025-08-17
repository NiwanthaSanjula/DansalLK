package com.s23010177.dansallk;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.List;

public class DansalaDetailsActivity extends AppCompatActivity {

    TextView txtName, txtLocation, txtStart, txtEnd, txtDescription;
    LinearLayout imagesContainer;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dansala_details);

        dbHelper = new DBHelper(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        txtName = findViewById(R.id.txtName);
        txtLocation = findViewById(R.id.txtLocation);
        txtStart = findViewById(R.id.txtStartTime);
        txtEnd = findViewById(R.id.txtEndTime);
        txtDescription = findViewById(R.id.txtDescription);
        imagesContainer = findViewById(R.id.imagesContainer);

        String dansalaId = getIntent().getStringExtra("dansalaId");
        if (dansalaId == null) {
            Toast.makeText(this, "No Dansala ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDansalaDetails(dansalaId);
    }

    private void loadDansalaDetails(String dansalaId) {
        DBHelper.Event dansala = dbHelper.getEventById(Integer.parseInt(dansalaId));

        if (dansala == null) {
            Toast.makeText(this, "No details found for this Dansala", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtName.setText(dansala.name);
        txtLocation.setText(dansala.location);
        txtStart.setText(dansala.startTime);
        txtEnd.setText(dansala.endTime);
        txtDescription.setText(dansala.description);

        imagesContainer.removeAllViews();
        List<String> images = dansala.images;
        if (images != null && !images.isEmpty()) {
            for (String imgUriStr : images) {
                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300);
                params.setMargins(8, 8, 8, 8);
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(this)
                        .load(Uri.parse(imgUriStr))
                        .placeholder(android.R.color.darker_gray)
                        .into(imageView);
                imagesContainer.addView(imageView);
            }
        }
    }
}
