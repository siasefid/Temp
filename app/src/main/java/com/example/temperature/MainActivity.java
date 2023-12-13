package com.example.temperature;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.Manifest;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int SPEECH_REQUEST_CODE = 100 ;
    private static final int ROOM_2 = 20;
    private static final int ROOM_3 = 23;

    private ToggleButton btn;
    private EditText text;
    private ImageView mic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text =  findViewById(R.id.text);
        mic = findViewById(R.id.imageView);
        btn = findViewById(R.id.toggle);
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO)!=
                PackageManager.PERMISSION_GRANTED){
        }
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSpeechRecognition("Speak");
                btn.setChecked(true);
                btn.setBackgroundColor(Color.GREEN);
            }
        });

    }


    private void startSpeechRecognition(String prompt) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);


        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Speech recognition not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        String spokenText = results.get(0);

        if (spokenText.toLowerCase().indexOf("room 2") !=-1) {
            text.setText("Room 2's temperature is: " + ROOM_2 + "°C");
        }
        else if (spokenText.toLowerCase().indexOf("room 3") !=-1) {
            text.setText("Room 3's temperature is: " + ROOM_3 + "°C");
        }
        else if (spokenText.toLowerCase().indexOf("all") !=-1) {
            text.setText("Your rooms" + "\n" + "\n" + "Room 2: " + ROOM_2 + "°C" + "\n" + "Room 3: " + ROOM_3 + "°C");
        }
        else if (spokenText.toLowerCase().indexOf("thank") !=-1) {
            text.setText("You're welcome! :)");
        }
        else {
            text.setText("command not found");
        }
        btn.setChecked(false);
        btn.setBackgroundColor(Color.RED);
    }
}