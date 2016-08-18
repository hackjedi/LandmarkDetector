package com.aldemo.landmarkdetector;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.aldemo.landmarkdetector.landmarks.TrainDetector;
import com.aldemo.landmarkdetector.landmarks.XrayScannerDetector;
import com.aldemo.landmarkdetector.listeners.LandmarkListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener,LandmarkListener {


    private static final String LOG_TAG = "LandmarkDetector";
    SensorManager sensorManager;
    XrayScannerDetector xrayScannerDetector;
    private Sensor magnetometer;
    NotificationCompat.Builder mBuilder;
    int notificationID = 1;
   TrainDetector trainDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xrayScannerDetector = new XrayScannerDetector(XrayScannerDetector.ALGORITHM_SIMPLEST,this);
        trainDetector  = new TrainDetector(this);
        registerSensorListener();
        Intent intent = new Intent(getApplicationContext(), SensorService.class );
        startService(intent);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.klm_wing_tip);
        mBuilder.setContentTitle("Landmark detection activated!");
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(notificationID, mBuilder.build());

    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;

        switch (sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                xrayScannerDetector.addRawData(event);
              //  trainDetector.addRawData(event);
                //   Log.d(LOG_TAG,"sensor event triggered");
                break;
            default:
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void registerSensorListener() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED).get(0), SensorManager.SENSOR_DELAY_FASTEST);

    }


    private void unregisterSensorListener() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onLandmarkDetected(int landmark) {
        mBuilder.setContentTitle("Landmark detected!");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        //create message!
        switch (landmark){
            case XrayScannerDetector.ALGORITHM_SIMPLEST:
                Log.d(LOG_TAG, "landmark detected: Xray scanner");
                mBuilder.setContentText("ALGORITHM_SIMPLEST at:" + currentDateandTime);
                break;
            case XrayScannerDetector.ALGORITHM_XRAY_MINMAX:
                mBuilder.setContentText("ALGORITHM_XRAY_MINMAX at:" + currentDateandTime);
                break;
            case TrainDetector.ENTER_TRAIN:
                Log.d(LOG_TAG, "landmark detected: train entered");
                mBuilder.setContentTitle("Landmark detected!");
                mBuilder.setContentText("ENTER_TRAIN at:" + currentDateandTime);
                break;
            case TrainDetector.EXIT_TRAIN:

                Log.d(LOG_TAG, "landmark detected: train left");
                mBuilder.setContentTitle("Landmark detected!");
                mBuilder.setContentText("EXIT_TRAIN at:" + currentDateandTime);
                break;
            default:
                break;
        }
        mBuilder.setVibrate(new long[] { 1000, 1000, 1000 });
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(notificationID, mBuilder.build());
    }
}
