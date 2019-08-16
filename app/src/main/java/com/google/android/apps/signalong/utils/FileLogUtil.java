package com.google.android.apps.signalong.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.android.apps.signalong.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日志信息保存至本地文件
 */
public class FileLogUtil {
    private static File file;
    private static boolean isDebug = BuildConfig.DEBUG;

    /**
     * 工具类初始化，在Application中调用
     */
    public static void init(Context context){
        /**
         * 路径：/storage/emulated/0/Android/data/package<包名>/files/Log
         */
        if (isDebug){
            file = new File(context.getExternalFilesDir("Log") + "/log.txt");
        }

    }

    /**
     * @param tag 标签
     * @param msg 日志信息
     */
    public static void e(Context context,String tag, String msg){
        Log.e(tag,msg);
        saveLogToSDCard(context.getClass().getName()+"  " + tag + ":  "+msg);
    }


    /**
     * 将异常信息写入SD卡
     *
     * @param msg
     */
    private static void saveLogToSDCard(String msg) {
        if (!isDebug) return;
        //得到当前年月日时分秒
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        msg = time+"/   "+msg+"\n";
        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            fos = new FileOutputStream(file,true);//第二个参数：true、追加    false、覆盖
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw != null){
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
