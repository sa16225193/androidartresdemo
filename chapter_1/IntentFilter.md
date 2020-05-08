启动Activity分为两种，显式调用和隐式调用。如果二者共存的话，以显式调用为主。

为了匹配过滤列表，需要同时匹配过滤列表中的action、category和data信息，否则匹配失败。一个过滤列表中的action、category和data可以有多个，所有的action、category、data分别构成不同类别，同一类别的信息共同约束当前类别的匹配过程。只有一个Intent同时匹配action类别、category类别、data类别才是完全匹配，只有完全匹配才能成功启动目标Activity。  
另外，一个Activity可以有多个intent-filter，一个Intent只要能匹配任何一组intent-filter，即可成功启动对应的Activity。
```
<activity
            android:name="com.ryg.chapter_1.ThirdActivity"
            android:configChanges="screenLayout"
            android:label="@string/app_name"
            android:launchMode="singleTask"
            android:taskAffinity="com.ryg.task1" >
            <intent-filter>
                <action android:name="com.ryg.charpter_1.c" />
                <action android:name="com.ryg.charpter_1.d" />

                <category android:name="com.ryg.category.c" />
                <category android:name="com.ryg.category.d" />
                <category android:name="android.intent.category.DEFAULT" />

                <data android:mimeType="text/plain" />
            </intent-filter>
        </activity>
```

## action的匹配规则
1. action是一个字符串，系统预定义了一些action，同时我们也可以自定义action。
2. 一个Intent-filter可以有多个action，只要匹配一个action即可
3. action区分大小写
4. 如果没有指定action，那么总匹配失败

## category的匹配规则
1. category是一个字符串，系统预定义了一些category，也可以自定义category
2. Intent中如果含有category，那么所有的category都必须和intent-filter中的一个category相同。
3. Intent可以不指定category，系统在调用startActivity或startActivityForResult时会默认加上android.intent.category.DEFAULT
4. 为了我们的activity能接收隐式调用，就必须在intent-filter中指定"android.intent.category.DEFAULT"


## data的匹配规则
```
<data android:scheme="string"
    android:host="string"
    android:port="string"
    android:path="string"
    android:pathPattern="string"
    android:pathPrefix="string"
    android:mimeType="string"/>
    
Intent intent = new Intent("com.ryg.charpter_1.c");
intent.addCategory("com.ryg.category.c");
intent.setDataAndType(Uri.parse("file://abc"), "text/plain");
startActivity(intent);
```
data由两部分组成，mimeType和URI。mimeType指媒体类型，比如image/jpeg和video/*。  

data的匹配规则和action类似，它也要求Intent中必须含有data数据，并且data数据能够完全匹配intent-filter中的某一个data。  
另外，如果要为Intent指定完整的data，必须调用setDataAndType方法，不能先调用setData，再调用setType，因为这两个方法会彼此清除对方的值。  
URI有默认值为content或file

- 可以通过PackageManager.resolveActivity或Intent.resolveActivity先看是否有匹配的Activity，没有会返回null。另外PackageManager.queryIntentActivities可以查询所有成功匹配的Activity信息。