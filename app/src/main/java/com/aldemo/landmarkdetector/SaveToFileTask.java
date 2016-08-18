package com.aldemo.landmarkdetector;

/**
 * Created by Alexander on 26/05/2016.
 */
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveToFileTask extends AsyncTask <String, Integer, Long> {
    private String folderName = "defaultfolder";
    Context context;
    String fileName = "Data.txt";

    public SaveToFileTask(Context context) {
        this.context = context;
        Log.d("fileLocation", context.getExternalFilesDir(null).getAbsolutePath() );
    }

    public SaveToFileTask(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
        Log.d("fileLocation", context.getExternalFilesDir(null).getAbsolutePath() );
    }

    public SaveToFileTask(Context context, String fileName, String folderName) {
        this.context = context;
        this.fileName = fileName;
        this.folderName = folderName;
        Log.d("fileLocation", context.getExternalFilesDir(null).getAbsolutePath() );
    }
    @Override
    protected Long doInBackground(String... params) {
        long a = 0;
        String fcontent = params[0];
        File folder = new File(context.getExternalFilesDir(null).toString() +"/"+folderName);
        if (!folder.isDirectory()){
              folder.mkdirs();
            Log.d("SaveFileTask","create directory");
        }
        File traceFile = new File(context.getExternalFilesDir(null).toString() +"/"+folderName+"/"+fileName);


     //   if (!traceFile.exists()) {

            try {
                traceFile.createNewFile();
                Log.d("SaveFileTask","file created "+ fileName);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("saveFile","file cannot be created");
            }
       // }

        try{
            // If file does not exists, then create it

            BufferedWriter writer = new BufferedWriter(new FileWriter(traceFile, true /*true for append*/));
            writer.write(fcontent);
            writer.close();

            //refresh data
           // Log.d("SaveToFileTask", ""+fcontent);
            MediaScannerConnection.scanFile(context, new String[] { traceFile.toString()}, null, null);

        } catch (IOException e) {
            e.printStackTrace();
        }


        return a;
    }


    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);
    }
}
