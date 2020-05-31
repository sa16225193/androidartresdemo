## View的位置参数
View的位置主要由四个顶点决定，分别对应View的四个属性：top、left、right、bottom，其中top是左上角纵坐标，left是左上角横坐标，right是右下角横坐标，bottom是右下角纵坐标。这些坐标都是相对于View的父容器来说的。
![image](http://note.youdao.com/yws/res/33297/5BA59B82328348FB991D0AEC1547C0E7)

View的宽高和坐标的关系
width = right - left
height = bottom - top

获取View的坐标
- Left = getLeft()
- Right = getRight()
- Top = getTop()
- Bottom = getBottom()

Android 3.0开始，View增加了x、y、translationX和translationY。其中x和y是View左上角的坐标，而translationX和translationY是View左上角相对于父容器的偏移量，默认为0。这几个参数也可以直接通过对应get方法获取。  
x = left + translationX  
y = top + translationY  
View在平移的过程中，top + left表示原始左上角的位置信息，其值不会改变，改变的是x、y、translationX和translationY四个参数。

## MotionEvent和TouchSlop
#### MotionEvent
在手指接触屏幕后所产生的一系列事件，典型事件类型有如下几种：
- ACTION_DOWN——手指刚接触屏幕
- ACTION_MOVE——手指在屏幕上移动
- ACTION_UP——手指从屏幕上松开的一瞬间

典型事件序列
- 点击 DOWN -> UP
- 滑动屏幕 DOWN -> MOVE -> ... -> MOVE -> UP

MotionEvent对象可以得到事件发生的x和y坐标。
- getX      相对于当前View左上角的x
- getY      相对于当前View左上角的y
- getRawX   相对于手机屏幕左上角的x
- getRawY   相对于手机屏幕左上角的y

#### TouchSlop
TouchSlop是系统所能识别出的被认为是滑动的最小距离，换句话说，当手指在屏幕上滑动时，如果两次滑动之间的距离小于这个常量，那么系统就不认为你是在进行滑动操作。TouchSlop常量和设备有关，不同设备可能值不同，通过`ViewConfiguration.get(getContext()).getScaledTouchSlop()`获取。  
源码中可以找到这个常量的定义，在frameworks/base/core/res/res/values/config.xml文件中。
```
<dimen name="config_viewConfigurationTouchSlop">8dp</dimen>
```

## VelocityTracker、GestureDetector和Scroller
#### VelocityTracker
速度追踪，用于追踪手指在滑动过程中的速度，包括水平和竖直方向的速度。它的使用过程很简单，首先，在View的onTouchEvent方法中追踪当前事件的速度：  
```
VelocityTracker velocityTracker = VelocityTracker.obtain();
velocityTracker.addMovement(event);

velocityTracker.computeCurrentVelocity(1000);//参数是时间间隔，单位为ms
int xVelocity = (int) velocityTracker.getXVelocity();
int yVelocity = (int) velocityTracker.getYVelocity();

//重置并回收
velocityTracker.clear();
velocityTracker.recycle();
```
速度 = （终点坐标 - 起点坐标）/ 时间段  
速度可以为负值，即逆坐标轴方向滑动。

#### GestureDetector
手势检测，用于辅助检测用户的单击、滑动、长按、双击等行为。使用过程如下：
```
//1. 创建一个GestureDetector对象并实现OnGestureListener接口
//根据需要还可以实现OnDoubleTapListener接口
GestureDetector mGestureDetector = new GestureDetector(this);
//解决长按屏幕后无法拖动的现象
mGestureDetector.setIsLongpressEnabled(false);

//2. 在目标View的onTouchEvent方法中添加如下实现
boolean consume = mGestureDetector.onTouchEvent(event);
return consume;
```

- OnGestureListener方法介绍  

方法名|描述
--|--
onDown|手指轻轻触摸屏幕的一瞬间，由1个ACTION_DOWN触发
onShowPress|手指轻轻触摸屏幕，尚未松开或拖动，由1个ACTION_DOWN触发。和onDown()的区别，它强调的是没有松开或者拖动的状态
onSingleTapUp|手指轻轻触摸屏幕后松开，伴随着1个MotionEvent.ACTION_UP而触发，这是单击行为
onScroll|手指按下屏幕并拖动，由1个ACTION_DOWN,多个ACTION_MOVE触发，这是拖动行为
onLongPress|用户长按
onFling|用户按下屏幕，快速滑动后松开，由1个ACTION_DOWN、多个ACTION_MOVE和1个ACTION_UP触发

- OnDoubleTapListener方法介绍  

方法名|描述
--|--
onDoubleTap|双击，由连续2次单击组成，不可能和onSingleTapConfirmed共存
onSingleTapConfirmed|严格单击行为。和onSingleTap的区别，如果触发了onSingleTapConfirmed，后面不可能再紧跟着另一个单击行为，即不可能是双击
onDoubleTapEvent|表示发送了双击行为，在双击期间，ACTION_DOWN、ACTION_MOVE和ACTION_UP都会触发此回调

比较常用的方法：onSingleTapUp（单击）、onFling（快速滑动）、onScroll（拖动）、onLongPress（长按）和onDoubleTap（双击）。实际开发中，可以不使用GestureDetector，完全可以自己在View的onTouchEvent方法中实现所需的监听。如果是监听双击行为，建议使用GestureDetector。

#### Scroller
弹性滑动对象，用于实现View的弹性滑动。当使用View的scrollTo、scrollBy方法滑动时，其过程是瞬间完成的，这个没有过渡效果的滑动用户体验不好。Scroller可以结合View的computeScroll方法实现有过渡效果的滑动。
```
Scroller scroller = new Scroller(mContext);

//缓慢滚动到指定位置
private void smoothScrollTo(int destX, int destY) {
    int scrollX = getScrollX();
    int delta = destX - scrollX;
    //1000ms内滑向destX，效果就是慢慢滑动
    mScroller.startScroll(scrollX, 0, delta, 0, 1000);
    invalidate();
}

@Override
public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
        scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
        postInvalidate();
    }
}
```
