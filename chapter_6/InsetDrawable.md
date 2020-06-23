InsetDrawable对应于<inset>标签，它可以将其他Drawable内嵌到自己当中，并可以在四周留出一定的间距。当一个View希望自己的背景比自己的实际区域小的时候，可以采用InsetDrawable来实现。同时，也可以通过LayerDrawable来实现。语法如下：
```
<?xml version="1.0" encoding="utf-8"?>
<inset
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/drawable_resource"
    android:insetTop="dimension"
    android:insetRight="dimension"
    android:insetBottom="dimension"
    android:insetLeft="dimension" />
```
insetTop、insetBottom、insetLeft、insetRight分别表示顶部、底部、左边和右边内凹的大小。在下面的例子中，inset中的shape距离View的边界为15dp。
```
<?xml version="1.0" encoding="utf-8"?>
<inset xmlns:android="http://schemas.android.com/apk/res/android"
    android:insetBottom="15dp"
    android:insetLeft="15dp"
    android:insetRight="15dp"
    android:insetTop="15dp" >
    
    <shape android:shape="rectangle" >
        <solid android:color="#ff0000" />
    </shape>
</inset>
```