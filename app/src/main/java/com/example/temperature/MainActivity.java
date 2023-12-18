package com.example.temperature;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.Manifest;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int SPEECH_REQUEST_CODE = 100;
    private static final int ROOM_2 = 20;
    private static final int ROOM_3 = 23;
    private ToggleButton btn;
    private EditText text;
    private ImageView mic;
    private TextToSpeech toSpeech;
    private ArrayList<String> results;
    private String spokenText;
    private final int optimaltemp1 = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = findViewById(R.id.text);
        mic = findViewById(R.id.imageView);
        btn = findViewById(R.id.toggle);
        toSpeech = new TextToSpeech(this, this);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
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
        results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        spokenText = results.get(0);
        LocalTime currentTime = LocalTime.now();


        if (spokenText.toLowerCase().indexOf("room 2") != -1) {
            text.setText("Room 2's temperature is: " + ROOM_2 + "°C");
        } else if (spokenText.toLowerCase().indexOf("room 3") != -1) {
            text.setText("Room 3's temperature is: " + ROOM_3 + "°C");
        } else if (spokenText.toLowerCase().indexOf("all") != -1) {
            text.setText("Your rooms" + "\n" + "\n" + "Room 2: " + ROOM_2 + "°C" + "\n" + "Room 3: " + ROOM_3 + "°C");
        } else if (spokenText.toLowerCase().indexOf("set temp") != -1) {
            if (currentTime.isAfter(LocalTime.of(8, 0)) && currentTime.isBefore(LocalTime.of(16, 0))) {
                text.setText("Temperature has been sat based on your scheduleTemp which is" + optimaltemp1 + "°C");
            } else {
                text.setText("what temp");
            }
        } else if (spokenText.toLowerCase().indexOf("thank") != -1) {
            text.setText("You're welcome! :)");
        } else {
            text.setText("command not found");
        }
        textToSpeech(text.getText().toString());
        btn.setChecked(false);
        btn.setBackgroundColor(Color.RED);
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            int b = toSpeech.setLanguage(Locale.US);
            if (b == TextToSpeech.LANG_MISSING_DATA || b == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TextToSpeech", "Language is not supported");
            } else {
                Log.e("TextToSpeech", "failed");

            }
        }
    }

    private void textToSpeech(String t) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toSpeech.setSpeechRate(0.5F);
            toSpeech.speak(t, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            toSpeech.speak(t, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}