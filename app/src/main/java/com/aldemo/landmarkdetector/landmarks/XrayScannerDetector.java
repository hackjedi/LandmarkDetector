package com.aldemo.landmarkdetector.landmarks;


import android.hardware.SensorEvent;
import android.util.Log;

import com.aldemo.landmarkdetector.listeners.LandmarkListener;


/**
 * Created by Alexander on 13/06/2016.
 */
public class XrayScannerDetector {

    public static final int ALGORITHM_SIMPLEST = 0;
    private static final int ALGORITHM_COMPLEX = 1;
    public static final int ALGORITHM_XRAY_MINMAX = 2;
    public static final int BUFFER_SIZE = 400;
    public static final int TIME_SPAN_PEEK = 500; //required time in milliseconds
    public static final int BIG_PEEK_SIZE = 400;
    public static final int BIG_PEEK_SIZE_MINMAX = 120;
    public static final int BIG_PEEK_MAX = 250;
    public static final int BIG_PEEK_NEGATIVE_EDGE = -60;
    public static final int LANDMARK_ID = 0;
    private boolean initialized = false;
    private boolean firstSample = false;
    private long firstTimeStamp = 0L;
    private final String LOG_TAG = "XrayScannerDetector";

    private int sampleTime = 20;
    private float[] arrayOld;
    private long[] timeArrayOld;
    private long[] timeArraynew;
    private float[] arrayNew;
    private float[] mergedArray = new float[2*BUFFER_SIZE];
    private long[] mergedTimeArray = new long[2*BUFFER_SIZE];
    private LandmarkListener landmarkListener;
    private int memoryPointer = 0;

    private int algorithm;
    private final float SMOOTHING_CONSTANT = 0.8f;
    private final float BIGPEEK_MIN_MINMAX = 160;

    /**
     *
     * @param algorithm
     * @param landmarkListener
     * @info constructor without a given sample time
     */
    public XrayScannerDetector(int algorithm, LandmarkListener landmarkListener){
        this.algorithm = algorithm;
        this.landmarkListener = landmarkListener;
        arrayOld = new float[BUFFER_SIZE];
        arrayNew = new float[BUFFER_SIZE];
        timeArrayOld = new long[BUFFER_SIZE];
        timeArraynew = new long[BUFFER_SIZE];
    }

    /**
     * @param algorithm
     * @param landmarkListener
     * @param sampleTime
     * @info constructor with a given sample time
     */
    public XrayScannerDetector(int algorithm, LandmarkListener landmarkListener, int sampleTime){
        this.algorithm = algorithm;
        this.landmarkListener = landmarkListener;
        arrayOld = new float[BUFFER_SIZE];
        arrayNew = new float[BUFFER_SIZE];
        this.sampleTime = sampleTime;
        initialized = true;
    }

    public void addBuffer(float[] arrayNew){

        this.arrayNew = arrayNew;
        switch (algorithm){
            case ALGORITHM_SIMPLEST:
            //    doAlgorithmSimplest();
                doAlgorithmMinMax();
                break;
            case ALGORITHM_COMPLEX:
                //doAlgorithmComplex();
                break;
            default:
                break;
        }
    }

    private void doAlgorithmMinMax(){
        mergeArray();
        float maxValue = mergedArray[0];
        float minValue = mergedArray[0];
        int maxPointer = 2*BUFFER_SIZE;
        for (int i=0; i<maxPointer; i++){
            if (mergedArray[i]> maxValue){
                maxValue = mergedArray[i];
            }
            if (mergedArray[i]<minValue){
                minValue=mergedArray[i];
            }
        }
        float difference = maxValue-minValue;
        if ((difference>= BIG_PEEK_SIZE_MINMAX)&&(difference<=BIGPEEK_MIN_MINMAX)){
            Log.d(LOG_TAG, "big peek detected: "  + difference + "  min value: " + minValue+ " max value :" + maxValue);
            landmarkListener.onLandmarkDetected(ALGORITHM_XRAY_MINMAX); // tell the activity a landmark is detected
            doAlgorithmSimplest();

        }

    }
    private void doAlgorithmSimplest() {

        int peekCount = 0;
        //mergeArray();
        float biggestDifference = 0.0f;
        float negativeEdge = 0.0f;
        // calculate the pointers and the time interval
        int intervalTimeSamples =  TIME_SPAN_PEEK/ sampleTime;
        int maxPointer = 2*BUFFER_SIZE-(2*intervalTimeSamples);
        int pulseWidth = 100/sampleTime;
        //calculate the magnetic difference
        for (int i =0; i<maxPointer; i++){
            for (int j=0; j<intervalTimeSamples;j++){
                float difference = mergedArray[i+j]-mergedArray[i];
                if ((difference>=BIG_PEEK_SIZE)&&(difference<BIG_PEEK_MAX)){
                    Log.d(LOG_TAG, "big peek detected: "  + difference);
                    //checking pulse width
                    boolean isConstant = true;
                    for (int p=0; p<=pulseWidth; p++){
                        if ((mergedArray[i+j]- mergedTimeArray[i+j+p])> 5){
                            isConstant = false;
                        }
                    }
                    Log.d(LOG_TAG, "pulse width : " + isConstant);
                    if (difference>biggestDifference){
                        biggestDifference=difference;
                    }
                    for (int k=1; k<intervalTimeSamples; k++) {
                        negativeEdge =  mergedArray[i+j+k]-mergedArray[i+j];
                        if ((negativeEdge<=BIG_PEEK_NEGATIVE_EDGE)&&(Math.abs(negativeEdge)<=difference)) {
                            peekCount++;
                            break;
                        }
                    }
                    Log.d(LOG_TAG,"data processed. Biggest difference: " + biggestDifference + "biggest negative peak: " + negativeEdge);
                }
            }

        }
        if (peekCount>=4){
            landmarkListener.onLandmarkDetected(ALGORITHM_SIMPLEST); // tell the activity a landmark is detected
            Log.d(LOG_TAG, "counter:  " + peekCount);
            setOldArray();
            // return;
        }

        Log.d(LOG_TAG, "algorithm simplest done " );;
    }

    /**
     *   merge array old and new
     */
    private void mergeArray(){

        for (int i=0; i<BUFFER_SIZE;i++){
            mergedArray[i] = arrayOld[i];
            mergedArray[BUFFER_SIZE+i] = arrayNew[i];
            mergedTimeArray[i] = timeArrayOld[i];
            mergedTimeArray[BUFFER_SIZE+i] = timeArraynew[i];
        }
        smoothArray();
    }
    /*
     * create a true copy instead of replacing the pointer
     */
    private void setOldArray(){
        for (int i=0; i<BUFFER_SIZE; i++){
            arrayOld[i] = arrayNew[i];
            timeArrayOld[i]=timeArraynew[i];
        }

    }
    private void doAlgorithmComplex(){

        mergeArray();


        // calculate the pointers and the time interval
        int intervalTimeSamples =  TIME_SPAN_PEEK/ sampleTime;
        int maxPointer = 2*BUFFER_SIZE-(2*intervalTimeSamples);


        //calculate the magnetic difference
        for (int i =0; i<maxPointer; i++){
            for (int j=0; j<intervalTimeSamples;j++){
                float difference = mergedArray[i+j]-mergedArray[i];
                if ((difference>=BIG_PEEK_SIZE)&&(difference<BIG_PEEK_MAX)){
                    for (int k=0; k<intervalTimeSamples; k++) {
                        float negativeEdge =  mergedArray[i+j+k]-mergedArray[i+j];
                        if ((negativeEdge<=BIG_PEEK_NEGATIVE_EDGE)&&(Math.abs(negativeEdge)<=difference)) {
                            landmarkListener.onLandmarkDetected(LANDMARK_ID); // tell the activity a landmark is detected
                            Log.d(LOG_TAG, "difference positive edge: " + difference + "  negative edge: " + negativeEdge + " timedifference: "
                                    +((mergedTimeArray[i+j+k]-mergedTimeArray[i+j])/1000000L));
                            setOldArray();
                            return;
                        }
                    }
                }
            }
        }
        Log.d(LOG_TAG,"data processed");
    }
    public void addRawData(SensorEvent sensorEvent){

        //get the sample time
        if (!initialized){
            if (!firstSample){
                firstTimeStamp = sensorEvent.timestamp;
                firstSample=true;
            }
            else{
                sampleTime = (int)((sensorEvent.timestamp-firstTimeStamp)/1000000L);
                initialized=true;
            }
        }
        if (memoryPointer<BUFFER_SIZE){

            float magnitudeMagnetic = (float)Math.sqrt(sensorEvent.values[0]*sensorEvent.values[0]+sensorEvent.values[1]*sensorEvent.values[1]+sensorEvent.values[2]*sensorEvent.values[2]);
            arrayNew[memoryPointer] = magnitudeMagnetic;
            timeArraynew[memoryPointer] = sensorEvent.timestamp;
            memoryPointer++;
            if (memoryPointer==BUFFER_SIZE){
                addBuffer(arrayNew);
                memoryPointer=0;
            }
        }
        else{
            Log.d(LOG_TAG, "buffer overload error");
            memoryPointer=0;
        }
    }

    public void smoothArray(){
        float value = mergedArray[0]; // start with the first input
        for (int i=0; i<(2*BUFFER_SIZE); ++i){
            float currentValue = mergedArray[i];
            value += (currentValue - value) / SMOOTHING_CONSTANT;
            mergedArray[i] = value;
        }
    }

}
