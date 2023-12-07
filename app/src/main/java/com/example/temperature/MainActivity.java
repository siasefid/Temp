package com.example.temperature;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.Manifest;

public class MainActivity extends AppCompatActivity {
    private Button knapp;
    private EditText ed;

    private EditText hej;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ed =  findViewById(R.id.text);
        imageView = findViewById(R.id.imageView);
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO)!=
                PackageManager.PERMISSION_GRANTED){

        }

    }
}