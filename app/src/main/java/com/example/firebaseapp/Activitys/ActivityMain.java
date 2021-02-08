package com.example.firebaseapp.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.firebaseapp.R;

public class ActivityMain extends AppCompatActivity {

    //views
    Button main_btn_register, main_btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        main_btn_login = findViewById(R.id.main_btn_login);
        main_btn_register = findViewById(R.id.main_btn_reguster);

        //handle register button click
        main_btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start register activity
                startActivity(new Intent(ActivityMain.this, ActivityRegister.class));
            }
        });

        //handle login button click
        main_btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start Login activity
                startActivity(new Intent(ActivityMain.this, ActivityLogin.class));
            }
        });
    }
}