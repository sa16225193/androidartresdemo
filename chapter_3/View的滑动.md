## 常见的View滑动方式
1. 使用scrollTo/scrollBy  
在滑动过程中，mScrollX的值总是等于View左边缘和View内容左边缘在水平方向的距离，而mScrollY的值总是等于View上边缘和View内容上边缘在竖直方向的距离。View边缘是指View的位置，由四个顶点组成，而View内容编译是指View中的内容的边缘，scrollTo和scrollBy只能改变View内容的位置，而不能改变View在布局中的位置。

2. 使用动画
使用动画来移动View，主要是操作View的translationX和translationY属性，即可以采用传统的View动画，也可以采用属性动画。

3. 改变布局参数
改变布局参数，即改变LayoutParams。
```
MargingLayoutParams params = (MarginLayoutParams)mButton1.getLayoutParams();
params.width += 100;
params.leftMargin += 100;
mButton1.requestLayout();//或者mButton1.setLayoutParams(params);
```

## 渐进式性滑动
实现渐进式滑动的方法很多，但有一个共同思想：将一次大的滑动分成若干次小的滑动并在一段时间内完成。具体实现方式比如通过Scroller、Hander#postDelayed以及Thread#sleep等。

1. 使用Scroller
```
Scroller scroller = new Scroller(mContext);

//缓慢滚动到指定位置
private void smoothScrollTo(int dextX, int destY) {
    int scrollX = getScrollX();
    int deltaX = destX - scrollX;
    //1000ms内滑向destX,效果就是慢慢滑动
    mScroller.startScroll(scrollX, 0, deltaX, 0, 1000);
    invalidate();
}

@Override
public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
        scrollTo(mScroller.getCurrx(), mScroller.getCurrY());
        postInvalidate();
    }
}
```

2. 通过动画
动画本身就是一种渐进的过程，因此通过它来实现的滑动天然就具有渐进式效果。  
下面是利用动画的特效模仿Scroller来实现View的渐进式滑动。
```
final int startX = 0;
final int deltaX = 100;
ValueAnimator animator = ValueAnimator.ofInt(0, 1).setDuration(1000);
animator.addUpdateListener(new AnimatorUpdateListener() {
    @Override
    public void onAnimationUpdate(ValueAnimator animator) {
        float fraction = animator.getAnimatedFraction();
        mButton1.scrollTo(startX + (int) (deltaX * fraction), 0);
    }
});
animator.start();
```

3. 使用延时策略
通过发送一系列延时消息从而达到一种渐进式效果，具体来说可以使用Handler或View的postDelayed方法，也可以使用线程的sleep方法。对于postDelayed方法来说，我们可以通过它来延时发送一个消息，然后在消息中进行View的滑动。对于sleep方法来说，通过在while循环中不断地滑动View和sleep，就可以实现渐进式滑动的效果。  
下面是采用Handler来做个示例。
```
private static final int MESSAGE_SCROLL_TO = 1;
private static final int FRAME_COUNT = 30;
private static final int DELAYED_TIME = 33;

private int mCount = 0;

@SuppressLint("HandlerLeak")
private Handler mHandler = new Handler() {
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_SCROLL_TO: {
                mCount++;
                if (mCount <= FRAME_COUNT) {
                    float fraction = mCount / (float) FRAME_COUNT;
                    int scrollX = (int) (fraction * 100);
                    mButton1.scrollTo(scrollX, 0);
                    mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLL_TO, DELAYED_TIME);
                }
                break;
            }
            default:
                break;
        }
    }
};
```
