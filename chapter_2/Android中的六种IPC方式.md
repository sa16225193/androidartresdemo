## 1. 使用Bundle

四大组件中的三大组件（Activity、Service、Receiver）都是支持在Intent中传递Bundle数据的。由于Bundle实现了Parcelable接口，所以它可以方便地在不同的进程间传输。  

Bundle传输的数据必须能够被序列化，比如基本类型、实现了Parcelable接口的对象、实现了Serializable接口的对象以及一些Android支持的特殊对象。  

除了直接传递数据这种典型的使用场景，还有一种特殊的使用场景。比如A进程正在进行一个计算，计算完成后要启动B进程的一个组件并把计算接口传递给B进程。可以考虑通过Intent启动进程B的Service（比如IntentService），计算完成后再启动进程B中要启动的组件，这样就能直接获取计算结果，且避免了跨进程通信的问题。

## 2. 使用文件共享
共享文件也是一种不错的进程间通信方式，两个进程通过读/写同一个文件来交换数据，比如A进程把数据写入文件，B进程通过读取这个文件来获取数据。

比如在进程A的Activity中向文件中写入User对象，在进程B的Activity中读取该文件的User对象。
```
//A进程的Activity写入User对象
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
    
//B进程的Activity向文件中读取User对象
private void recoverFromFile() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                User user = null;
                File cachedFile = new File(MyConstants.CACHE_FILE_PATH);
                if (cachedFile.exists()) {
                    ObjectInputStream objectInputStream = null;
                    try {
                        objectInputStream = new ObjectInputStream(
                                new FileInputStream(cachedFile));
                        user = (User) objectInputStream.readObject();
                        Log.d(TAG, "recover user:" + user);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        MyUtils.close(objectInputStream);
                    }
                }
            }
        }).start();
    }
```
通过文件共享的方式不支持并发读/写，同时读出的内容可能不是最新的。


## 3. 使用Messenger
Messenger翻译为信使，通过它可以在不同进程中传递Message对象。Messenger是一种轻量级的IPC方案，它的底层实现是AIDL。  
在Messenger中进行数据传递必须将数据放入Message中，而Messenger和Message都实现了Parcelable接口，因此可以跨进程传输。Message中能使用的载体只有what、arg1、arg2、Bundle和replyTo。Message中的object字段在同一个进程中是很实用的，但是在进程间通信的时候，在Android2.2以前不支持，2.2以后，只有系统提供的实现了Parcelable对象才支持，自定义的Pacelable对象不支持。但可以通过Bundle来传递。  
当客户端发送消息时，如果需要接收服务端回复的消息，通过Message的replyTo参数将客户端的Messenger对象传递给服务端。

1. 构造方法
```
public Messenger(Handler target) {
    mTarget = target.getIMessenger();
}

public Messenger(IBinder target) {
    mTarget = IMessenger.Stub.asInterface(target);
}
```

2. 使用示例
- 服务端代码
```
public class MessengerService extends Service {

    private static final String TAG = "MessengerService";

    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MyConstants.MSG_FROM_CLIENT:
                Log.i(TAG, "receive msg from Client:" + msg.getData().getString("msg"));
                Messenger client = msg.replyTo;
                Message relpyMessage = Message.obtain(null, MyConstants.MSG_FROM_SERVICE);
                Bundle bundle = new Bundle();
                bundle.putString("reply", "嗯，你的消息我已经收到，稍后会回复你。");
                relpyMessage.setData(bundle);
                try {
                    client.send(relpyMessage);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new MessengerHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

}

//注册service
<service
            android:name=".messenger.MessengerService"
            android:process=":remote" >
            <intent-filter>
                <action android:name="com.ryg.MessengerService.launch" />
            </intent-filter>
        </service>
```

- 客户端代码
```
public class MessengerActivity extends Activity {

    private static final String TAG = "MessengerActivity";

    private Messenger mService;
    private Messenger mGetReplyMessenger = new Messenger(new MessengerHandler());
    
    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MyConstants.MSG_FROM_SERVICE:
                Log.i(TAG, "receive msg from Service:" + msg.getData().getString("reply"));
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.d(TAG, "bind service");
            Message msg = Message.obtain(null, MyConstants.MSG_FROM_CLIENT);
            Bundle data = new Bundle();
            data.putString("msg", "hello, this is client.");
            msg.setData(data);
            msg.replyTo = mGetReplyMessenger;
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        Intent intent = new Intent(this, MessengerService.class);
        intent.setPackage("com.example.chapter_2.messenger");
        intent.setAction("com.ryg.MessengerService.launch");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }
}
```

## 4. 使用AIDL
- 客户端调用远程服务的方法，被调用的方法运行在服务端的Binder线程池中，同时客户端线程会被挂起，所以尽量在子线程调用远程服务的方法。  
- 客户端的onServiceConnected和onServiceDisconnected方法都允许在主线程，所以也不能在这两个方法中直接调用远程服务的耗时方法。  
- 服务端的方法本身就运行在服务端的Binder线程池中，所以服务端方法本身就可以执行耗时操作，不需要开子线程进行异步任务。  
- 当远程服务需要调用客户端的listener中的方法时，被调用的方法也运行在Binder线程池中，只不过是客户端的线程池。此时不能访问UI，如果要访问UI，请使用Handler切换到主线程。 
- Binder可能意外死亡，两种重连服务方法：一是给Binder设置DeathRecipient监听，在binderDied回调中重连远程服务；二是在onServiceDisconnected中重连远程服务。二者区别，binderDied回调在binder线程执行，onServiceDisconnected在主线程执行。
- 远程服务添加校验，验证方式有多种：
比如权限校验，先在AndroidMenifest中声明权限
```
//声明权限
<permission
        android:name="com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE"
        android:protectionLevel="normal" />

//申请权限
    <uses-permission android:name="com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE" />
```
在onBind或者onTransact方法中进行权限验证。
```
//onBind加入权限校验
@Override
    public IBinder onBind(Intent intent) {
        int check = checkCallingOrSelfPermission("com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE");
        Log.d(TAG, "onbind check=" + check);
        //权限校验失败返回null，这样客户端就无法绑定远程服务
        if (check == PackageManager.PERMISSION_DENIED) {
            return null;
        }
        return mBinder;
    }
    
//onTransact加入权限校验
public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            int check = checkCallingOrSelfPermission("com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE");
            Log.d(TAG, "check=" + check);
            //校验失败返回false，这样远程方法不会执行
            if (check == PackageManager.PERMISSION_DENIED) {
                return false;
            }

            //也可以通过包名进行校验
            String packageName = null;
            String[] packages = getPackageManager().getPackagesForUid(
                    getCallingUid());
            if (packages != null && packages.length > 0) {
                packageName = packages[0];
            }
            Log.d(TAG, "onTransact: " + packageName);
            if (!packageName.startsWith("com.example.android_art_res_demo")) {
                return false;
            }

            return super.onTransact(code, data, reply, flags);
        }
```
还可以采用Uid和Pid来做验证，通过getCallingUid()和getCallingPid可以拿到客户端所属应用的Uid和Pid，通过这两个参数可以做一些验证工作，比如验证包名。另外做权限验证时，可以为Service指定android:permission属性。

1. AIDL接口的创建
```
//Book.aidl
package com.example.chapter_2;

parcelable Book;


//IBookManager.aidl
package com.example.chapter_2;

import com.example.chapter_2.Book;

interface IBookManager {
     List<Book> getBookList();
     void addBook(in Book book);
     void registerListener(IOnNewBookArrivedListener listener);
     void unregisterListener(IOnNewBookArrivedListener listener);
}

//IOnNewBookArrivedListener.aidl
package com.example.chapter_2;

import com.example.chapter_2.Book;

interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}
```
在AIDL文件中，并不是所有的数据类型都支持，主要支持如下几种类型：
- 基本数据类型（int、long、char、boolean、double等)
- String和CharSequence
- List：只支持ArrayList，里面每个元素都必须能够被AIDL支持
- Map：只支持HashMap，里面的每个元素都必须被AIDL支持，包括key和value
- Parcelable：所有实现了Parcelable接口的对象
- AIDL：所有的AIDL接口本身也可以在AIDL文件中使用
自定义的Parcelable对象和AIDL对象必须要显式import进来，即使在同一个包内。  

> 建议:所有和AIDL相关的类和文件全部放入一个包中，这样做的好处是，当客户端是另外一个应用时，可以直接把整个包复制到客户端工程。

- 服务端代码
服务端的List使用CopyOnWriteArrayList，支持并发读/写。AIDL方法是在服务端的Binder线程池中执行的，因此当多个客户端同时连接的时候，会存在多个线程同时访问的情形。  
AIDL中能够使用的List只有ArrayList，但AIDL所支持的是抽象的List，虽然服务端返回的是CopyOnWriteArrayList，但是在Binder中会按照List的规范去访问数据，并最终形成一个新的ArrayList传递给客户端，类似的还有ConcurrentHashMap。
```
//BookManagerService.java
public class BookManagerService extends Service {

    private static final String TAG = "BMS";

    private AtomicBoolean mIsServiceDestoryed = new AtomicBoolean(false);

    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();

    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<>();

    private Binder mBinder = new IBookManager.Stub() {

        @Override
        public List<Book> getBookList() {
            return mBookList;
        }

        @Override
        public void addBook(Book book) {
            mBookList.add(book);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            int check = checkCallingOrSelfPermission("com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE");
            Log.d(TAG, "check=" + check);
            if (check == PackageManager.PERMISSION_DENIED) {
                return false;
            }

            String packageName = null;
            String[] packages = getPackageManager().getPackagesForUid(
                    getCallingUid());
            if (packages != null && packages.length > 0) {
                packageName = packages[0];
            }
            Log.d(TAG, "onTransact: " + packageName);
            if (!packageName.startsWith("com.example.android_art_res_demo")) {
                return false;
            }

            return super.onTransact(code, data, reply, flags);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) {
            mListenerList.register(listener);

            final int N = mListenerList.beginBroadcast();
            mListenerList.finishBroadcast();
            Log.d(TAG, "registerListener, current size:" + N);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) {
            boolean success = mListenerList.unregister(listener);

            if (success) {
                Log.d(TAG, "unregister success.");
            } else {
                Log.d(TAG, "not found, can not unregister.");
            }
            final int N = mListenerList.beginBroadcast();
            mListenerList.finishBroadcast();
            Log.d(TAG, "unregisterListener, current size:" + N);
        };

    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1, "Android"));
        mBookList.add(new Book(2, "Ios"));
        new Thread(new ServiceWorker()).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        int check = checkCallingOrSelfPermission("com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE");
        Log.d(TAG, "onbind check=" + check);
        if (check == PackageManager.PERMISSION_DENIED) {
            return null;
        }
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mIsServiceDestoryed.set(true);
        super.onDestroy();
    }

    private void onNewBookArrived(Book book) throws RemoteException {
        mBookList.add(book);
        final int N = mListenerList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnNewBookArrivedListener l = mListenerList.getBroadcastItem(i);
            if (l != null) {
                try {
                    l.onNewBookArrived(book);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mListenerList.finishBroadcast();
    }

    private class ServiceWorker implements Runnable {
        @Override
        public void run() {
            // do background processing here.....
            while (!mIsServiceDestoryed.get()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId = mBookList.size() + 1;
                Book newBook = new Book(bookId, "new book#" + bookId);
                try {
                    onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

//注册service
<service
            android:name=".BookManagerService"
            android:process=":remote" >
        </service>
        
//注册和申请权限，用于Service的权限校验
<permission
        android:name="com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE"
        android:protectionLevel="normal" />

    <uses-permission android:name="com.ryg.chapter_2.permission.ACCESS_BOOK_SERVICE" />
```

- 客户端实现
```
public class BookManagerActivity extends Activity {

    private static final String TAG = "BookManagerActivity";
    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;

    private IBookManager mRemoteBookManager;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_NEW_BOOK_ARRIVED:
                Log.d(TAG, "receive new book :" + msg.obj);
                break;
            default:
                super.handleMessage(msg);
            }
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binder died. tname:" + Thread.currentThread().getName());
            if (mRemoteBookManager == null)
                return;
            mRemoteBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mRemoteBookManager = null;
            // TODO:这里重新绑定远程Service
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            IBookManager bookManager = IBookManager.Stub.asInterface(service);
            mRemoteBookManager = bookManager;
            try {
                mRemoteBookManager.asBinder().linkToDeath(mDeathRecipient, 0);
                List<Book> list = bookManager.getBookList();
                Log.i(TAG, "query book list, list type:"
                        + list.getClass().getCanonicalName());
                Log.i(TAG, "query book list:" + list.toString());
                Book newBook = new Book(3, "Android进阶");
                bookManager.addBook(newBook);
                Log.i(TAG, "add book:" + newBook);
                List<Book> newList = bookManager.getBookList();
                Log.i(TAG, "query book list:" + newList.toString());
                bookManager.registerListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mRemoteBookManager = null;
            Log.d(TAG, "onServiceDisconnected. tname:" + Thread.currentThread().getName());
        }
    };

    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {

        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook)
                    .sendToTarget();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_manager);
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void onButton1Click(View view) {
        Toast.makeText(this, "click button1", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (mRemoteBookManager != null) {
                    try {
                        List<Book> newList = mRemoteBookManager.getBookList();
                        Log.i(TAG, "onButton1Click()...query book list:" + newList.toString());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        if (mRemoteBookManager != null
                && mRemoteBookManager.asBinder().isBinderAlive()) {
            try {
                Log.i(TAG, "unregister listener:" + mOnNewBookArrivedListener);
                mRemoteBookManager.unregisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();
    }

}
```
虽然服务端返回的是CopyOnWriteArrayList类型，但是客户端收到的仍然是ArrayList类型。

3. RemoteCallbackList
是系统专门提供用于删除跨进程listener的接口。RemoteCallbackList是一个泛型，支持管理任意的AIDL接口。
```
public class RemoteCallbackList<E extends IInterface>
```
它的工作原理很简单，内部有一个Map结构专门保存所有的AIDL回调，这个Map的key是IBinder类型，value是Callback类型
```
ArrayMap<IBinder, Callback> mCallbacks = new ArrayMap<>();
```
其中Callback封装了真正的远程listener，当客户端注册listener的时候，它会把这个listener的信息存入mCallbacks中。
```
IBinder key = listener.asBinder();
Callback value = new Callback(listener, cookie);
```
虽然多进程传输的客户端同一个对象会在服务端生成不同的对象，但是这些对象的binder对象是同一个。

RemoteCallback标准用法
```
private void onNewBookArrived(Book book) throws RemoteException {
        mBookList.add(book);
        final int N = mListenerList.beginBroadcast();//1. 开始
        for (int i = 0; i < N; i++) {
            IOnNewBookArrivedListener l = mListenerList.getBroadcastItem(i);//2. 获取listener
            if (l != null) {
                try {
                    l.onNewBookArrived(book);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mListenerList.finishBroadcast();//3. 结束
    }
```
使用RemoteCallbackList，有一点需要注意，我们无法像List一样去操作它。遍历RemoteCallbackList必须按照下面的方式进行，其中beginBroadcast和finishBroadcast必须配对使用，哪怕我们仅仅是想获取RemoteCallbackList中的元素个数，比如
```
final int N = mListenerList.beginBroadcast();
for(int i = 0; i < N; i++) {
    IOnNewBookArrivedListener l = mListenerList.getBroadcastItem(i);
    if (l != null) {
        //
    }
}
mListenerList.finishBroadcast();
```

## 5. 使用ContentProvider
ContentProvider是Android中提供的专门用于不同应用间数据共享的方式。和Messenger一样，ContentProvider底层实现也是Binder。
ContentProvider主要以表格的形式来组织数据，并且可以包含多个表。除了表格的形式，ContentProvider还支持文件数据，比如图片、视频等。虽然ContentProvider的底层数据看起来很像一个SQLite数据库，但是ContentProvider对底层的数据存储方式没有任何要求。

## 6. 使用Socket
使用Socket通信需要注意两点：
1. 首先需要声明权限
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
2. 不能在主线程访问网络，Android 4.0及以上会抛出异常android.os.NetworkOnMainThreadException

- 代码示例
服务端代码
```
public class TCPServerService extends Service {

    private boolean mIsServiceDestoryed = false;
    private String[] mDefinedMessages = new String[] {
            "你好啊，哈哈",
            "请问你叫什么名字呀？",
            "今天北京天气不错啊，shy",
            "你知道吗？我可是可以和多个人同时聊天的哦",
            "给你讲个笑话吧：据说爱笑的人运气不会太差，不知道真假。"
    };

    @Override
    public void onCreate() {
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mIsServiceDestoryed = true;
        super.onDestroy();
    }

    private class TcpServer implements Runnable {

        @SuppressWarnings("resource")
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8688);
            } catch (IOException e) {
                System.err.println("establish tcp server failed, port:8688");
                e.printStackTrace();
                return;
            }

            while (!mIsServiceDestoryed) {
                try {
                    // 接受客户端请求
                    final Socket client = serverSocket.accept();
                    System.out.println("accept");
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        };
                    }.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void responseClient(Socket client) throws IOException {
        // 用于接收客户端消息
        BufferedReader in = new BufferedReader(new InputStreamReader(
                client.getInputStream()));
        // 用于向客户端发送消息
        PrintWriter out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(client.getOutputStream())), true);
        out.println("欢迎来到聊天室！");
        while (!mIsServiceDestoryed) {
            String str = in.readLine();
            System.out.println("msg from client:" + str);
            if (str == null) {
                break;
            }
            int i = new Random().nextInt(mDefinedMessages.length);
            String msg = mDefinedMessages[i];
            out.println(msg);
            System.out.println("send :" + msg);
        }
        System.out.println("client quit.");
        // 关闭流
        MyUtils.close(out);
        MyUtils.close(in);
        client.close();
    }

}
```

## IPC方式比较
名称|优点|缺点|适用场景
--|--|--|--|--
Bundle|简单易用|只有传输Bundle支持的数据类型|四大组件间的进程间通信
文件共享|简单易用|不适合高并发场景，并且无法做到进程间的即时通信|无并发访问情形，交换简单的数据实时性不高的场景
AIDL|功能强大，支持一对多并发通信，支持实时通信|使用稍复杂，需要处理好线程同步|一对多通信且有RPC需求
Messenger|功能一般，支持一对多串行通信，支持实时通信|不能很好的处理高并发情形，不支持RPC，数据通过Message进行传输，因此只能传输Bundle支持的数据类型|低并发的一对多即时通信，无RPC需求，或者无须返回结果的RPC需求
ContentProvider|在数据源访问方面功能强大，支持一对多并发数据共享，可通过Call方法扩展其他操作|可以理解为受约束的AIDL，主要提供数据源的CRUD操作|一对多的进程间数据共享
Socket|功能强大，可以通过网络传输字节流，支持一对多并发实时通信|实现细节稍微有点繁琐，不支持直接的RPC|网络数据交换

