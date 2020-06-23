TransitionDrawable对应于<transition>标签，它用于实现两个Drawable之间的淡入淡出效果。语法如下：
```
<?xml version="1.0" encoding="utf-8"?>
<transition xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:drawable="@[package:]drawable/drawable_resource"
        android:id="@[+][package:]id/resource_name"
        android:top="dimension"
        android:right="dimension"
        android:bottom="dimension"
        android:left="dimension" />
</transition>
```

#### 示例
1. 首先定义TransitionDrawable
```
<?xml version="1.0" encoding="utf-8"?>
<transition xmlns:android="http://schemas.android.com/apk/res/android" >

    <item android:drawable="@drawable/shape_drawable_gradient_linear"/>
    <item android:drawable="@drawable/shape_drawable_gradient_radius"/>

</transition>
```

2. 接着将上面的TransitionDrawable设置为View的背景
```
 <TextView
    android:id="@+id/test_transition"
    android:layout_width="100dp"
    android:layout_height="100dp"
    android:background="@drawable/transition_drawable"
    android:gravity="center"
    android:text="TransitionDrawable" />
```

3. 最后通过startTransition和reverseTransition方法来实现淡入淡出的效果。
```
View v = findViewById(R.id.test_transition);
TransitionDrawable drawable = (TransitionDrawable) v.getBackground();
drawable.startTransition(1000);
```