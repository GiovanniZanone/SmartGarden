package com.example.smartgarden;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ChartActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DatabaseReference mPostReference;
    private GraphView graph;
    private double i = 1.0;
    private LineGraphSeries<DataPoint> series_moisture;
    private LineGraphSeries<DataPoint> series_humidity;
    private LineGraphSeries<DataPoint> series_temperature;
    private LineGraphSeries<DataPoint> series_light;
    private LineGraphSeries<DataPoint> series_pressure;
    private RadioButton elementSelected;
    private Calendar mCalendar;
    private DateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss:SS");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar2);
        mPostReference = FirebaseDatabase.getInstance().getReference("SmartGarden");

        setSupportActionBar(myToolbar);
        Spinner spinner = (Spinner) findViewById(R.id.simpleSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.period_length_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner and add listener
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        //select moisture
        elementSelected=findViewById(R.id.radioButton);
        elementSelected.setChecked(true);

        graph = findViewById(R.id.Graph);
        Viewport vp = graph.getViewport();
        vp.setXAxisBoundsManual(true);
        vp.setMaxX(200);
        vp.setMinX(0);

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
        Query query = databaseReference.limitToFirst(60);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot element : snapshot.getChildren()) {
                    HashMap<String, String> result = (HashMap<String, String>) element.getValue();
                    series_moisture.appendData(new DataPoint(Double.parseDouble(element.getKey()), Double.parseDouble(result.get("moisture"))),false, 60, false);
                    series_humidity.appendData(new DataPoint(Double.parseDouble(element.getKey()), Double.parseDouble(result.get("humidity"))),false, 60, false);
                    series_temperature.appendData(new DataPoint(Double.parseDouble(element.getKey()), Double.parseDouble(result.get("temp"))),false, 60, false);
                    series_light.appendData(new DataPoint(Double.parseDouble(element.getKey()), Double.parseDouble(result.get("light"))),false, 60, false);
                    series_pressure.appendData(new DataPoint(Double.parseDouble(element.getKey()), Double.parseDouble(result.get("pressure"))),false, 60, false);
                    graph.invalidate();
                    graph.getViewport().scrollToEnd();
                    i++;
                }
                graph.removeAllSeries();
                graph.addSeries(series_moisture);
                Log.d("series", String.valueOf(series_moisture));
                graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if (isValueX) {
                            mCalendar.setTimeInMillis((long) value);
                            System.out.println(mDateFormat.format(mCalendar.getTimeInMillis()));
                            return mDateFormat.format(mCalendar.getTimeInMillis());
                        } else {
                            return super.formatLabel(value, isValueX);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));

    }

    private void humidity_chart() {
        graph.removeAllSeries();
        graph.addSeries(series_humidity);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));

    }

    private void temperature_chart() {
        graph.removeAllSeries();
        graph.addSeries(series_temperature);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));

    }

    private void light_chart() {
        graph.removeAllSeries();
        graph.addSeries(series_light);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));

    }

    private void pressure_chart() {
        graph.removeAllSeries();
        graph.addSeries(series_pressure);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Object selectedItem = adapterView.getSelectedItem();
        switch (selectedItem.toString()){
            case "last hour":
                Log.d("ITEM SELECTED", selectedItem.toString());
                break;
            case "last six hours":
                Log.d("ITEM SELECTED", selectedItem.toString());
                break;
            case "last day":
                Log.d("ITEM SELECTED", selectedItem.toString());
                break;
            case "last week":
                Log.d("ITEM SELECTED", selectedItem.toString());
                break;
            case "last month":
                Log.d("ITEM SELECTED", selectedItem.toString());
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
