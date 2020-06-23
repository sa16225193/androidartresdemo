ClipDrawable对应于<clip>标签，它可以根据自己当前的等级（level）来裁剪另一个Drawable，裁剪方向可以通过android:clipOrientation和android:gravity这两个属性来共同控制。语法如下：
```
<?xml version="1.0" encoding="utf-8"?>
<clip
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/drawable_resource"
    //表示裁剪方向，有水平和竖直两个方向
    android:clipOrientation=["horizontal"|"vertical"]
    android:gravity=["top"|"bottom"|"left"|"right"|"center_vertical"|"fill_vertical"|"center_horizontal"|"fill_horizontal"|"center"|"fill"|"clip_vertical"|"clip_horizontal"] />
```

#### ClipDrawable的gravity属性
选项|含义
--|--
top|将内部的Drawable放在容器顶部，不改变大小。如果为竖直裁剪，那么从底部开始裁剪
bottom|将内部的Drawable放在容器底部，不改变大小。如果为竖直裁剪，那么从顶部开始裁剪
left|将内部的Drawable放在容器左边，不改变大小。如果为水平裁剪，那么从右边开始裁剪（默认值）
right|将内部的Drawable放在容器右边，不改变大小。如果为水平裁剪，那么从左边开始裁剪
center_vertical|将内部的Drawable在容器中竖直居中，不改变大小。如果为竖直裁剪，那么从上下同时开始裁剪。
fill_vertical|使内部的Drawable在竖直方向上填充容器。如果为竖直裁剪，那么仅当ClipDrawable等级为0时，才能有裁剪行为
center_horizontal|参考center_vertical
fill_horizontal|参考fill_vertical
center|使内部的Drawable在水平和竖直方向上同时填充容器。仅当ClipDrawable的等级为0时，才能有裁剪行为。
fill|使内部的Drawable在水平和竖直方向上同时填充容器。仅当ClipDrawable的等级为0时，才能有裁剪行为
clip_vertical|附加选项，表示竖直方向的裁剪，较少使用
clip_horizontal|附加选项，表示水平方向的裁剪，较少使用

#### 示例
1. 定义ClipDrawable
```
<?xml version="1.0" encoding="utf-8"?>
<clip xmlns:android="http://schemas.android.com/apk/res/android"
    android:clipOrientation="vertical"
    android:drawable="@drawable/image1"
    android:gravity="bottom" />

```
2. 设置为View的背景
```
<ImageView
    android:id="@+id/test_clip"
    android:layout_width="100dp"
    android:layout_height="100dp"
    android:gravity="center"
    android:src="@drawable/clip_drawable" />
```

3. 代码中设置ClipDrawable的等级
```
ImageView testClip = (ImageView) findViewById(R.id.test_clip);
ClipDrawable testClipDrawable = (ClipDrawable) testClip.getDrawable();
testClipDrawable.setLevel(8000);
```

Drawable的level是有范围的，即0~10000。对于ClipDrawable来说，等级0表示完全裁剪，即整个Drawable都不可见了，而等级10000表示不裁剪。在上面的代码中将等级设置为8000，表示裁剪了2000，即在顶部裁剪掉20%的区域。