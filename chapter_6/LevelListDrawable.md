LevelListDrawable对应于<level-list>标签，它同样表示一个Drawable集合，集合中的每个Drawable都有一个等级（level）的概念。根据不同的等级，LevelListDrawable会切换为对应的Drawable。语法如下：
```
<?xml version="1.0" encoding="utf-8"?>
<level-list
    xmlns:android="http://schemas.android.com/apk/res/android" >
    <item
        android:drawable="@drawable/drawable_resource"
        android:maxLevel="integer"
        android:minLevel="integer" />
</level-list>
```
上面的语法中，每个item表示一个Drawable，并且有对应的等级范围，由android:minLevel和android:maxLevel来指定，在最大值和最小值之间的等级会对应此item中的Drawable。  Drawable的等级是有范围的，即0~10000.
当Drawable作为View的背景时，可以通过Drawable的setLevel方法来设置不同的等级从而切换具体的Drawable。如果作为ImageView的前景Drawable，那么还可以通过ImageView的setImageLevel方法来切换Drawable。

#### 示例
```
<?xml version="1.0" encoding="utf-8"?>
<level-list xmlns:android="http://schemas.android.com/apk/res/android" >
    <item 
        android:drawable="@drawable/status_off"
        android:maxLevel="0" />
    
    <item
        android:drawable="@drawable/status_on"
        android:maxLevel="1" />
</level-list>
```