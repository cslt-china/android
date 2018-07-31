package com.google.android.apps.cslt.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class FileUtils {

    public static boolean existFile(String url)
    {
        if(url!=null)
        {
            File file=new File(url);
            return file.exists();
        }
        return false;

    }



    public static void removeFile(String url)
    {
        if(existFile(url))
        {
            File file=new File(url);
            if(file.isFile())
            {
                file.delete();
            }
        }
    }


    public static String getVideoPath()
    {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCamera");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCamera", "failed to create directory");
                return null;
            }
        }
        return  mediaStorageDir.getPath() + File.separator +
                "VID_UPLOAD.mp4";
    }


}
