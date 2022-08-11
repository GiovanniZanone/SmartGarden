package com.example.smartgarden;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Date;
import java.util.HashMap;

public class ChartActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DatabaseReference mPostReference;
    private GraphView graph;
    private LineGraphSeries<DataPoint> series_moisture;
    private LineGraphSeries<DataPoint> series_humidity;
    private LineGraphSeries<DataPoint> series_temperature;
    private LineGraphSeries<DataPoint> series_light;
    private LineGraphSeries<DataPoint> series_pressure;
    private RadioButton elementSelected;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        Toolbar myToolbar = findViewById(R.id.my_toolbar2);
        mPostReference = FirebaseDatabase.getInstance().getReference("SmartGarden");

        setSupportActionBar(myToolbar);
        Spinner spinner = findViewById(R.id.simpleSpinner);
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
        radioGroup=findViewById(R.id.radioGroup);

        graph = findViewById(R.id.Graph);
        Viewport vp = graph.getViewport();
        vp.setXAxisBoundsManual(true);
        // activate horizontal zooming and scrolling
        vp.setScalable(true);
        // activate horizontal scrolling
        vp.setScrollable(true);
        // activate horizontal and vertical zooming and scrolling
        vp.setScalableY(true);
        // activate vertical scrolling
        vp.setScrollableY(true);

        graph.getGridLabelRenderer().setHorizontalLabelsAngle(120);
        series_moisture = new LineGraphSeries<>();

        series_humidity = new LineGraphSeries<>();

        series_temperature = new LineGraphSeries<>();

        series_light = new LineGraphSeries<>();

        series_pressure = new LineGraphSeries<>();

        DatabaseReference databaseReference = mPostReference.child("SensorsData");
        databaseReference.limitToLast(60).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful())
                Log.e("firebase", "Error getting data", task.getException());
            else {
                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                for (DataSnapshot element : task.getResult().getChildren()) {
                    HashMap<String, String> result = (HashMap<String, String>) element.getValue();
                    Log.i("result", String.valueOf(result));
                    Log.i("key", element.getKey());

                    series_moisture.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("moisture"))),false, 60, false);
                    series_humidity.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("humidity"))),false, 60, false);
                    series_temperature.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("temp"))),false, 60, false);
                    series_light.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("light"))),false, 60, false);
                    series_pressure.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("pressure"))),false, 60, false);
                    graph.invalidate();
                    graph.getViewport().scrollToEnd();
                    vp.setMaxX(series_moisture.getHighestValueX());
                    vp.setMinX(series_moisture.getLowestValueX());

                }
                graph.removeAllSeries();
                graph.addSeries(series_moisture);
                graph.getGridLabelRenderer().setVerticalAxisTitle("moisture %");
                graph.getGridLabelRenderer().setHorizontalAxisTitle("time");
                graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
                graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
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
        graph.getGridLabelRenderer().setVerticalAxisTitle("moisture %");
        graph.addSeries(series_moisture);

    }

    private void humidity_chart() {
        graph.removeAllSeries();
        graph.getGridLabelRenderer().setVerticalAxisTitle("humidity %");
        graph.addSeries(series_humidity);

    }

    private void temperature_chart() {
        graph.removeAllSeries();
        graph.getGridLabelRenderer().setVerticalAxisTitle("temperature in °C");
        graph.addSeries(series_temperature);

    }

    private void light_chart() {
        graph.removeAllSeries();
        graph.getGridLabelRenderer().setVerticalAxisTitle("lihgt in Lux");
        graph.addSeries(series_light);

    }

    private void pressure_chart() {
        graph.removeAllSeries();
        graph.getGridLabelRenderer().setVerticalAxisTitle("pressure in kPa");
        graph.addSeries(series_pressure);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Viewport vp = graph.getViewport();
        series_moisture=null;
        series_humidity=null;
        series_light=null;
        series_pressure=null;
        series_temperature=null;
        series_moisture = new LineGraphSeries<>();
        series_humidity = new LineGraphSeries<>();
        series_temperature = new LineGraphSeries<>();
        series_light = new LineGraphSeries<>();
        series_pressure = new LineGraphSeries<>();
        DatabaseReference databaseReference = mPostReference.child("SensorsData");
        Object selectedItem = adapterView.getSelectedItem();
        switch (selectedItem.toString()){
            case "last hour":
                databaseReference.limitToLast(60).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        Log.d("firebase", String.valueOf(task.getResult().getValue()));
                        for (DataSnapshot element : task.getResult().getChildren()) {
                            HashMap<String, String> result = (HashMap<String, String>) element.getValue();
                            Log.i("result", String.valueOf(result));
                            Log.i("key", element.getKey());

                            series_moisture.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("moisture"))),false, 60, false);
                            series_humidity.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("humidity"))),false, 60, false);
                            series_temperature.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("temp"))),false, 60, false);
                            series_light.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("light"))),false, 60, false);
                            series_pressure.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("pressure"))),false, 60, false);
                            graph.invalidate();
                            graph.getViewport().scrollToEnd();
                            vp.setMaxX(series_moisture.getHighestValueX());
                            vp.setMinX(series_moisture.getLowestValueX());

                        }
                        graph.removeAllSeries();
                        addchart();
                        //graph.getGridLabelRenderer().setHumanRounding(false);
                        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
                        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
                    }
                });
                break;
            case "last six hours":
                databaseReference.limitToLast(360).orderByKey().get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        Log.d("firebase", String.valueOf(task.getResult().getValue()));
                        for (DataSnapshot element : task.getResult().getChildren()) {
                            HashMap<String, String> result = (HashMap<String, String>) element.getValue();
                            Log.i("result", String.valueOf(result));
                            Log.i("key", element.getKey());

                            series_moisture.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("moisture"))),false, 360, false);
                            series_humidity.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("humidity"))),false, 360, false);
                            series_temperature.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("temp"))),false, 360, false);
                            series_light.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("light"))),false, 360, false);
                            series_pressure.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("pressure"))),false, 360, false);
                            graph.invalidate();
                            graph.getViewport().scrollToEnd();
                            vp.setMaxX(series_moisture.getHighestValueX());
                            vp.setMinX(series_moisture.getLowestValueX());

                        }
                        graph.removeAllSeries();
                        addchart();
                        //graph.getGridLabelRenderer().setHumanRounding(false);
                        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
                        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
                    }
                });
                break;
            case "last day":
                databaseReference.limitToLast(1440).orderByKey().get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        Log.d("firebase", String.valueOf(task.getResult().getValue()));
                        for (DataSnapshot element : task.getResult().getChildren()) {
                            HashMap<String, String> result = (HashMap<String, String>) element.getValue();
                            Log.i("result", String.valueOf(result));
                            Log.i("key", element.getKey());

                            series_moisture.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("moisture"))),false, 1440, false);
                            series_humidity.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("humidity"))),false, 1440, false);
                            series_temperature.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("temp"))),false, 1440, false);
                            series_light.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("light"))),false, 1440, false);
                            series_pressure.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("pressure"))),false, 1440, false);
                            graph.invalidate();
                            graph.getViewport().scrollToEnd();
                            vp.setMaxX(series_moisture.getHighestValueX());
                            vp.setMinX(series_moisture.getLowestValueX());

                        }
                        graph.removeAllSeries();
                        addchart();
                        //graph.getGridLabelRenderer().setHumanRounding(false);
                        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
                        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
                    }
                });
                break;
            case "last week":
                databaseReference.limitToLast(10080).orderByKey().get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        Log.d("firebase", String.valueOf(task.getResult().getValue()));
                        for (DataSnapshot element : task.getResult().getChildren()) {
                            HashMap<String, String> result = (HashMap<String, String>) element.getValue();
                            Log.i("result", String.valueOf(result));
                            Log.i("key", element.getKey());

                            series_moisture.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("moisture"))),false, 10080, false);
                            series_humidity.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("humidity"))),false, 10080, false);
                            series_temperature.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("temp"))),false, 10080, false);
                            series_light.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("light"))),false, 10080, false);
                            series_pressure.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("pressure"))),false, 10080, false);
                            graph.invalidate();
                            graph.getViewport().scrollToEnd();
                            vp.setMaxX(series_moisture.getHighestValueX());
                            vp.setMinX(series_moisture.getLowestValueX());

                        }
                        graph.removeAllSeries();
                        addchart();
                        //graph.getGridLabelRenderer().setHumanRounding(false);
                        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
                        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
                    }
                });
                break;
            case "last month":
                databaseReference.limitToLast(43800).orderByKey().get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        Log.d("firebase", String.valueOf(task.getResult().getValue()));
                        for (DataSnapshot element : task.getResult().getChildren()) {
                            HashMap<String, String> result = (HashMap<String, String>) element.getValue();
                            Log.i("result", String.valueOf(result));
                            Log.i("key", element.getKey());

                            series_moisture.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("moisture"))),false, 43800, false);
                            series_humidity.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("humidity"))),false, 43800, false);
                            series_temperature.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("temp"))),false, 43800, false);
                            series_light.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("light"))),false, 43800, false);
                            series_pressure.appendData(new DataPoint(new Date((long) Double.parseDouble(element.getKey())), Double.parseDouble(result.get("pressure"))),false, 43800, false);
                            graph.invalidate();
                            graph.getViewport().scrollToEnd();
                            vp.setMaxX(series_moisture.getHighestValueX());
                            vp.setMinX(series_moisture.getLowestValueX());

                        }
                        graph.removeAllSeries();
                        addchart();
                        //graph.getGridLabelRenderer().setHumanRounding(false);
                        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
                        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
                    }
                });
                break;
        }

    }

    private void addchart() {

            elementSelected=radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
            if(elementSelected.getText().toString().equals("Moisture"))
                moisture_chart();
            else if (elementSelected.getText().toString().equals("Humidity"))
                humidity_chart();
            else if (elementSelected.getText().toString().equals("Temperature"))
                temperature_chart();
            else if (elementSelected.getText().toString().equals("Light"))
                light_chart();
            else
                pressure_chart();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
