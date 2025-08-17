package com.s23010177.dansallk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);


        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 3000); // 3-second delay


        //Add lantern waving animation
        ImageView lantern1 = findViewById(R.id.lantern1);
        ImageView lantern2 = findViewById(R.id.lantern2);

        Animation wave = AnimationUtils.loadAnimation(this, R.anim.wave);
        Animation wave2 = AnimationUtils.loadAnimation(this, R.anim.wave2);
        lantern1.startAnimation(wave);
        lantern2.startAnimation(wave2);






    }
}