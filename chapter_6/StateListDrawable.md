StateListDrawable对应于<selector>标签，它也是表示Drawable的集合。每个Drawable对应View的一种状态，这样系统就会根据View的状态来选择合适的Drawable。StateListDrawable主要用于设置可单击的View的背景，最常见的是Button。语法如下：
```
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android"
    //StateListDrawable的固有大小是否不随着状态的改变而改变
    //true表示保持不变，即为内部所有Drawable的固有大小的最大值
    //false表示跟随状态改变而改变，默认为false
    android:constantSize=["true"|"false"]
    //是否开启抖动效果，默认开启
    android:dither=["true"|"false"]
    //StateListDrawable的padding是否随着状态改变而改变
    //true表示跟随状态改变
    //false是内部所有Drawable的padding的最大值，默认为false
    android:variablePadding=["true"|"false"]
    <item
        android:drawable="@[package:]drawable/drawable_resource"
        android:state_pressed=["true"|"false"]
        android:state_focused=["true"|"false"]
        android:state_hovered=["true"|"false"]
        android:state_selected=["true"|"false"]
        android:state_checkable=["true"|"false"]
        android:state_checked=["true"|"false"]
        android:state_enabled=["true"|"false"]
        android:state_activated=["true"|"false"]
        android:state_window_focused=["true"|"false"]
</selector>
```

#### selector常见状态及含义
状态|含义
--|--
android:state_pressed|表示按下状态，比如Button被按下后仍没有松开时的状态
android:state_focused|表示View已经获取了焦点
android:state_selected|表示用户选择了View
android:state_checked|表示用户选中了View，一般适用于CheckBox这类在选中和非选中状态之间进行切换的View
android:state_enabled|表示View当前处于可用状态

#### 示例
```
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- pressed -->
    <item android:state_pressed="true"
        android:drawable="@drawable/button_pressed" />
    
    <!-- focused -->
    <item android:state_focused="true"
        android:drawable="@drawable/button_focused" />
        
    <!-- default -->
    <item android:drawable="@drawable/button_normal" />
```
系统会根据View当前的状态从selector中选择对应的item，每个item对应一个具体的Drawable，系统按照从上到下的顺序查找，直到查找到第一条匹配的item。一般来说，默认的item都应该放在selector的最后并且不附带任何状态，这样当上面的item都不匹配时，就会选择默认的item。因为默认的item不附带状态，就可以匹配任何状态。