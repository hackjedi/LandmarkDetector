package com.aldemo.landmarkdetector;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aldemo.landmarkdetector.landmarks.TrainDetector;
import com.aldemo.landmarkdetector.landmarks.XrayScannerDetector;
import com.aldemo.landmarkdetector.listeners.LandmarkListener;

/**
 * Created by Alexander on 14/06/2016.
 */
public class SensorService extends Service implements SensorEventListener, LandmarkListener {


    private SensorManager sensorManager = null;
    private Sensor sensor = null;
    private XrayScannerDetector xrayScannerDetector;
    private TrainDetector trainDetector;
    private static final String LOG_TAG = "SensorService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        xrayScannerDetector = new XrayScannerDetector(XrayScannerDetector.ALGORITHM_SIMPLEST, this);
        trainDetector  = new TrainDetector(this);
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        switch (sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                xrayScannerDetector.addRawData(event);
                Log.d(LOG_TAG,"sensor event triggered");
                break;
            default:
                break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLandmarkDetected(int landmark) {

    }
}
