package com.example.chapter_13;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ThirteenChapterActivity extends Activity implements View.OnClickListener {

    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_thirteen);
        initView();
    }

    private void initView() {
        mButton = (Button) findViewById(R.id.button1);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mButton) {
            // 在这里模拟异常抛出情况，人为抛出一个运行时异常
            throw new RuntimeException("自定义异常：这是自己抛出的异常");
        }
    }

}
