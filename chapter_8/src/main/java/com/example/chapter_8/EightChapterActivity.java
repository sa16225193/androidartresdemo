package com.example.chapter_8;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class EightChapterActivity extends Activity {
    private static final String TAG = "EightChapterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_eight);
    }

    public void onButtonClick(View v) {
        if (v.getId() == R.id.button1) {
            Intent intent = new Intent(this, WindowActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.button2) {
            Intent intent = new Intent(this, SystemWindowActivity.class);
            startActivity(intent);
        }
    }

}
