package com.example.chapter_2;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Process;
import android.util.Log;

import com.example.chapter_2.util.MyUtils;

public class MyApplication extends Application {

    private static final String TAG = "com.example.chapter_2.MyApplication";

    @SuppressLint("LongLogTag")
    @Override
    public void onCreate() {
        super.onCreate();
        String processName = MyUtils.getProcessName(getApplicationContext(),
                Process.myPid());
        Log.d(TAG, "application start, process name:" + processName);
    }

}
