### 为什么需要binder连接池  

如果项目越来越庞大，每个AIDL都创建一个Service，那么会消耗太多的系统资源，而且会使我们的应用看起来很重量级。针对上述问题，我们需要减少Service的数量，将所有的AIDL放在同一个Service中去管理。  
每个业务模块创建自己的AIDL接口并实现此接口，然后向服务端提供自己的唯一标识和其对应的Binder对象；对服务端来说，只需要一个Service，并提供一个queryBinder接口，业务模块可以根据这个接口获取自己的binder对象。  
由此可见，binder连接池的作用就是将每个业务模块的binder请求统一转发到远程Service中执行，从而避免了重复创建Service的过程。
![image](http://note.youdao.com/yws/res/33242/AB5746CFF83A4B49977453C5E941FB12)

### binder连接池示例
1. 提供两个AIDL接口（ISecurityCenter和ICompute）来模拟个业务模块都要使用的AIDL情况，其中ISecurityCenter接口提供加解密功能。声明如下：
```
//ISecurityCenter.aidl
package com.example.chapter_2.binderpool;

interface ISecurityCenter {
    String encrypt(String content);
    String decrypt(String password);
}

//ICompute.aidl
package com.example.chapter_2.binderpool;

interface ICompute {
    int add(int a, int b);
}

//SecurityCenterImpl.java
public class SecurityCenterImpl extends ISecurityCenter.Stub {

    private static final char SECRET_CODE = '^';

    @Override
    public String encrypt(String content) throws RemoteException {
        char[] chars = content.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] ^= SECRET_CODE;
        }
        return new String(chars);
    }

    @Override
    public String decrypt(String password) throws RemoteException {
        return encrypt(password);
    }

}

//ComputeImpl.java
public class ComputeImpl extends ICompute.Stub {

    @Override
    public int add(int a, int b) throws RemoteException {
        return a + b;
    }

}
```

2. 为Binder连接池创建AIDL接口IBinderPool.aidl
```
//IBinderPool.aidl
package com.example.chapter_2.binderpool;

interface IBinderPool {

    /**
     * @param binderCode, the unique token of specific Binder<br/>
     * @return specific Binder who's token is binderCode.
     */
    IBinder queryBinder(int binderCode);
}
```

3. 为Binder连接池创建远程Service并实现IBinderPool
```
//BinderPoolImpl.java
public class BinderPoolImpl extends IBinderPool.Stub {

    public BinderPoolImpl() {
        super();
    }

    @Override
    public IBinder queryBinder(int binderCode) {
        IBinder binder = null;
        switch (binderCode) {
            case BINDER_SECURITY_CENTER: {
                binder = new SecurityCenterImpl();
                break;
            }
            case BINDER_COMPUTE: {
                binder = new ComputeImpl();
                break;
            }
            default:
                break;
        }

        return binder;
    }
}

//BinderPoolService.java
public class BinderPoolService extends Service {

    private static final String TAG = "BinderPoolService";

    private Binder mBinderPool = new BinderPool.BinderPoolImpl();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinderPool;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
```

4. 业务通过queryBinder完成多个AIDL接口的工作
```
//BinderPool.java,获取不同的binder对象
public class BinderPool {
    private static final String TAG = "BinderPool";
    public static final int BINDER_NONE = -1;
    public static final int BINDER_COMPUTE = 0;
    public static final int BINDER_SECURITY_CENTER = 1;

    private Context mContext;
    private IBinderPool mBinderPool;
    private static volatile BinderPool sInstance;
    private CountDownLatch mConnectBinderPoolCountDownLatch;

    private BinderPool(Context context) {
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    public static BinderPool getInsance(Context context) {
        if (sInstance == null) {
            synchronized (BinderPool.class) {
                if (sInstance == null) {
                    sInstance = new BinderPool(context);
                }
            }
        }
        return sInstance;
    }

    private synchronized void connectBinderPoolService() {
        mConnectBinderPoolCountDownLatch = new CountDownLatch(1);
        Intent service = new Intent(mContext, BinderPoolService.class);
        mContext.bindService(service, mBinderPoolConnection,
                Context.BIND_AUTO_CREATE);
        try {
            mConnectBinderPoolCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * query binder by binderCode from binder pool
     *
     * @param binderCode the unique token of binder
     * @return binder who's token is binderCode<br>
     * return null when not found or BinderPoolService died.
     */
    public IBinder queryBinder(int binderCode) {
        IBinder binder = null;
        try {
            if (mBinderPool != null) {
                binder = mBinderPool.queryBinder(binderCode);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return binder;
    }

    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // ignored.
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                mBinderPool.asBinder().linkToDeath(mBinderPoolDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mConnectBinderPoolCountDownLatch.countDown();
        }
    };

    private IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.w(TAG, "binder died.");
            mBinderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipient, 0);
            mBinderPool = null;
            connectBinderPoolService();
        }
    };

}

//BinderPoolActivity.java
public class BinderPoolActivity extends Activity {
    private static final String TAG = "BinderPoolActivity";

    private ISecurityCenter mSecurityCenter;
    private ICompute mCompute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binder_pool);
        new Thread(new Runnable() {

            @Override
            public void run() {
                doWork();
            }
        }).start();
    }

    private void doWork() {
        BinderPool binderPool = BinderPool.getInsance(BinderPoolActivity.this);
        IBinder securityBinder = binderPool
                .queryBinder(BinderPool.BINDER_SECURITY_CENTER);
        ;
        mSecurityCenter = ISecurityCenter.Stub
                .asInterface(securityBinder);
        Log.d(TAG, "visit ISecurityCenter");
        String msg = "helloworld-安卓";
        Log.i(TAG, "content:" + msg);
        try {
            String password = mSecurityCenter.encrypt(msg);
            Log.i(TAG, "encrypt:" + password);
            Log.i(TAG, "decrypt:" + mSecurityCenter.decrypt(password));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "visit ICompute");
        IBinder computeBinder = binderPool
                .queryBinder(BinderPool.BINDER_COMPUTE);
        ;
        mCompute = ICompute.Stub.asInterface(computeBinder);
        try {
            Log.i(TAG, "3+5=" + mCompute.add(3, 5));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
```
为什么要在子线程去执行？因为在Binder连接池中，我们通过CountDownLatch将bindService这一异步操作转换成同步操作，这就意味着它可能是耗时的，另外binder方法的调用过程可能也是耗时的，因此不建议在主线程执行。  
BinderPool是一个单例实现，为了优化体验，可以提前在Application初始化时就去初始化BinderPool。  
另外BinderPool中有断线重连机制，当远程服务意外终止时，binderPool会重新建立连接。