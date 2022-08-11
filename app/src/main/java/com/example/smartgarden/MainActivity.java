package com.example.smartgarden;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

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
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("Datasnapshot: ", "String"+snapshot.getValue());
                HashMap<String, String> result = (HashMap<String, String>) snapshot.getValue();
                moistProgress.setProgress((int) Float.parseFloat(result.get("moisture")));
                moistPercent.setText(result.get("moisture")+"%");
                humidProgress.setProgress((int) Float.parseFloat(result.get("humidity")));
                humidPercent.setText(result.get("humidity")+"%");
                lightValue.setText(result.get("light") +"Lux");
                tempValue.setText(result.get("temp") +"°C");
                pressureValue.setText(result.get("pressure") +"kPa");
                if (Float.parseFloat(result.get("humidity")) >= 60 && Float.parseFloat(result.get("temp")) >= 15) {
                     weatherReport.setText("It is very humid outside");
                }else if (Float.parseFloat(result.get("temp")) >= 18 && (Float.parseFloat(result.get("light")) >= 700)) {
                    weatherReport.setText("Warm and sunny");
                }else if (Float.parseFloat(result.get("temp")) >= 18 && (Float.parseFloat(result.get("light")) < 700)) {
                    weatherReport.setText("Warm and cloudy");
                }else if (Float.parseFloat(result.get("temp")) < 18 && (Float.parseFloat(result.get("light")) >= 700)) {
                    weatherReport.setText("A little cold, but sunny outside");
                }else if (Float.parseFloat(result.get("temp")) < 18 && (Float.parseFloat(result.get("light")) < 700)) {
                    weatherReport.setText("A little cold and cloudy");
                }else{
                    weatherReport.setText("Weather is normal");
                }
                if (Float.parseFloat(result.get("humidity")) >= 60) {
                    weatherReport.append("\n High humidity,better turn on the fan!");
                }
                if (Float.parseFloat(result.get("light")) <= 100) {
                    weatherReport.append("\n Low Light,better turn on the lights!");
                }
                if (Float.parseFloat(result.get("moisture")) <= 10) {
                    weatherReport.append("\n Low Moisture,better turn on the water pump!");
                }
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
        };
        databaseReference.addChildEventListener(childEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


}