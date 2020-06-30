## 使用WindowManager添加View
下面代码是将一个Button添加到屏幕坐标为（100, 300）的位置上，并可以跟随手指移动
```
public class WindowActivity extends Activity implements OnTouchListener {

    private static final String TAG = "WindowActivity";

    private Button mFloatingButton;
    private LayoutParams mLayoutParams;
    private WindowManager mWindowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_window);
        initView();
    }

    private void initView() {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    public void onButtonClick(View v) {
        mFloatingButton = new Button(this);
        mFloatingButton.setText("click me");
        mLayoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0,
                PixelFormat.TRANSPARENT);
        mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE
                | LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        mLayoutParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mLayoutParams.x = 100;
        mLayoutParams.y = 300;
        mFloatingButton.setOnTouchListener(this);
        mWindowManager.addView(mFloatingButton, mLayoutParams);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int x = (int) event.getX();
                int y = (int) event.getY();
                mLayoutParams.x = rawX;
                mLayoutParams.y = rawY;
                mWindowManager.updateViewLayout(mFloatingButton, mLayoutParams);
                break;
            }
            case MotionEvent.ACTION_UP: {
                break;
            }
            default:
                break;
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        try {
            mWindowManager.removeView(mFloatingButton);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
```

WindowManager.LayoutParams的flags和type参数比较重要。Flags参数表示Window的属性，有以下几个常用选项：  

- FLAG_NOT_FOCUSABLE
表示Window不需要获取焦点，也不需要接收各种输入事件，此标记会同时启用FLAG_NOT_TOUCH_MODAL,最终事件会直接传递给下层的具有焦点的Window。
- FLAG_NOT_TOUCH_MODAL
在此模式下，系统会将当前Window区域以外的单击事件传递给底层的Window，当前Window区域以内的单击事件则自己处理。这个标记很重要，一般来说都需要开启此标记，否则其他Window将无法收到单击事件。
- FLAG_SHOW_WHEN_LOCKED
开启此模式可以让Window显示在锁屏的界面上。

Type参数表示Window的类型，有三种，分别是应用Window、子Window和系统Window。应用类Window对应一个Activity。子Window不能独立存在，需要附属在特定的父Window中，比如常见的Dialog就是一个子Window。系统Window是需要声明权限的，比如Toast和系统状态栏都是系统Window。

Window是分层的，每个Window都有对应的z-ordered，层级大的会覆盖在层级小的Window上面。应用Window的层级范围是1~99，子Window的层级范围是1000~1999，系统Window的层级范围是2000~2999。

系统Window一般可以选用TYPE_SYSTEM_OVERLAY或者TYPE_SYSTEM_ERROR。如果采用TYPE_SYSTEM_ERROR，需要`mLayoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR`，同时声明权限：`<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>`。

### ViewManager
WindowManager继承自ViewManager
```
public interface ViewManager {
    //添加View
    public void addView(View view, ViewGroup.LayoutParams params);
    
    //更新View
    public void updateViewLayout(View view, ViewGroup.LayoutParams params);
    
    //删除View
    public void removeView(View view);
}
```

## Window的内部机制

Window是一个抽象的概念，每个Window都对应着一个View和一个ViewRootImpl，Window和View通过ViewRootImpl来建立联系。实际使用中无法直接访问Window，必须通过WindowManager。

### Window的添加过程
Window的添加通过WindowManager的addView来实现，而WindowManager真正实现是WindowManagerImpl类。在WindowManagerImpl中Window三大操作的实现如下：
```
@Override
public void addView(View view, ViewGroup.LayoutParams params) {
    mGlobal.addView(view, params, mDisplay, mParentWindow);
}

@Override
public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
    mGlobal.updateViewLayout(view, params);
}

@Override
public void removeView(View view) {
    mGlobal.removeView(view, false);
}
```
WindowManagerImpl对Window的三大操作全部交给WindowManagerGlobal来处理。WindowManagerImpl这种工作模式是典型的桥接模式，将所有操作全部委托给WindowManagerGlobal来实现。WindowManagerGlobal的addView主要分如下几步：
1. 检查参数是否合法，如果是子Window还需调整布局参数
```
if (view == null) throw new IllegalArgumentException("view must not be null");
if (display == null) throw new IllegalArgumentException("display must not be null");

if (!(params instanceof WindowManager.LayoutParams)) {
    throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
}

final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;
if (parentWindow != null) {
    parentWindow.adjustLayoutParamsForSubWindow(wparams);
}
```

2. 创建ViewRootImpl并将View添加到列表中
在WindowManagerGlobal内部有如下几个列表比较重要：
```
//存储所有Window所对应的View
private final ArrayList<View> mViews = new ArrayList<View>();
//存储所有Window对应的ViewRootImpl
private final ArrayList<ViewRootImpl> mRoots = new ArrayList<ViewRootImpl>();
//存储所有Window对应的布局参数
private final ArrayList<WindowManager.LayoutParams> mParams = new ArrayList<WindowManager.LayoutParams>();
//存储正在被删除的View的对象，或者说调用了removeView方法但还没未完成删除的Window对象。
private final ArraySet<View> mDyingViews = new ArraySet<View>();

//addView
root = new ViewRootImpl(view.getContext(), display);
view.setLayoutParams(wparams);

mViews.add(view);
mRoots.add(root);
mParams.add(wparams);
```

3. 通过ViewRootImpl来更新界面并完成Window的添加过程
View的绘制过程由ViewRootImpl来完成，在setView内部会通过requestLayout来完成异步刷新请求。
```
public void requestLayout() {
    if (!mHandlingLayoutInLayoutRequest) {
        checkThread();
        mLayoutRequested = true;
        scheduleTraversals();
    }
}
```
接着会通过WindowSession最终完成Window的添加。mWindowSession的类型是IWindowSession，是一个binder对象，真正的实现类是Session。所以Window的添加过程是一次IPC调用
```
try {
    mOrigWindowType = mWindowAttribute.type;
    mAttachInfo.mRecomputeGlobalAttributes = true;
    collectViewAttributes();
    res = mWindowSession.addToDisplay(mWindow, mSeq, mWindowAttributes, getHostVisibility(), mDisplay.getDisplayId(), mAttachInfo.mContentInsets, mInputChannel);
} catch(RemoteException e) {
    mAdded = false;
    mView = null;
    mAttachInfo.mRootView = null;
    mInputChannedl = null;
    mFallbackEventHandler.setView(null);
    unsheduleTraversals();
    setAccessibilityFocus(null, null);
    throw new RuntimeException("Adding window failed", e);
}
```
在Session内部会通过WindowManagerService来实现Window的添加
```
public int addToDisplay(IWindow window, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int displayId, Rect outContentInsets, InputChannel outInputChannel) {
    return mService.addWindow(this, window, seq, attrs, viewVisibility, displayId, outContentInsets, outInputChannel);
}
```

### Window的删除过程
Window的删除也是通过WindowManagerGlobal来实现的。下面是WindowManagerGlobal的removeView的实现：
```
public void removeView(View view, boolean immediate) {
    if (view == null) throw new IllegalArgumentException("view must not be null");
    
    synchronized(mLock) {
        int index findViewLocked(view, true);
        View curView = mRoots.get(index).getView();
        removeViewLocked(index, immediate);
        if (curView == view) {
            return;
        }
        
        throw new IllegalStateException("Calling with view " + view  + " but the ViewAncestor is attached to " + curView);
    }
}
```
先通过findViewLocked()找到待删除的View的索引，然后调用removeViewLocked来做进一步的删除。
```
private void removeViewLocked(int index, boolean immediate) {
    ViewRootImpl root = mRoots.get(index);
    View view = root.getView();
    if (view != null) {
        InputMethodManager imm = InputMethodManager.getInstance();
        if (imm != null) {
            imm.windowDismissed(mViews.get(index).getWindowToken());
        }
    }
    boolean deferred = root.die(immediate);
    if (view != null) {
        view.assignParent(null);
        if (deferred) {
            mDyingViews.add(view);
        }
    }
}
```
removeViewLocked是通过ViewRootImpl来完成删除操作。在WindowManager中提供两种删除接口removeView和removeViewImmediate，它们分别表示异步删除和同步删除。一般不需要使用removeViewImmediate来删除WIndow以免发生意外错误。  
具体的删除操作由ViewRootImpl的die方法来完成，只是发送一个请求删除的消息。
```
boolean die(boolean immediate) {
    if(immediate && !mIsInTraversal) {
        doDie();
        return false;
    }
    
    if(!mIsDrawing) {
        destroyHardwareRederer();
    } else {
        Log.e(TAG, "Attempting to destroy the window while drawing!\n" + " window = " + this + ", title=" + mWindowAttributes.getTitle());
    }
    mHandler.sendEmptyMessage(MSG_DIE);
    return true;
}
```
在doDie方法内部会调用dispatchDetachedFromWindow方法，执行删除View的逻辑。dispatchDetachedFromWindow方法主要做四件事：
1. 垃圾回收相关的工作，比如清除数据和消息、移除回调
2. 通过Session的remove方法删除Window:mWindowSession.remove(mWindow),同样是IPC过程，最终会调用WindowManagerService的removeWindow方法
3. 调用View的dispatchDetachedFromWindow方法，在内部调用View的onDetachedFromWindow以及onDetachedFromWindowInternal。
4. 调用WindowManagerGlobal的doRemoveView方法刷新数据，包括mRoots、mParams以及mDyingViews，需要将当前Window所关联的这三类对象从类别中删除。

### Window的更新过程
还是从WindowManagerGlobal的updateViewLayout方法开始：
```
public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
    if (view == null) {
        throw new IllegalArgumentException("view must not be null");
    }
    if (!(params instanceof WindowManager.LayoutParams)) {
        throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
    }
    final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;
    
    view.setLayoutParams(wparams);
    
    synchronized(mLock) {
        int index = findViewLocked(view, true);
        ViewRootImpl root = mRoots.get(index);
        mParams.remove(index);
        mParams.add(index, wparams);
        root.setLayoutParams(wparams, false);
    }
}
```
updateViewLayout方法做的事情就比较简单了，首先需要更新View的LayoutParams并替换掉老的LayoutParams，接着再更新ViewRootImpl中的LayoutParams。这一步是通过ViewRootImpl.setLayoutParams方法实现的。在ViewRootImpl中会通过scheduleTraversals方法来对View重新布局，包括测量、布局、重绘三个过程。另外，ViewRootImpl还会通过WindowSession来更新Window的视图，这个过程最终由WindowManagerService的relayoutWindow来具体实现，同样是一个IPC过程。

## Window的创建过程

View是Android中的视图的呈现方式，但是View不能单独存在，必须附着在抽象的Window上面，因此有视图的地方就有Window。Android中提供视图的地方有Activity、Dialog、Toast，除此之外，还有PopUpWindow、菜单等。
### Activity的Window创建过程
Activity最终会有ActivityThread中的performLaunchActivity()来完成启动过程，这个方法内部会通过类加载器创建Activity的实例对象，并调用其attach方法为其关联运行过程中所依赖的一系列上下文环境变量。
```
java.lang.ClassLoader cl = r.packageInfo.getClassLoader();
activity = mInstrumentation.newActivity(cl, component.getClassName(), r.intent);
...
if (activity != null) {
    Context appContext = createBaseContextForActivity(r, activity);
    CharSequence title = r.activityInfo.loadLabel(appContext.getPackageManager());
    Configuration config = new Configuration(mCompatConfiguration);
    if (DEBUG_CONFIGURATION) Slog.v(TAG, "Launching activity " + r.activityInfo.name + " with config " + config);
    activity.attach(appContext, this, getInstrumentation(), r.token, r.ident, app, r.intent, r.activityInfo, title, r.parent, r.embeddedId, r.lastNonConfigurationInstances, config, r.voiceInteractor);
}
```
在Activity的attach方法里，系统会创建Activity所属的Window对象并为其设置回调接口，Window对象的创建是通过PolicyManager的makeNewWindow方法实现的。由于Activity实现了Window的Callback接口，因此当Window接收到外界的状态改变时就会回调Activity的方法。Callback接口中的方法很多，比如onAttachedToWindow、onDetachedFromWindow、dispatchTouchEvent。
```
mWindow = PolicyManager.makeNewWindow(this);
mWindow.setCallback(this);
mWindow.setOnWindowDismissedCallback(this);
mWindow.getLayoutInflater().setPrivateFactory(this);
if (info.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
    mWindow.setSoftInputMode(info.softInputMode);
}
if (info.uiOptions != 0) {
    mWindow.setUiOptions(info.uiOptions);
}
```
可以看出，Activity的Window是通过PolicyManager的一个工厂方法来创建的。PolicyManager是一个策略类，几个工厂方法都在策略接口IPolicy中声明了。
```
public interface IPolicy {
    public Window makeNewWindow(Context context);
    public LayoutInflater makeNewLayoutInflater(Context context);
    public WindowManagerPolicy makeNewWindowManager();
    public FallbackEventHandler makeNewFallbackEventHandler(Context context);
}
```
在实际的调用中，PolicyManager的真正实现是Policy类，Policy类中的makeNewWindow方法如下，可以发现Window的具体实现是PhoneWindow。
```
public Window makeNewWindow(Context context) {
    return new PhoneWindow(context);
}
```
下面分析Activity的视图是怎么附属到Window上，由于Activity的视图由setContentView提供
```
public void setContentView(int layoutResId) {
    getWindow().setContentView(layoutResId);
    initWindowDecorActionBar();
}
```
从Activity的setContentView的实现可以看出，Activity将具体实现交给了Window处理，而Window的具体实现是PhoneWindow，所以只需看PhoneWindow的相关逻辑即可。PhoneWindow的setContentView方法大致如下：
1. 如果没有DecorView，就创建它
DecorView是Activity中的顶级View，一般包含标题栏和内部栏，标题栏会随主题变换而改变，但内容栏一定存在。并且内容具体固定的id为android.R.id.content。DecorView的创建过程由installDecor方法来完成，在方法内部会通过generateDecor方法直接创建DecorView，此时DecorView只是空白的FrameLayout。
```
protected DecorView generateDecor() {
    return new DecorView(getContext(), -1);
}
```
为了初始化DecorView的结构，PhoneWindow还需要通过generateLayout方法来加载具体的布局文件到DecorView中，具体的布局文件和系统版本以及主题有关，这个过程如下所示：
```
View in = mLayoutInflater.inflate(layoutResource, null);
decor.addView(in, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
mContentRoot = (ViewGroup) in;
ViewGroup contentParent = (ViewGroup) findViewById(ID_ANDROID_CONTENT);
```
其中ID_ANDROID_CONTENT的定义如下，这个id所对应的ViewGroup就是mContentParent：
```
public static final int ID_ANDROID_CONTENT = com.android.internal.R.id.content;
```

2. 将View添加到DecorView的mContentParent中
直接将Activity的视图添加到DecorView的mContentParent中，即`mLayoutInfalter.inflate(layoutResId, mContentParent)`。

3. 回调Activity的onContentChanged方法通知Activity视图发生改变
由于Activity实现了Window的Callback接口，只需回调Activity的onContentChanged方法告知Activity的布局文件已经被添加到DecorView的mContentParent中。
```
final Callback cb = getCallback();
if (cb != null && !isDestroyed()) {
    cb.onContentChanged();
}
```
经过上面三个步骤，DecorView已经被创建并初始化完成，Activity的布局文件也成功添加到DecorView的mContentParent中，但此时DecorView还未被WindowManager正式添加到Window中。在ActivityThread的handleResumeActivity方法中，首先会调用Activity的onResume方法，接着会调用Activity的makeVisible()方法，此方法真正执行DecorView的添加和显示，此时Activity的视图才能被用户看到。
```
void makeVisible() {
    if (!mWindowAdded) {
        ViewManager wm = getWindowManager();
        wm.addView(mDecor, getWindow().getAttributes());
        mWindowAdded = true;
    }
    mDecor.setVisibility(View.VISIBLE);
}
```

### Dialog的Window创建过程
Dialog的Window创建过程和Activity类似，有如下几个步骤。
1. 创建Window
Dialog中Window的创建同样是通过PolicyManager的makeNewWindow方法完成的，创建后的对象实际上是PhoneWindow。
```
Dialog(Context context, int theme, boolean createContextThemeWrapper) {
    mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Window w = PolicyManager.makeNewWindow(mContext);
    mWindow = w;
    w.setCallback(this);
    w.setOnWindowDismissedCallback(this);
    w.setWindowManager(mWindowManager, null, null);
    w.setGravity(Gravity.CENTER);
    mListenersHandler = new ListenersHandler(this);
}
```
2. 初始化DecorView并将Dialog的视图添加到DecorView中
这个过程也和Activity类似，都是通过Window去添加指定的布局文件。
```
public void setContentView(int layoutResId) {
    mWindow.setContentView(layoutResId);
}
```
3. 将DecorView添加到Window中并显示
在Dialog的show方法中，会通过WindowManager将DecorView添加到Window中。
```
mWindowManager.addView(mDecor, l);
mShowing = true;
```
当Dialog被关闭时，会通过WindowManager来移除DecorView：`mWindowManager.removeViewImmediate(mDecor);`

普通的Dialog有一个特殊之处，必须采用Activity的Context，如果采用Application的Context，会报错。
```
Dialog dialog = new Dialog(this.getApplicationContext());
TextView textView = new TextView(this);
textView.setText("this is toast!");
dialog.setContentView(textView);
dialog.show();
```
上述代码运行时报错如下:
```
2020-06-30 16:45:07.114 8104-8104/com.example.android_art_res_demo E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.example.android_art_res_demo, PID: 8104
    java.lang.RuntimeException: Unable to start activity ComponentInfo{com.example.android_art_res_demo/com.example.chapter_8.SystemWindowActivity}: android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid; is your activity running?
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3123)
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3258)
        at android.app.servertransaction.LaunchActivityItem.execute(LaunchActivityItem.java:78)
        at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:108)
        at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:68)
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1955)
        at android.os.Handler.dispatchMessage(Handler.java:106)
        at android.os.Looper.loop(Looper.java:193)
        at android.app.ActivityThread.main(ActivityThread.java:7029)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:537)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:858)
     Caused by: android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid; is your activity running?
        at android.view.ViewRootImpl.setView(ViewRootImpl.java:1045)
        at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:368)
        at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:94)
        at android.app.Dialog.show(Dialog.java:329)
        at com.example.chapter_8.SystemWindowActivity.initView(SystemWindowActivity.java:39)
        at com.example.chapter_8.SystemWindowActivity.requestPermission(SystemWindowActivity.java:29)
        at com.example.chapter_8.SystemWindowActivity.onCreate(SystemWindowActivity.java:20)
        at android.app.Activity.performCreate(Activity.java:7163)
        at android.app.Activity.performCreate(Activity.java:7154)
        at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1288)
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3097)
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3258) 
        at android.app.servertransaction.LaunchActivityItem.execute(LaunchActivityItem.java:78) 
        at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:108) 
        at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:68) 
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1955) 
        at android.os.Handler.dispatchMessage(Handler.java:106) 
        at android.os.Looper.loop(Looper.java:193) 
        at android.app.ActivityThread.main(ActivityThread.java:7029) 
        at java.lang.reflect.Method.invoke(Native Method) 
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:537) 
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:858) 
```
上面错误信息很明确，是没有应用token导致的，而应用token一般只有Activity拥有。另外系统级别Window比较特殊，可以不需要token，所以上面的例子只需指定Dialog的Window类型为系统类型即可。
比如`dialog.getWindow().setType(LayoutParams.TYPE_APPLICATION_OVERLAY);`  
系统级Dialog需要声明权限
```
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```
Android 6.0系统及以上需要动态申请权限
```
private void requestPermission() {
        //判断是否已经有权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //没有的话直接跳转权限申请页面
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, GET_DIALOG_PERMISSION);
        } else {
            showDialog();
        }
    }
```

### Toast的Window创建过程
Toast也是基于Window实现的，但由于Toast有定时取消功能，所以系统采用了Handler。在Toast内部有两类IPC过程，一类是Toast访问NotificationManagerService,第二类是NotificationManagerService回调Toast里的TN接口。  
Toast属于系统Window，内部视图由两种方式指定，一种是系统默认样式，另一种是通过setView方法来指定一个自定义View，两种都对应Toast的一个View类型的内部成员mNextView。Toast提供了show和cancel分别用于显示和隐藏Toast，它的内部是一个IPC过程，show和cancel实现如下 ：
```
public void show() {
    if (mNextView == null) {
        throw new RuntimeException("setView must have been called");
    }
    
    INotificationManager service = getService();
    String pkg = mContext.getOpPackageName();
    TN tn = mTN;
    tn.mNextView = mNextView;
    
    try {
        service.enqueueToast(pkg, tn, mDuration);
    } catch (RemoteException e) {
        
    }
}

public void cancel() {
    mTN.hide();
    
    try {
        getService().cancelToast(mContext.getPackageName(), mTN);
    } catch (RemoteException e) {
        
    }
}
```
显示和隐藏Toast都需要通过NMS来实现，由于NMS运行在系统进程，所以只能通过远程调用的方式来实现。TN是一个binder类，在Toast和NMS进行IPC的过程中，当NMS处理Toast的显示或隐藏请求时会跨进程回调TN中的方法，这个时候由于TN运行在Binder线程池中，所以需要Handler将其切换到当前线程。注意，由于这里使用了Handler，所以Toast无法在没有Looper的线程中弹出。
enqueueToast首先将Toast请求封装为ToastRecord对象并将其添加到一个名为mToastQueue的队列中。mToastQueue其实是一个ArrayList，对于非系统应用，mToastQueue最多同时存在50个ToastRecord，这样是为了防止DOC(拒绝服务攻击)。
```
if (!isSystemToast) {
    int count = 0;
    final int N = mToastQueue.size();
    for (int i = 0; i < N; i++) {
        final ToastRecord r = mToastQueue.get(i);
        if (r.pkg.equals(pkg)) {
            count ++;
            if (count >= MAX_PACKAGE_NOTIFICATIONS) {
                Slog.e(TAG, "Package has already posted " + count  + " toasts. Not showing more. Package = " + pkg);
                return;
            }
        }
    }
}
```
当ToastRecord被添加到mToastQueue中后，NMS就会通过showNextToastLocked方法来显示当前的Toast。Toast的显示是由ToastRecord的callback来完成的，这个callback实际是Toast中的TN对象的远程binder。
```
void showNextToastLocked()  {
    ToastRecord record = mToastQueue.get(0);
    while(record != null) {
        if (DBG) Slog.d(TAG, "Show pkg = " + record.pkg + "callback = " + record.callback);
        try {
            record.callback.show();
            scheduleTimeoutLocked(record);
            return;
        } catch (RemoteException e) {
            Slog.w(TAG, "Object died trying to show notification " + record.callback + "in package " + record.pkg);
            //remove it from the list and let the process die
            int index = mToastQueue.indexOf(record);
            if (index >= 0) {
                mToastQueue.remove(index);
            }
            keepProcessAliveLocked(record.pid);
            if (mToastQueue.size() > 0) {
                record = mToastQueue.get(0);
            } else {
                record = null;
            }
        }
    }
}
```
Toast显示以后，NMS还会通过scheduleTimeoutLocked方法来发送延时消息，具体取决于Toast的时长。
```
private void scheduleTimeoutLocked(ToastRecord r) {
    mHandler.removeCallbacksAndMessages(r);
    Message m = Message.obtain(mHandler, MESSAGE_TIMEOUT, r);
    long delay = r.duration == Toast.LENGTH_LONG ? LONG_DELAY : SHORT_DELAY;
    mHandler.sendMessageDelayed(m, delay);
}
```
在上面的代码中，LONG_DELAY是3.5s，而SHORT_DELAY是2s。延迟相应的时间后，NMS会通过cancelToastLocked方法来隐藏Toast并将其从mToastQueue中移除，这个时候如果mToastQueue中还有其他Toast，那么NMS就继续显示其他Toast。  
Toast的隐藏也是通过ToastRecord的callback来完成的，这同样也是一次IPC过程，它的工作方式和Toast显示过程类似。
```
try {
    record.callback.hide();
} catch (RemoteException e)  {
    
}
```
Toast的显示和隐藏实际上是通过Toast的TN类实现的，它有show和hide方法，分别对应Toast的显示和隐藏。
```
@Override
public void show() {
    if (localLOGV) Log.v(TAG, "SHOW: " + this);
    mHandler.post(mShow);
}

@Override
public void hide() {
    if (localLOGV) Log.v(TAG, "HIDE: " + this);
    mHandler.post(mHide);
}
```
mShow和mHide是两个Runnable，内部分别调用了handleShow和handleHide方法。内部通过WindowManager将Toast的视图添加和移除Window。
```
mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
mWM.addView(mView, mParams);

if (mView.getParent() != null) {
    if (localLOGV) Log.v(TAG, "REMOVE! " + mView + " in " + this);
    mWM.removeView(mView);
}
```
