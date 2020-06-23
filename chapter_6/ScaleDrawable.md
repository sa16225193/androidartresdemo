ScaleDrawable对应于<scale>标签，它可以根据自己的等级（level）将指定的Drawable缩放到一定比例。语法如下：
```
<?xml version="1.0" encoding="utf-8"?>
<scale
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/drawable_resource"
    android:scaleGravity=["top"|"bottom"|"left"|"right"|"center_certical"|"fill_vertical"|"center_horizontal"|"fill_horizontal"|"center"|"fill"|"clip_vertical"|"clip_horizontal"]
    android:scaleHeight="percentage"
    android:scaleWidth="percentage" />
```
在上面的属性中，scaleGravity等同于shape中的gravity。而scaleWidth和scaleHeight分别表示指定Drawable宽和高的缩放比例，以百分比的形式表示，比如25%。  
ScaleDrawable的等级默认为0，此时ScaleDrawable不可见。要想可见，需要修改level。从ScaleDrawable的draw方法可以看出：
```
public void draw(Canvas canvas) {
    if (mScaleState.mDrawable.getLevel() != 0) 
        mScaleState.mDrawable.draw(canvas);
}
```
ScaleDrawable的级别越大，那么内部的Drawable就越大。另外，从ScaleDrawable的内部实现来看，ScaleDrawable的作用更偏向于缩小一个特点的Drawable。

#### 示例
将一张图片缩小为原大小的30%
```
<?xml version="1.0" encoding="utf-8"?>
<scale xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/image1"
    android:scaleHeight="70%"
    android:scaleWidth="70%"
    android:scaleGravity="center" />
```

设置ScaleDrawable的等级大于0且小于10000
```
View testScale = findViewById(R.id.test_scale);
ScaleDrawable testScaleDrawable = (ScaleDrawable) testScale.getBackground();
testScaleDrawable.setLevel(10);
```