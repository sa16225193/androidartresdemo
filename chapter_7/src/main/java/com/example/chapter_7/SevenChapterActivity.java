package com.example.chapter_7;

import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.lang.annotation.Target;
import java.util.ArrayList;

public class SevenChapterActivity extends Activity {

    private static final String TAG = "SevenChapterActivity";
    private Button button;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seven_chapter);
        button = findViewById(R.id.button2);
        initView();
    }

    public void onButtonClick(View v) {
        if (v.getId() == R.id.button1) {
            Intent intent = new Intent(this, TestActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
        } else if (v.getId() == R.id.button2) {
            button.post(new Runnable() {
                @Override
                public void run() {
                    performAnimate(button, button.getWidth(), 500);
                }
            });
        } else if (v.getId() == R.id.button3) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_item);
            LayoutAnimationController controller = new LayoutAnimationController(animation);
            controller.setDelay(0.5f);
            controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
            listView.setLayoutAnimation(controller);
        } else if (v.getId() == R.id.button4) {
            final Button button = findViewById(R.id.button4);
            button.post(new Runnable() {
                @Override
                public void run() {
                    Rotate3dAnimation rotate3dAnimation = new Rotate3dAnimation(0, 360, button.getX() + button.getWidth() / 2, button.getY() + button.getHeight() / 2,100, false);
                    rotate3dAnimation.setDuration(1000);
                    button.startAnimation(rotate3dAnimation);
                }
            });
        } else if (v.getId() == R.id.button5) {
            final Button button = findViewById(R.id.button5);
            button.post(new Runnable() {
                @Override
                public void run() {
                    ViewWrapper wrapper = new ViewWrapper(button);
                    ObjectAnimator.ofInt(wrapper, "width", button.getWidth() / 2).setDuration(5000).start();
                }
            });
        } else if (v.getId() == R.id.button6) {
            final Button button = findViewById(R.id.button6);
            button.post(new Runnable() {
                @Override
                public void run() {
                    ValueAnimator valueAnimator = ValueAnimator.ofInt(100, 1);
                    final int width = button.getWidth();
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                        //持有一个IntEvaluator对象，方便下面估值的时候使用
                        private IntEvaluator intEvaluator = new IntEvaluator();

                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            //获得当前动画的进度值，整型，1~100之间
                            int currentValue = (int) animation.getAnimatedValue();
                            float fraction = animation.getAnimatedFraction();
                            Log.d(TAG, "current value = " + currentValue + ", fraction = " + fraction);

                            //获得当前进度占整个动画过程的比例,浮点型，0~1之间
                            button.getLayoutParams().width = intEvaluator.evaluate(fraction, width, width / 2);
                            button.requestLayout();
                        }
                    });
                    valueAnimator.setDuration(5000).start();
                }
            });
        }
    }

    private void performAnimate(final View target, final int start, final int end) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(1, 100);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            // 持有一个IntEvaluator对象，方便下面估值的时候使用
            private IntEvaluator mEvaluator = new IntEvaluator();

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                // 获得当前动画的进度值，整型，1-100之间
                int currentValue = (Integer) animator.getAnimatedValue();
                Log.d(TAG, "current value: " + currentValue);

                // 获得当前进度占整个动画过程的比例，浮点型，0-1之间
                float fraction = animator.getAnimatedFraction();
                // 直接调用整型估值器通过比例计算出宽度，然后再设给Button
                target.getLayoutParams().width = mEvaluator.evaluate(fraction, start, end);
                target.requestLayout();
            }
        });

        valueAnimator.setDuration(5000).start();
    }

    private void initView() {
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout mListContainer = findViewById(R.id.container);
        final int screenWidth = MyUtils.getScreenMetrics(this).widthPixels;
        final int screenHeight = MyUtils.getScreenMetrics(this).heightPixels;
        for (int i = 0; i < 1; i++) {
            ViewGroup layout = (ViewGroup) inflater.inflate(
                    R.layout.content_layout, mListContainer, false);
            layout.getLayoutParams().width = screenWidth;
            TextView textView = (TextView) layout.findViewById(R.id.title);
            textView.setText("page " + (i + 1));
            layout.setBackgroundColor(Color.rgb(255 / (i + 1), 255 / (i + 1), 0));
            createList(layout);
            mListContainer.addView(layout);
        }
    }

    private void createList(ViewGroup layout) {
        listView = layout.findViewById(R.id.list);

        ArrayList<String> datas = new ArrayList<String>();
        for (int i = 0; i < 50; i++) {
            datas.add("name " + i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.content_list_item, R.id.name, datas);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(SevenChapterActivity.this, "click item",
                        Toast.LENGTH_SHORT).show();

            }
        });
    }
}
