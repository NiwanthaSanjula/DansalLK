package com.s23010177.dansallk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    ImageView roundedBackgroundImage;
    LinearLayout btnDansal, btnThoran;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_home);

        // Reference views
        roundedBackgroundImage = findViewById(R.id.roundedBackgroundImage);
        btnDansal = findViewById(R.id.btnDansal);
        btnThoran = findViewById(R.id.btnThoran);

        // Enable rounded clipping for top corners (works on API 21+)
        roundedBackgroundImage.setClipToOutline(true);

        // Button listeners
        btnDansal.setOnClickListener(v -> openDansalMap());
        btnThoran.setOnClickListener(v -> openThoranMap());
    }

    // Called when Dansal button is clicked
    public void openDansalMap() {
        Intent intent = new Intent(this, DansalMapActivity.class); // Replace with actual activity
        startActivity(intent);
    }

    // Called when Thoran button is clicked
    public void openThoranMap() {
        Intent intent = new Intent(this, DansalMapActivity.class); // Replace with actual activity
        startActivity(intent);
    }
}
