package com.example.sbarai.openkart.Models;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.sbarai.openkart.R;
import com.example.sbarai.openkart.SplashScreen;

public class LoginActivity extends AppCompatActivity {

    Button bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        bt = (Button)findViewById(R.id.button2);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SplashScreen.class);
                startActivity(intent);
            }
        });
    }
}
