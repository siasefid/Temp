package com.example.temperature;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.Manifest;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int SPEECH_REQUEST_CODE = 100;
    private static final int ROOM_2 = 20;
    private static final int ROOM_3 = 23;
    private static final int ROOM_1_SIZE = 20;
    private static final int ROOM_2_SIZE = 25;
    private static final int ROOM_3_SIZE = 30;
    private static final int OPTIMAL_TEMP = 10;
    private ToggleButton btn;
    private EditText text;
    private ImageView mic;
    private TextToSpeech toSpeech;
    private ArrayList<String> results;
    private String spokenText;
    private String outputValue = "";
    private Handler handler = new Handler();
    private LocalTime scheduleStart;
    private LocalTime scheduleEnd;

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
        voiceCommands(spokenText);
        voiceCommands(spokenText);
        btn.setChecked(false);
        btn.setBackgroundColor(Color.RED);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                textToSpeech(text.getText().toString());
            }
        },2500);
    }
    private void voiceCommands(String command) {
        String baseCommand = "schedule set";
        if (command.toLowerCase().contains(baseCommand)) {
            if (command.contains("to")) {
                scheduleSetCommand(command);
            }
            else{
                delayText("No such command");
            }
        } else {
            switch (command.toLowerCase()) {
                case "room 1":
                case "room one":
                    room1Command();
                    break;
                case "room 2":
                case "room two":
                    delayText("Room 2's temperature is: " + ROOM_2 + "°");
                    break;
                case "room 3":
                case "room three":
                    delayText("Room 3's temperature is: " + ROOM_3 + "°");
                    break;
                case "all rooms":
                    allRoomsCommand();
                    break;
                case "temperature set":
                    temperatureSetCommand();
                    break;
                case "thank you":
                    delayText("You're welcome!");
                    break;
                default:
                    delayText("No such command");
                    break;
            }
        }
    }
    private void room1Command(){
        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... params) {
                run("python listsensors.py");
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                text.setText("Room 1's temperature is: " + outputValue);
                textToSpeech("Room 1's temperature is: " + outputValue);
            }
        }.execute(1);
    }
    private void allRoomsCommand(){
        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                run("python listsensors.py");
                return null;            }
            @Override
            protected void onPostExecute(Void v) {
                text.setText("Your rooms\n\n" +
                        String.format("Room 1: " + outputValue) + "\n" +
                        "Room 2: " + ROOM_2 + "°" + "\n" +
                        "Room 3: " + ROOM_3 + "°");
                textToSpeech("Your rooms\n\n" +
                        String.format("Room 1: " + outputValue) +"\n" +
                        "Room 2: " + ROOM_2 + "°" + "\n" +
                        "Room 3: " + ROOM_3 + "°");
            }
        }.execute(1);
    }
    private void temperatureSetCommand(){
        ZoneId zoneId = ZoneId.of("GMT+1");
        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);
        LocalTime currentTime = currentDateTime.toLocalTime();

        //when no schedule has been set
        if(scheduleStart == null && scheduleEnd == null){
            delayText("The optimal temperatures have been set to your rooms based on their sizes:\n\n" +
                    "Room 1 of size " + ROOM_1_SIZE + " square meters to: " + getRoom1Temp() + "°" + "\n" +
                    "Room 2 of size " + ROOM_2_SIZE + " square meters to: " + getRoom2Temp() + "°" + "\n" +
                    "Room 3 of size " + ROOM_3_SIZE + " square meters to: " + getRoom3Temp() + "°");
            // Inside the user's schedule
        }else if (currentTime.isAfter(scheduleStart) && currentTime.isBefore(scheduleEnd)) {
            delayText("Based on your schedule, you are not at home right now.\n\n" +
                    "Therefore, all rooms will be set to a temperature of " + OPTIMAL_TEMP + "°");
            // Outside the user's schedule
        } else {
            delayText("The optimal temperatures have been set to your rooms based on their sizes:\n\n" +
                    "Room 1 of size " + ROOM_1_SIZE + " square meters to: " + getRoom1Temp() + "°" + "\n" +
                    "Room 2 of size " + ROOM_2_SIZE + " square meters to: " + getRoom2Temp() + "°" + "\n" +
                    "Room 3 of size " + ROOM_3_SIZE + " square meters to: " + getRoom3Temp() + "°");
        }
    }
    private void scheduleSetCommand(String spokenText) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H[:mm]");
        // Extract the time portion from the spoken text
        String timeText = spokenText.replaceFirst("schedule set", "").trim();

        // Split the time portion into start and end time strings
        String[] tokens = timeText.split("to");
        if (tokens.length == 2) {
            String startTime = tokens[0].trim();
            String endTime = tokens[1].trim();

            scheduleStart = LocalTime.parse(startTime, formatter);
            scheduleEnd = LocalTime.parse(endTime, formatter);
            delayText("Your schedule has been set from " + scheduleStart + " to " + scheduleEnd);
        }
        else{
            delayText("Couldn't get your schedule. Please try again");
        }
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
    private int getRoom1Temp(){
        return (ROOM_1_SIZE/2) + 8;
    }
    private int getRoom2Temp(){
        return (ROOM_2_SIZE/2) + 8;
    }
    private int getRoom3Temp(){
        return (ROOM_3_SIZE/2) + 8;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Cancel any pending Handler tasks when the activity is paused
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any pending Handler tasks when the activity is destroyed
        handler.removeCallbacksAndMessages(null);
    }

    public void run(String command) {
        StringBuilder output = new StringBuilder();
        String hostname = "raspberrypi";
        String username = "pi";
        String password = "pi";
        try {
            Connection conn = new Connection(hostname);
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (isAuthenticated == false)
                throw new IOException("Authentication failed.");
            Session sess = conn.openSession();
            sess.execCommand(command);
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                System.out.println(line);
                output.append(line);
            }
            outputValue = output.toString();

            System.out.println("ExitCode: " + sess.getExitStatus());
            sess.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(2);
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
    private void delayText(String t){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                text.setText(t);
            }
        },2500);
    }
}