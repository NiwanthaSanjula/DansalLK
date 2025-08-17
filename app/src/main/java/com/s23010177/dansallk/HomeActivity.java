package com.s23010177.dansallk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        LinearLayout btnDansal = findViewById(R.id.btnDansal);
        LinearLayout btnThoran = findViewById(R.id.btnThoran);

        btnDansal.setOnClickListener(v -> {
            // Open Dansal map activity
            startActivity(new Intent(this, DansalMapActivity.class));
        });

        btnThoran.setOnClickListener(v -> {
            // Open Thoran map activity
            startActivity(new Intent(this, ThoranMapActivity.class));
        });

    }
}
