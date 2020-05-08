## android:launchMode指定启动模式
1. standard    
每次启动都会产生一个新的Activity实例，多次启动会产生多个实例。  
在这种模式下，谁启动了这个Activity，那么这个Activity就运行在启动它的那个Activity所在的栈中。比如Activity A启动了Activity B,那么B就会进入到A所在的栈中。  
如果使用ApplicationContext直接启动standard模式的Activity，会报错。这时需要为待启动的Activity指定FLAG_ACTIVITY_NEW_TASK标记位，这样启动的时候就会为它创建一个新的任务栈，这个时候待启动的Activity实际上是以singleTask模式启动的。

2. singleTop  
栈顶复用模式。如果已经启动的Activity已经在栈顶，就不产生新实例，同时它的onNewIntent方法会回调。但这个Activity的onCreate、onStart不会被系统调用。但如果该Activity不是在栈顶，那么新Activity还是会重建。

3. singleTask
栈内复用模式。这是一种单实例模式，在这种模式下，只要Activity在一个栈中存在，那么多次启动此Activity都不会重新创建实例。和singleTop一样，系统也会回调其onNewIntent。

当一个具有singleTask模式的Activity A请求启动后，系统首先会寻找是否存在A想要的任务栈，如果不存在，就重新创建一个任务栈，然后创建A的实例后把A放入栈中。如果存在A所需的任务栈，这时要看A是否在栈中有实例存在。如果不存在实例，就创建A的实例并压入栈中。如果存在A的实例，并且A不在栈顶，就将栈内A上的实例出栈，使A位于栈顶，并调用onNewIntent方法。

**注意**  
&emsp;&emsp;如果启动的Activity存在后台一个任务栈，则启动该Activity实例，将此任务栈切换到前台。此时，返回键返回，也会先Activity实例栈的下一个Activity。 

4. singleInstance  
单实例模式。这是一种加强的singleTask模式，它除了具有singleTask模式的所有特性为，还加强了一点，那就是具有此种模式的Activity只能单独地位于一个任务栈中。为此Activity实例单独建立一个Activity栈，栈内不能有其他Activity实例。比如用来做QQ分享的跳转页面。

**注意：**  
&emsp;&emsp;如果在一个singleTop或者singleInstance的ActivityA中通过startActivityForResult()方法启动另一个ActivityB,则系统直接返回Activity.RESULT.CANCELED而不会等待返回该ActivityA。
&emsp;&emsp;这是由于Framework限制了这两种启动模式，因为Android开发者认为，不同Task之间，默认不能传递数据。如果一定要传递，就通过Intent来绑定数据。

## 指定Activity的任务栈
```
<activity
    android:name="com.ryg.chapter_1.SecondActivity"
    android:configChanges="screenLayout"
    android:label="@string/app_name"
    android:launchMode="standard"
    android:taskAffinity="com.ryg.task1" />
```
taskAffinity参数标识了Activiy所需要的任务栈的名字，默认情况下，所有Activity所需的任务栈为应用的包名。默认Activity的taskAffinity值继承自Application的taskAffinity，而Application默认taskAffinity为包名。  
当然，我们可以为每个Activity都单独指定taskAffinity属性，但不能和包名相同，否则就相当于没有指定。taskAffinity属性主要和singleTask启动模式或者allowTaskReparenting属性配对使用，在其他情况下没有意义。  
另外任务栈分为前台任务栈和后台任务栈，后台任务栈中的Activity位于暂停状态，用户可以通过切换将后台任务栈再次调到前台。

当taskAffinity和allowTaskReparenting结合的时候，情况比较复杂。当一个应用A启动了应用B的某个Activity后，如果这个Activity的allowTaskReparenting属性为true的话，那么当应用B被启动后，此Activity会直接从应用A的任务栈转移到应用B的任务栈中。
  
比如现有两个应用A和B，A启动了B的一个Activity C，然后按Home回到桌面，然后再单击B的桌面图标，这个时候并不是启动了B的主Activity，而是重新显示了已经被应用A启动的Activity C，或者说，C从A的任务栈转移到了B的任务栈中。

## Intent Flag指定启动模式  
1. Intent.FLAG_ACTIVITY_NEW_TASK  
与android:launchMode="singleTask"效果相同。  
使用一个新的Task来启动一个Activity，但启动的每个Activity都在一个新的Task中。通常用于在Service中启动Activity，因为Service中并不存在Activity栈，所以使用该Flag来创建一个新的Activity栈，并创建Activity的实例。
2. FLAG_ACTIVITY_SINGLE_TOP  
与android:launchMode="singleTop"效果相同。
3. FLAG_ACTIVITY_CLEAR_TOP  
具有此标记位的Activity，当它启动时，在同一个任务栈中，所有位于它上面的Activity都要出栈。一般需要和FLAG_ACTIVITY_NEW_TASK配合使用。如果被启动的Activity采用standard模式启动，那么它连同它之上的Activity都要出栈，系统会创建新的Activity实例并放入栈顶。
4. FLAG_ACTIVITY_NO_HISTORY  
使用这种模式启动Activity，当该Activity启动其他Activity后，该Activity就消失了，不会保留在Activity栈中。比如A以这种模式启动B，B再启动C，则当前Activity栈为A-C。
5. FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
具有这个标记的Activity不会出现在历史Activity的列表中，当某些情况下我们不希望用户通过历史列表回到我们的Activity的时候这个标记比较有用。它等同于xml中指定activity的属性`android:excludeFromRecents="true"`

- AndroidMenifest与Flag设置启动模式的区别

1. 通过Intent设置的启动模式优先级高于AndroidMenifest中指定的启动模式。
2. AndroidMenifest无法直接为Activity设定FLAG_ACTIVITY_CLEAR_TOP标识
3. Flag无法为Activity指定singleInstance模式

## 清空任务栈  
可以在AndroidManifest文件中的<activity>标签使用以下几种属性来清理任务栈。  
- clearTaskOnLaunch     
每次启动该Activity实例时，将该Activity任务栈中，此Activity实例之上的Activity都清除。
- finishOnTashLaunch  
启动该一个其他Activity栈的Activity实例，再按返回键，此Activity就会被清理。   
一个应用场景如下：启动分享Activity，点击QQ分享跳转到QQ的分享Activity，分享完成后点击返回键，将此应用的分享Activity也自动退出。
- alwaysRetainTaskState
如果设置alwaysRetainTaskState="true",则该Activity所在的Activity栈将不接收任何清理命令，一直保持当前Task状态。避免退到后台过久，Activity被清理。