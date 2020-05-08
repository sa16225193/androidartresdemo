package com.example.chapter_2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.example.chapter_2.binderpool.BinderPoolActivity;
import com.example.chapter_2.file.SecondActivity;
import com.example.chapter_2.messenger.MessengerActivity;
import com.example.chapter_2.socket.TCPClientActivity;
import com.example.chapter_2.util.MyConstants;
import com.example.chapter_2.util.MyUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SecondChapterActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_chapter);
        UserManager.sUserId = 2;
    }

    public void ipcByFile(View v) {
        Intent intent = new Intent();
        intent.setClass(SecondChapterActivity.this, SecondActivity.class);
        User user = new User(0, "jake", true);
        user.book = new Book();
        intent.putExtra("extra_user", (Serializable) user);
        startActivity(intent);
    }

    public void ipcByMessenger(View v) {
        Intent intent = new Intent();
        intent.setClass(SecondChapterActivity.this, MessengerActivity.class);
        startActivity(intent);
    }

    public void ipcByAIDL(View v) {
        Intent intent = new Intent();
        intent.setClass(SecondChapterActivity.this, BookManagerActivity.class);
        startActivity(intent);
    }

    public void ipcBySocket(View v) {
        Intent intent = new Intent();
        intent.setClass(SecondChapterActivity.this, TCPClientActivity.class);
        startActivity(intent);
    }

    public void binderPool(View v) {
        Intent intent = new Intent();
        intent.setClass(SecondChapterActivity.this, BinderPoolActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "UserManage.sUserId=" + UserManager.sUserId);
        persistToFile();
    }

    private void persistToFile() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                User user = new User(1, "hello world", false);
                File dir = new File(MyConstants.CHAPTER_2_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File cachedFile = new File(MyConstants.CACHE_FILE_PATH);
                ObjectOutputStream objectOutputStream = null;
                try {
                    objectOutputStream = new ObjectOutputStream(
                            new FileOutputStream(cachedFile));
                    objectOutputStream.writeObject(user);
                    Log.d(TAG, "persist user:" + user);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    MyUtils.close(objectOutputStream);
                }
            }
        }).start();
    }
}
