package com.example.android_art_res_demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.example.chapter_2.SecondChapterActivity;
import com.example.chapter_3.ThirdChapterActivity;
import com.example.chapter_4.ForthChapterActivity;
import com.example.chapter_5.FifthChapterActivity;

public class MainActivity extends AppCompatActivity {

    private String[] permissionList = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            for (String permission : permissionList) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{permission}, 1);
                }
            }
        }
    }

    public void goSecond(View v) {
        Intent intent = new Intent(this, SecondChapterActivity.class);
        startActivity(intent);
    }

    public void goThird(View v) {
        Intent intent = new Intent(this, ThirdChapterActivity.class);
        startActivity(intent);
    }

    public void goForth(View v) {
        Intent intent = new Intent(this, ForthChapterActivity.class);
        startActivity(intent);
    }

    public void goFifth(View v) {
        Intent intent = new Intent(this, FifthChapterActivity.class);
        startActivity(intent);
    }
}
