package com.example.smartgarden;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference database;
    private Switch lightSwitch;
    private Switch fanSwitch;
    private Switch pumpSwitch;
    private ProgressBar moistProgress;
    private TextView moistPercent;
    private ProgressBar humidProgress;
    private TextView humidPercent;
    private TextView lightValue;
    private TextView tempValue;
    private TextView pressureValue;
    private TextView weatherReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar =findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.showContextMenu();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.setTitle(" Smart Garden");
        myToolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        myToolbar.setOnMenuItemClickListener(item -> {
            if(item.getItemId()==R.id.action_favorite){
                Intent intent = new Intent(MainActivity.this, ChartActivity.class);
                startActivity(intent);
            }
            return false;
        });

        database = FirebaseDatabase.getInstance().getReference("SmartGarden");

        lightSwitch = findViewById(R.id.switch2);
        fanSwitch = findViewById(R.id.switch3);
        pumpSwitch = findViewById(R.id.switch4);
        moistProgress = findViewById(R.id.progress_bar_moist);
        moistPercent = findViewById(R.id.text_view_progress_moist);
        humidProgress = findViewById(R.id.progress_bar_humidity);
        humidPercent = findViewById(R.id.text_view_progress_humidity);
        lightValue = findViewById(R.id.textViewLight);
        tempValue = findViewById(R.id.textViewTemp);
        pressureValue = findViewById(R.id.textViewPressure2);
        weatherReport = findViewById(R.id.textViewHome);

        lightSwitch.setOnClickListener(view -> {
            if(lightSwitch.isChecked()){
                database.child("Devices").child("light").setValue(true);
                lightSwitch.setText("On");
            }
            else{
                database.child("Devices").child("light").setValue(false);
                lightSwitch.setText("Off");
            }
        });

        fanSwitch.setOnClickListener(view -> {
            if(fanSwitch.isChecked()){
                database.child("Devices").child("fan").setValue(true);
                fanSwitch.setText("On");
            }
            else{
                database.child("Devices").child("fan").setValue(false);
                fanSwitch.setText("Off");
            }
        });

        pumpSwitch.setOnClickListener(view -> {
            if(pumpSwitch.isChecked()){
                database.child("Devices").child("pump").setValue(true);
                pumpSwitch.setText("On");
            }
            else{
                database.child("Devices").child("pump").setValue(false);
                pumpSwitch.setText("Off");
            }
        });

        startVerifyStatusSensor();
        startVerifyStatusValues();
    }

    public void startVerifyStatusSensor(){
        database.child("Devices").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            }
            else {
                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                HashMap<String, Boolean> check_key = (HashMap<String, Boolean>) task.getResult().getValue();
                if(check_key.get("light")){
                    lightSwitch.setText("On");
                    lightSwitch.setChecked(true);
                }
                else {
                    lightSwitch.setText("Off");
                    lightSwitch.setChecked(false);
                }
                if(check_key.get("fan")){
                    fanSwitch.setText("On");
                    fanSwitch.setChecked(true);
                }
                else{
                    fanSwitch.setText("Off");
                    fanSwitch.setChecked(false);
                }
                if(check_key.get("pump")){
                    pumpSwitch.setText("On");
                    pumpSwitch.setChecked(true);
                }
                else{
                    pumpSwitch.setText("Off");
                    pumpSwitch.setChecked(false);
                }
            }
        });
    }

    public void startVerifyStatusValues(){
        DatabaseReference databaseReference = database.child("SensorsData");
        databaseReference.limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("Datasnapshot: ", String.valueOf(snapshot.getValue()));
                Log.d("Datasnapshot key: ", snapshot.getKey());
                HashMap<String, String> result = (HashMap<String, String>) snapshot.getValue();
                moistProgress.setProgress((int) Float.parseFloat(result.get("moisture")));
                moistPercent.setText(result.get("moisture")+"%");
                humidProgress.setProgress((int) Float.parseFloat(result.get("humidity")));
                humidPercent.setText(result.get("humidity")+"%");
                lightValue.setText(result.get("light") +"Lux");
                tempValue.setText(result.get("temp") +"°C");
                pressureValue.setText(result.get("pressure") +"kPa");
                ZambrettiWeatherForecasting(Double.valueOf(result.get("pressure")),result);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    Boolean pastPressure=false;
    private void ZambrettiWeatherForecasting(Double pressure,HashMap<String, String> currentValue) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("SmartGarden").child("SensorsData");
        databaseReference.limitToLast(120).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful())
                    Log.e("firebase", "Error getting data", task.getException());
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    for (DataSnapshot element : task.getResult().getChildren()) {
                        if(!pastPressure) {
                            HashMap<String, String> result = (HashMap<String, String>) element.getValue();
                            Float oldPressure = Float.parseFloat(result.get("pressure"));
                            Log.d("oldPressure", String.valueOf(oldPressure));
                            Log.d("oldPressure", element.getKey());
                            Log.d("Pressure", "value ->" + pressure);
                            Integer forecast;
                            String weatherForecast = null;
                            if (oldPressure - pressure > 0.16) {
                                forecast = (int) Math.round(127 - 0.12 * pressure * 10);
                                Log.d("Forecast", "value" + forecast);
                                switch (forecast) {
                                    case 1:
                                        weatherForecast = "Settled fine";
                                        break;
                                    case 2:
                                        weatherForecast = "Fine Weather";
                                        break;
                                    case 3:
                                        weatherForecast = "Fine, Becoming Less Settled";
                                        break;
                                    case 4:
                                        weatherForecast = "Fairly Fine, Showery Later";
                                        break;
                                    case 5:
                                        weatherForecast = "Showery, Becoming More Unsettled";
                                        break;
                                    case 6:
                                        weatherForecast = "Unsettled, Rain Later";
                                        break;
                                    case 7:
                                        weatherForecast = "Rain at Times, Worse Later";
                                        break;
                                    case 8:
                                        weatherForecast = "Rain at Times, Becoming Very Unsettled";
                                        break;
                                    case 9:
                                        weatherForecast = "Very Unsettled, Rain";
                                        break;
                                }
                            } else if (pressure - oldPressure > 0.16) {
                                forecast = (int) Math.round(185 - 0.16 * pressure * 10);
                                Log.d("Forecast", "value" + forecast);
                                switch (forecast) {
                                    case 20:
                                        weatherForecast = "Settled fine";
                                        break;
                                    case 21:
                                        weatherForecast = "Fine Weather";
                                        break;
                                    case 22:
                                        weatherForecast = "Becoming Fine";
                                        break;
                                    case 23:
                                        weatherForecast = "Fairly Fine, Improving";
                                        break;
                                    case 24:
                                        weatherForecast = "Fairly Fine, Possibly Showers Early";
                                        break;
                                    case 25:
                                        weatherForecast = "Showery Early, Improving";
                                        break;
                                    case 26:
                                        weatherForecast = "Changeable, Mending";
                                        break;
                                    case 27:
                                        weatherForecast = "Rather Unsettled, Clearing Later";
                                        break;
                                    case 28:
                                        weatherForecast = "Unsettled, Probably Improving";
                                        break;
                                    case 29:
                                        weatherForecast = "Unsettled, Short Fine Intervals";
                                        break;
                                    case 30:
                                        weatherForecast = "Very Unsettled, Finer at Times";
                                        break;
                                    case 31:
                                        weatherForecast = "Stormy, Possibly Improving";
                                        break;
                                    case 32:
                                        weatherForecast = "Stormy, Much Rain\n";
                                        break;
                                }
                            } else {
                                forecast = (int) Math.round(144 - 0.13 * pressure * 10);
                                Log.d("Forecast", "value" + forecast);
                                switch (forecast) {
                                    case 10:
                                        weatherForecast = "Settled fine";
                                        break;
                                    case 11:
                                        weatherForecast = "Fine Weather";
                                        break;
                                    case 12:
                                        weatherForecast = "Fine, Possibly Showers";
                                        break;
                                    case 13:
                                        weatherForecast = "Fairly Fine, Showers Likely";
                                        break;
                                    case 14:
                                        weatherForecast = "Showery, Bright Intervals";
                                        break;
                                    case 15:
                                        weatherForecast = "Changeable, Some Rain";
                                        break;
                                    case 16:
                                        weatherForecast = "Unsettled, Rain at Times";
                                        break;
                                    case 17:
                                        weatherForecast = "Rain at Frequent Intervals";
                                        break;
                                    case 18:
                                        weatherForecast = "Very Unsettled, Rain";
                                        break;
                                    case 19:
                                        weatherForecast = "Stormy, Much Rain";
                                        break;
                                }
                            }
                            weatherReport.setText(weatherForecast);
                            if (Float.parseFloat(currentValue.get("humidity")) >= 60) {
                                weatherReport.append("\n High humidity, better turn on the fan!");
                            }
                            if (Float.parseFloat(currentValue.get("light")) <= 100) {
                                weatherReport.append("\n Low Light, better turn on the lights!");
                            }
                            if (Float.parseFloat(currentValue.get("moisture")) <= 10) {
                                weatherReport.append("\n Low Moisture, better turn on the water pump!");
                            }
                            pastPressure=true;
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


}