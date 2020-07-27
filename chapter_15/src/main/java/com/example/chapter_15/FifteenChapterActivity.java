package com.example.chapter_15;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class FifteenChapterActivity extends Activity implements TestManager.OnDataArrivedListener {
    private static final String TAG = "MainActivity";

    private static Context sContext;

    private Button mButton;
    private ObjectAnimator mAnimator;

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_fifteen);
        /*
         * 制造内存泄漏
         */
        sContext = this;//1. 静态成员持有Activity引用导致的泄漏

        mButton = findViewById(R.id.button1);
        TestManager.getInstance().registerListener(this);//2. 单例持有Activity引用导致的泄漏

        mAnimator = ObjectAnimator.ofFloat(mButton, "rotation",
                0, 360).setDuration(2000);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.start();//3. 属性动画未及时停止导致的泄漏

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        /*
         * 制造ANR
         */
//        SystemClock.sleep(30 * 1000);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                testANR();
//            }
//        }).start();
//        SystemClock.sleep(10);
//        initView();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private synchronized void testANR() {
        SystemClock.sleep(30 * 1000);
    }

    private synchronized void initView() {

    }

    @Override
    public void onDataArrived(Object data) {
        Log.i(TAG, data.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mAnimator.isRunning()) {
//            mAnimator.cancel();
//        }

//        sContext = null;

//        TestManager.getInstance().unregisterListener(this);
    }
}
