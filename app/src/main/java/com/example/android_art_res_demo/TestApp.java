package com.example.android_art_res_demo;

import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.example.chapter_13.CrashHandler;
import com.example.chapter_2.util.MyUtils;

public class TestApp extends Application {

    private static final String TAG = "TestApp";
    private static TestApp sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        //在这里为应用设置异常处理程序，然后我们的程序才能捕获未处理的异常
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);

        String processName = MyUtils.getProcessName(getApplicationContext(),
                Process.myPid());
        Log.d(TAG, "application start, process name:" + processName);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static TestApp getInstance() {
        return sInstance;
    }

}
