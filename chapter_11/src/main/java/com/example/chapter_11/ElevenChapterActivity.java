package com.example.chapter_11;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ElevenChapterActivity extends Activity {
    private static final String TAG = "ElevenChapterActivity";
    private TextView mTextView;
    private PausableThreadPoolExecutor mPausableThreadPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_eleven);
        mTextView = findViewById(R.id.text);
        scheduleThreads();
    }

    public void command(View v) {
        if (v instanceof Button) {
            if (TextUtils.equals(((Button) v).getText(), "开始")) {
                ((Button) v).setText("暂停");
                mPausableThreadPool.resume();
            } else {
                ((Button) v).setText("开始");
                mPausableThreadPool.pause();
            }
        }
    }

    private void scheduleThreads() {
        runAsyncTask();
        runIntentService();
        runThreadPool();
        runPriorityThreadPool();
        runPausableThreadPool();
    }

    private void runPausableThreadPool() {
        mPausableThreadPool = new PausableThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
        for (int i = 1; i <= 100; i++) {
            final int priority = i;
            mPausableThreadPool.execute(new PriorityRunnable(priority) {
                @Override
                public void doSth() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextView.setText(priority + "");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 使用优先级队列的线程池
     */
    private void runPriorityThreadPool() {
        ExecutorService priorityThreadPool = new ThreadPoolExecutor(3, 3, 0L, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
        for (int i = 1; i <= 10; i++) {
            final int priority = i;
            priorityThreadPool.execute(new PriorityRunnable(priority) {
                @Override
                public void doSth() {
                    String threadName = Thread.currentThread().getName();
                    Log.v(TAG, "线程：" + threadName + ",正在执行优先级为：" + priority + "的任务");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void runThreadPool() {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
            }
        };

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4);
        fixedThreadPool.execute(command);
        
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        cachedThreadPool.execute(command);
        
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(4);
        // 2000ms后执行command
        scheduledThreadPool.schedule(command, 2000, TimeUnit.MILLISECONDS);
        // 延迟10ms后，每隔1000ms执行一次command
        scheduledThreadPool.scheduleAtFixedRate(command, 10, 1000, TimeUnit.MILLISECONDS);

        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(command);
    }

    private void runIntentService() {
        Intent service = new Intent(this, LocalIntentService.class);
        service.putExtra("task_action", "com.ryg.action.TASK1");
        startService(service);
        service.putExtra("task_action", "com.ryg.action.TASK2");
        startService(service);
        service.putExtra("task_action", "com.ryg.action.TASK3");
        startService(service);
    }

    private void runAsyncTask() {
        try {
            new DownloadFilesTask().execute(new URL("http://www.baidu.com"),
                    new URL("http://www.renyugang.cn"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private class DownloadFilesTask extends AsyncTask<URL, Integer, Long> {
        protected Long doInBackground(URL... urls) {
            int count = urls.length;
            long totalSize = 0;
            for (int i = 0; i < count; i++) {
                // totalSize += Downloader.downloadFile(urls[i]);
                publishProgress((int) ((i / (float) count) * 100));
                // Escape early if cancel() is called
                if (isCancelled())
                    break;
            }
            return totalSize;
        }

        protected void onProgressUpdate(Integer... progress) {
//             setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            // showDialog("Downloaded " + result + " bytes");
        }
    }

}
