package com.example.chapter_5;

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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class FifthChapterActivity extends Activity {

    private static int sId = 0;
    private static final String sDEFAULT_CHANNEL_ID = "001";
    private static final String sCHANNEL_DEFAULT = "default_channel";
    private static final String sHIGH_CHANNEL_ID = "002";
    private static final String sCHANNEL_HIGH = "high_channel";

    private LinearLayout mRemoteViewsContent;

    private BroadcastReceiver mRemoteViewsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            RemoteViews remoteViews = intent
                    .getParcelableExtra(MyConstants.EXTRA_REMOTE_VIEWS);
            if (remoteViews != null) {
                updateUI(remoteViews);
            }
        }
    };

    private void updateUI(RemoteViews remoteViews) {
//        View view = remoteViews.apply(this, mRemoteViewsContent);

        //通过资源文件名称来加载布局，支持跨应用加载布局文件
        int layoutId = getResources().getIdentifier("layout_simulated_notification", "layout", getPackageName());
        View view = getLayoutInflater().inflate(layoutId, mRemoteViewsContent, false);
        remoteViews.reapply(this, view);
        mRemoteViewsContent.addView(view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_fifth);
        initView();
    }

    private void initView() {
        mRemoteViewsContent = (LinearLayout) findViewById(R.id.remote_views_content);
        IntentFilter filter = new IntentFilter(MyConstants.REMOTE_ACTION);
        registerReceiver(mRemoteViewsReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mRemoteViewsReceiver);
        super.onDestroy();
    }

    public void sendDefNotify(View v) {
        sId++;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, sDEFAULT_CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentText("hello world");
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);
        Intent intent = new Intent(this, DemoActivity_2.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Android 8.0及以上,发送通知必须设置通知的渠道,即NotificationChannel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createNotificationChannel(sDEFAULT_CHANNEL_ID, sCHANNEL_DEFAULT, NotificationManager.IMPORTANCE_DEFAULT);
        }

        Notification notification = builder.build();
        manager.notify(sId, notification);//如果第一个参数的id相同，那么后续通知会替代调之前通知
    }

    public void sendCustomNotify(View v) {
        sId++;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, sHIGH_CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentText("hello ketty");
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);
        Intent intent = new Intent(this, DemoActivity_1.class);
        intent.putExtra("sid", "" + sId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        System.out.println(pendingIntent);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
        remoteViews.setTextViewText(R.id.msg, "chapter_5: " + sId);
        remoteViews.setImageViewResource(R.id.icon, R.drawable.icon1);
        PendingIntent openActivity2PendingIntent = PendingIntent.getActivity(this,
                0, new Intent(this, DemoActivity_2.class), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.open_activity2, openActivity2PendingIntent);
        builder.setContent(remoteViews);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Android 8.0及以上,如果某通知渠道被关闭，可以提示用户打开
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel(sHIGH_CHANNEL_ID);
            if (channel != null) {
                if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                    Intent setIntent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    setIntent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    setIntent.putExtra(Settings.EXTRA_CHANNEL_ID, channel.getId());
                    startActivity(setIntent);
                    Toast.makeText(this, "请手动将通知打开", Toast.LENGTH_SHORT).show();
                }
            } else {
                createNotificationChannel(sHIGH_CHANNEL_ID, sCHANNEL_HIGH, NotificationManager.IMPORTANCE_HIGH);
            }
            builder.setNumber(2);//角标支持显示数量
        }

        Notification notification = builder.build();
        manager.notify(sId, notification);
    }

    /**
     * 通知支持角标功能
     * Android 8.0及以上,Google制定了角标规范,也提供了标准的API
     * @param channelId
     * @param channelName
     * @param importance
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setShowBadge(true);//默认支持角标,关闭需要设置为false
        channel.enableLights(true);//是否在桌面icon右上角展示小红点
        channel.setLightColor(Color.YELLOW);//小红点颜色
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }

    /**
     * 删除通知渠道
     * 但是这个功能非常不建议大家使用。
     * 因为Google为了防止应用程序随意地创建垃圾通知渠道，会在通知设置界面显示所有被删除的通知渠道数量
     */
    public void deleteChannel(View v) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(sDEFAULT_CHANNEL_ID);
        }
    }

    public void appWidgetProvider(View v) {
        Intent intent = new Intent(this, DemoActivity_2.class);
        startActivity(intent);
    }
}
