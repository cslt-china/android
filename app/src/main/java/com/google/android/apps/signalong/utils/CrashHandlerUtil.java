package com.google.android.apps.signalong.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;


import com.google.android.apps.signalong.MyApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * bug收集工具类
 * Created by jk on 2019-04-01
 */
public class CrashHandlerUtil implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    /**
     * 异常日志 存储位置为根目录下的 Crash文件夹
     */
    private static final String PATH = Environment.getExternalStorageDirectory() + File.separator + "Android" +
            File.separator + "data" + File.separator + MyApplication.getAppContext().getPackageName() + File.separator + "log"+File.separator;

    /**
     * 文件名
     */
    public static final String FILE_NAME = "crash";

    /**
     * 文件名后缀
     */
    private static final String FILE_NAME_SUFFIX = ".txt";

    private static CrashHandlerUtil sInstance = new CrashHandlerUtil();
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;
    private Context mContext;


    private CrashHandlerUtil() {

    }

    public static CrashHandlerUtil getInstance() {
        return sInstance;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        //得到系统的应用异常处理器
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        //将当前应用异常处理器改为默认的
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context.getApplicationContext();

    }


    /**
     * 这个是最关键的函数，当系统中有未被捕获的异常，系统将会自动调用 uncaughtException 方法
     *
     * @param thread 为出现未捕获异常的线程
     * @param ex     为未捕获的异常 ，可以通过e 拿到异常信息
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        File file;
        //导入异常信息到SD卡中
        try {
            file = dumpExceptionToSDCard(ex);
            uploadExceptionToServer(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //这里可以上传异常信息到服务器，便于开发人员分析日志从而解决Bug
        ex.printStackTrace();
        //如果系统提供了默认的异常处理器，则交给系统去结束程序，否则就由自己结束自己
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
        }

    }

    /**
     * 将异常信息写入SD卡
     *
     * @param e
     */
    private File dumpExceptionToSDCard(Throwable e) throws IOException {
        //如果SD卡不存在或无法使用，则无法将异常信息写入SD卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        File dir = new File(PATH);
        //如果目录下没有文件夹，就创建文件夹
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //得到当前年月日时分秒
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        //在定义的Crash文件夹下创建文件
        File file = new File(PATH + FILE_NAME + time + FILE_NAME_SUFFIX);

        try{
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            //写入时间
            pw.print("crash time: ");
            pw.println(time);
            //写入手机信息
            dumpPhoneInfo(pw);
            pw.println();//换行
            e.printStackTrace(pw);
            pw.close();//关闭输入流
        } catch (Exception e1) {
            Log.e(TAG,"dump crash info failed");
        }

        return file;
    }

    /**
     * 获取手机各项信息
     * @param pw
     */
    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        //得到包管理器
        PackageManager pm = mContext.getPackageManager();
        //得到包对象
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),PackageManager.GET_ACTIVITIES);
        //写入APP版本号
        pw.print("App VersionName: ");
        pw.println(pi.versionName);
        pw.print("App VersionCode: ");
        pw.println(pi.versionCode);
        //写入 Android 版本号
        pw.print("OS Version（Android系统版本）: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);
        //手机制造商
        pw.print("Vendor（手机制造商）: ");
        pw.println(Build.MANUFACTURER);
        //手机型号
        pw.print("Model（手机型号）: ");
        pw.println(Build.MODEL);
        //CPU架构
        pw.print("CPU ABI: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pw.println(Build.SUPPORTED_ABIS);
        }else {
            pw.println(Build.CPU_ABI);
        }
    }

    /**
     * 将错误信息上传至服务器
     * @param file
     */
    private void uploadExceptionToServer(File file) {
        if (file.exists()){
            Log.i(TAG, "uploadExceptionToServer: 文件存在，文件名:"+file.getName()+"\n 文件路径："+file.getAbsolutePath());
        }else {
            Log.i(TAG, "uploadExceptionToServer: 日志文件不存在");
        }
    }


}
