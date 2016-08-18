package com.aldemo.landmarkdetector.landmarks;

import android.hardware.SensorEvent;
import android.util.Log;

import com.aldemo.landmarkdetector.listeners.LandmarkListener;

/**
 * Created by Alexander on 16/06/2016.
 */

public class TrainDetector {

    public static final int LANDMARK_ID = 1;
    public static final int ENTER_TRAIN = 99;
    public static final int EXIT_TRAIN = 98;
    private final String LOG_TAG = "TrainDetector";
    public static final int DELAY = 100;
    LandmarkListener landmarkListener;
    float currentMagnitudeMagnetic;
    boolean inTrain = false;

    int pointer = 0;
    int inTrainCounter=0;
    int offTrainCounter=0;

    public TrainDetector(LandmarkListener landmarkListener){

        this.landmarkListener = landmarkListener;

    }

    public void addRawData(SensorEvent sensorEvent){

        if (pointer<=DELAY){

            if (pointer==DELAY){
                 currentMagnitudeMagnetic = (float)Math.sqrt(sensorEvent.values[0]*sensorEvent.values[0]+sensorEvent.values[1]*sensorEvent.values[1]+sensorEvent.values[2]*sensorEvent.values[2]);
                doAlgorithm();
                pointer =0;
            }
            pointer++;
        }
        else{
            //ERROR
            pointer=0;
        }


    }

    private void doAlgorithm() {
       if(!inTrain) {
           if (currentMagnitudeMagnetic >= 190) {
               inTrainCounter++;
           }
           if (inTrainCounter==5){
               inTrain=true;
               inTrainCounter=0;
               landmarkListener.onLandmarkDetected(TrainDetector.ENTER_TRAIN);
           }
       }
        else{
           if (currentMagnitudeMagnetic<=40){
               offTrainCounter++;
           }
           if (offTrainCounter==5){
               inTrain=false;
               offTrainCounter=0;
               landmarkListener.onLandmarkDetected(TrainDetector.EXIT_TRAIN);
           }
       }
        Log.d(LOG_TAG, "algorithm running");
    }

}
