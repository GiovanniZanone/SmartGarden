package com.example.smartgarden;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChartActivity extends AppCompatActivity {

    DatabaseReference mPostReference;
    private GraphView graph;
    private double i = 1.0;
    private LineGraphSeries<DataPoint> series_moisture;
    private LineGraphSeries<DataPoint> series_humidity;
    private LineGraphSeries<DataPoint> series_temperature;
    private LineGraphSeries<DataPoint> series_light;
    private LineGraphSeries<DataPoint> series_pressure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar2);
        mPostReference = FirebaseDatabase.getInstance().getReference("SmartGarden");

        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        graph = findViewById(R.id.Graph);
        Viewport vp = graph.getViewport();
        vp.setXAxisBoundsManual(true);
        vp.setMaxX(200);
        vp.setMinX(0);

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(135);
        series_moisture = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0,0)
        });

        series_humidity = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0,0)
        });

        series_temperature = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0,0)
        });

        series_light = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0,0)
        });

        series_pressure = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0,0)
        });

        DatabaseReference databaseReference = mPostReference.child("SensorsData");
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("Datasnapshot: ", "String" + snapshot.getValue());
                HashMap<String, String> result = (HashMap<String, String>) snapshot.getValue();
                series_moisture.appendData(new DataPoint(i, Double.parseDouble(result.get("moisture"))),false, 1000, false);
                series_humidity.appendData(new DataPoint(i, Double.parseDouble(result.get("humidity"))),false, 1000, false);
                series_temperature.appendData(new DataPoint(i, Double.parseDouble(result.get("temp"))),false, 1000, false);
                series_light.appendData(new DataPoint(i, Double.parseDouble(result.get("light"))),false, 1000, false);
                series_pressure.appendData(new DataPoint(i, Double.parseDouble(result.get("pressure"))),false, 1000, false);
                graph.invalidate();
                graph.getViewport().scrollToEnd();
                i++;
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

    public void checkChart(View view){
        boolean checked = ((RadioButton)view).isChecked();
        switch (view.getId()){
            case R.id.radioButton:
                if(checked){
                    moisture_chart();
                    break;
                }
            case R.id.radioButton2:
                if(checked){
                    humidity_chart();
                    break;
                }
            case R.id.radioButton3:
                if(checked){
                    temperature_chart();
                    break;
                }
            case R.id.radioButton4:
                if(checked){
                    light_chart();
                    break;
                }
            case R.id.radioButton5:
                if(checked){
                    pressure_chart();
                    break;
                }
        }
    }

    public void moisture_chart(){
        graph.removeAllSeries();
        graph.addSeries(series_moisture);
    }

    private void humidity_chart() {
        graph.removeAllSeries();
        graph.addSeries(series_humidity);
    }

    private void temperature_chart() {
        graph.removeAllSeries();
        graph.addSeries(series_temperature);
    }

    private void light_chart() {
        graph.removeAllSeries();
        graph.addSeries(series_light);
    }

    private void pressure_chart() {
        graph.removeAllSeries();
        graph.addSeries(series_pressure);
    }
}
