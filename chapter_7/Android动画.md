Android动画可以分为三种：View动画、帧动画和属性动画，其实帧动画也属于View动画。

## View动画
View动画有四种效果:  
- 平移——TranslateAnimation
- 缩放——ScaleAnimation
- 旋转——RotateAnimation
- 透明度——AlphaAnimation

View动画即可以通过代码创建，也可以通过XML文件定义。建议采用XML
来定义，可读性更好。

#### View动画的四种变换
名称|标签|子类|效果
--|--|--|--
平移动画|<translate>|TranslateAnimation|移动View
缩放动画|<scale>|ScaleAnimation|放大或缩小View
旋转动画|<rotate>|RotateAnimation|旋转View
透明度动画|<alpha>|AlphaAnimation|改变View的透明度

要使用View动画，首先要创建动画的XML文件，路径为res/anim/filename.xml。View动画的语法：
```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    //插值器，影响动画速度。默认为@android:anim/accelerate_decelerate_interpolator，即加速减速插值器
    android:interpolator="@[package:]anim/interpolator_resource"
    //集合中的动画是否共享同一个插值器
    android:shareInterpolator=["true"|"false"] 
    //动画的持续时间，单位ms
    android:duration="integer"
    //动画结束后View是否停留在结束位置，true表示停留，false表示不停留
    android:fillAfter=["true"|"false"]>
    //透明度动画
    <alpha
        //透明度的起始值，比如0.1
        android:fromAlpha="float"
        //透明度的结束值,比如1
        android:toAlpha="float" />
    //缩放动画
    <scale
        //水平方向缩放的起始值，比如0.5
        android:fromXScale="float"
        //水平方向缩放的结束值，比如1.2
        android:toXScale="float"
        //竖直方向缩放的起始值
        android:fromYScale="float"
        //竖直方向缩放的结束值
        android:toYScale="float"
        //缩放的轴点的x坐标，默认情况轴点是View的中心点。如果把轴点设置为View的右边界，那么View只会向左边进行缩放。
        android:pivotX="float"
        //缩放的轴点的y坐标
        android:pivotY="float" />
    //平移动画
    <translate
        //表示x的起始值，比如0
        android:fromXDelta="float"
        //表示x的结束值，比如100
        android:toXDelta="float"
        //表示y的起始值
        android:fromYDelta="float"
        //表示y的结束值
        android:toYDelta="float" />
    //旋转动画
    <rotate
        //旋转开始的角度，比如0
        android:fromDegrees="float"
        //旋转结束的角度，比如180
        android:toDegrees="float"
        //旋转的轴点的x坐标
        android:pivotX="float"
        //旋转的轴点的y坐标
        android:pivotY="float" />
    <set>
    ...
    </set>
</set>
```
<set>标签表示动画集合，对应AnimationSet类，可以包含若干个动画，包括其他动画集合。

#### 使用XML的动画
```
Button mButton = findViewById(R.id.button1);
Animation animation = AnimationUtils.loadAnimation(this, R.anim.animation_test);
mButton.startAnimation(animation);
```

#### 直接在代码中使用View动画
```
AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
alphaAnimation.setDuration(300);
mButton.startAnimation(alphaAnimation);
```

### 自定义View动画
自定义View动画只需要继承Animation抽象类，重写initialize和applyTransformation方法。自定义View动画需要数学中的矩阵变换相关知识，一般较少使用。下面是实现绕y轴旋转同时沿着z轴平移的3D效果Rotate3dAnimation：
```
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by liuyong on 2020/6/28
 */
public class Rotate3dAnimation extends Animation {
    private final float mFromDegrees;
    private final float mToDegrees;
    private final float mCenterX;
    private final float mCenterY;
    private final float mDepthZ;
    private final boolean mReverse;
    private Camera mCamera;

    public Rotate3dAnimation(float fromDegrees, float toDegrees, float centerX, float centerY, float depthZ, boolean reverse) {
        this.mFromDegrees = fromDegrees;
        this.mToDegrees = toDegrees;
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        this.mDepthZ = depthZ;
        this.mReverse = reverse;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        final float fromDegrees = mFromDegrees;
        float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);
        
        final float centerX = mCenterX;
        final float centerY = mCenterY;
        final Camera camera = mCamera;
        final Matrix matrix = t.getMatrix();
        
        camera.save();
        if (mReverse) {
            camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
        } else {
            camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
        }
        camera.rotateY(degrees);
        camera.getMatrix(matrix);
        camera.restore();
        
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }
}

```

## 帧动画
帧动画是顺序播放一组预先定义好的图片，系统提供了AnimationDrawable类来使用帧动画。XML语法如下：
```
//res/drawable/frame_animation.xml文件路径
<?xml version="1.0" encoding="utf-8"?>
<animation-list xmlns:android="http://schemas.android.com/apk/res/android"
    android:oneshot="false">
    <item android:drawable="@drawable/image1" android:duration="500" />
    <item android:drawable="@drawable/image2" android:duration="500" />
    <item android:drawable="@drawable/image3" android:duration="500" />
</animation-list>
```

然后将上述的Drawable作为View的背景并通过Drawable来播放动画即可。
```
Button mButton = findViewById(R.id.button1);
mButton.setBackgroundResource(R.drawable.frame_animation);
AnimationDrawable drawable = (AnimationDrawable) mButton.getBackground();
drawable.start();
```

> 帧动画使用简单，但容易引起OOM，尽量避免使用。

## LayoutAnimation
LayoutAnimation作用于ViewGroup，可以为它的子元素指定出场效果，常使用在ListView上。

使用步骤：
1. 定义LayoutAnimation
```
//res/anim/anim_layout.xml
<layoutAnimation
    xmlns:android="http://schemas.android.com/apk/res/android"
    子元素开始动画的延迟时间，假如子元素入场动画时间周期为300ms，那么0.5表示延迟150ms
    android:delay="0.5"
    //子元素动画的顺序
    //normal顺序显示动画
    //reverse后面的子元素先执行动画
    //random随机播放入场动画
    android:animationOrder="normal"
    //为子元素指定具体的入场动画
    android:animation="@anim/anim_item"/>
```

2. 定义子元素的入场动画
```
//res/anim/anim_item.xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="300"
    android:interpolator="@android:anim/accelerate_interpolator"
    android:shareInterpolator="true" >
    
    <alpha
        android:fromAlpha="0.0"
        andorid:toAlpha="1.0" />
    
    <translate
        android:fromXDelta="500"
        android:toXDelta="0" />
</set>
```

3. 为ViewGroup指定android:layoutAnimation属性,这种方式适用于所有的ViewGroup
```
<ListView
    android:id="@+id/list"
    andorid:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layoutAnimation="@anim/anim_layout"
    android:background="#fff4f7f9"
    andorid:cacheColorHint="#00000000"
    android:divider="#dddbdb"
    andorid:dividerHeight="1.0px"
    android:listSelector="@android:color/transparent" />
```
除了在XML中指定LayoutAnimation外，还可以通过LayoutAnimationController来实现。
```
ListView listView = layout.findViewById(R.id.list);
Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_item);
LayoutAnimationController controller = new LayoutAnimationController(animation);
controller.setDelay(0.5f);
controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
listView.setLayoutAnimation(controller);
```
 
## Activity的切换效果   

Activity有默认的切换效果，但是这个效果我们是可以自定义的，主要用到overridePendingTransition(int enterAnim, int exitAnim)这个方法，这个方法必须在startActivity(Intent)或者finish()之后被调用才生效。

- enterAnim——Activity被打开时，所需的动画资源id
- exitAnim——Activity被暂停时，所需的动画资源id

当启动一个Activity时，可以按照如下方式添加自定义的切换效果：
```
Intent intent = new Intent(this, TestActivity.class);
startActivity(intent);
overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
```

当Activity退出时，也可以为其指定自己的切换效果
```
@Override
public void finish() {
    super.finish();
    overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
}
```
需要注意的是，overridePendingTransition方法必须位于startActivity或者finish之后，否则不起作用。

## 属性动画
属性动画从API11才有，之前版本可以使用nineoldandroids兼容（代理View动画实现，本质还是View动画），网址：http://nineoldandroids.com。

使用示例：
```
//改变一个对象（myObject）的translationY属性，让其沿Y轴向上平移一段距离
ObjectAnimator.ofFloat(myObject, "translationY", -myObject.getHeight()).start();

//改变一个对象的背景色，让背景色3秒内实现从0xFFFF8080到0xFF8080FF的渐变
ValueAnimator colorAnim = ObjectAnimator.ofInt(this, "backgroundColor", /*Red*/0xFFFF8080, /*Blue*/0xFF8080FF);
colorAnim.setDuration(3000);
colorAnim.setEvaluator(new ArgbEvaluator());
colorAnim.setRepeatCount(ValueAnimator.INFINITE);
colorAnim.setRepeatMode(ValueAnimator.REVERSE);
colorAnim.start();

//动画集合，5秒内对View进行旋转、平移、缩放和透明度变换
AnimatorSet set = new AnimatorSet();
set.playTogether(
    ObjectAnimator.ofFloat(myView, "rotationX", 0, 360),
    ObjectAnimator.ofFloat(myView, "rotationY", 0, 180),
    ObjectAnimator.ofFloat(myView, "rotation", 0, -90),
    ObjectAnimator.ofFloat(myView, "translationX", 0, 90),
    ObjectAnimator.ofFloat(myView, "translationY", 0, 90),
    ObjectAnimator.ofFloat(myView, "scaleX", 1, 1.5f),
    ObjectAnimator.ofFloat(myView, "scaleY", 1. 0.5f),
    ObjectAnimator.ofFloat(myView, "alpha", 1, 0.25f, 1)
);
set.setDuration(5000).start();
```

属性动画除了通过代码实现，还可以通过XML来定义，需要定义在res/animator/目录。语法如下：
```
<set
    android:ordering=["together"|"sequentially"]>
    
    <objectAnimator
        //属性动画作用对象的属性名称
        android:propertyName="string
        //属性动画的时长
        android:duration="int"
        //属性的起始值
        android:valueFrom="float|int|color"
        //属性的结束值
        android:valueTo="float|int|color"
        //动画的延迟时间，单位ms
        android:startOffset="int"
        //动画重复次数, -1表示无限循环
        android:repeatCount="int"
        //动画重复模式
        android:repeatMode=["repeat"|"reverse"]
        //属性的类型，如果属性表示的颜色，则不需要指定valueType
        android:valueType="[intType"|"floatType"] />
        
    <animator
        android:duration="int"
        android:valueFrom="float|int|color"
        android:valueTo="float|int|color"
        android:startOffset="int"
        android:repeatCount="int"
        android:repeatMode=["repeat"|"reverse"]
        android:valueType=["intType"|"floatType"] />
        
    <set>
        ...
    </set>
</set>
```
在XML中可以定义ValueAnimator、ObjectAnimator以及AnimatorSet，其中<set>标签对应AnimatorSet，<animator>标签对应ValueAnimator，而<objectAnimator>对应ObjectAnimator。<set>标签的android:ordering属性有两个可选值："together"和"sequentially",其中"together"表示动画集合中的子动画同时播放，"sequentially"表示动画集合中的子动画顺序依次播放。默认为"together"

使用示例：
1. 通过XML定义属性动画
```
//res/animator/property_animator.xml
<set android:ordering="together">
    <objectAnimator
        android:propertyName="x"
        android:duration="300"
        android:valueTo="200"
        android:valueType="intType" />
        
    <objectAnimator
        android:propertyName="y"
        android:duration="300"
        android:valueTo="300"
        android:valueType="intType" />
</set>
```
2. 使用XML属性动画
```
AnimatorSet set = (AnimatorSet)AnimatorInflater.loadAnimator(myContext, R.anim.property_animator);
set.setTarget(mButton);
set.start();
```

### 插值器和估值器
- TimeInterpolator  
    为时间插值器，根据时间流逝的百分比来计算出当前属性值改变的百分比，系统预置的有LinearInterpolator(线性插值器：匀速动画)、AccelerateDecelerateInterpolator(加速减速插值器：两头慢中间快)和DecelerateInterpolator(减速插值器)
- TypeEvaluator  
为类型估值器，根据当前属性改变的百分比来计算改变后的属性值，系统预置的有IntEvaluator(针对整型属性)、FloatEvaluator(针对浮点型属性)和ArgbEvaluator(针对Color属性)。

### 属性动画的监听器
属性动画主要有两个接口AnimatorUpdateListener和AnimatorListener。同时为了方便开发，系统还提供了AnimatorListenerAdapter类，这样可以有选择地实现AnimatorListener的接口。
```
public static interface AnimatorListener {
    void onAnimationStart(Animator animation);
    void onAnimationEnd(Animator animation);
    void onAnimationCancel(Animator animation);
    void onAnimationRepeat(Animator animation);
}

public static interface AnimatorUpdateListener {
    //动画每播放一帧(动画10ms/帧)，该方法回调一次
    void onAnimationUpdate(ValueAnimator animation);
}
```

### 对任意属性做动画
比如给Button做宽度动画，由于Button的width没有对应的get和set方法，故无法直接通过ObjectAnimator做宽度动画。  
解决办法：
1. 给Button的width属性增加set和get方法（源码无法更改，放弃）
2. 用一个类来包装原始对象，间接为其提供get和set方法
```
private void performAnimate() {
    ViewWrapper wrapper = new ViewWrapper(mButton);
    ObjectAnimator.ofInt(wrapper, "width", 500).setDuration(5000).start();
}

private static class ViewWrapper {
    private View target;
    
    public void (View target) {
        this.target = target;
    }
    
    public int getWidth() {
        return target.getLayoutParams().width;
    }
    
    public void setWidth(int width) {
        target.getLayoutParams().width = width;
        target.requestLayout();
    }
}
```
3. 采用ValueAnimator，监听动画过程，自己实现属性的改变
```
performAnimate(mButton, mButton.getWidth(), 500);

private void performAnimate(final View target, final int start, final end) {
    ValueAnimator valueAnimator = ValueAnimator.ofInt(1, 100);
    valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
        //持有一个IntEvaluator对象，方便下面估值的时候使用
        private IntEvaluator intEvaluator = new IntEvaluator();

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            //获得当前动画的进度值，整型，1~100之间
            int currentValue = (int) animation.getAnimatedValue();
            float fraction = animation.getAnimatedFraction();
            Log.d(TAG, "current value = " + currentValue + ", fraction = " + fraction);

            //获得当前进度占整个动画过程的比例,浮点型，0~1之间
            button.getLayoutParams().width = intEvaluator.evaluate(fraction, start, end);
            target.requestLayout();
        }
    });
    valueAnimator.setDuration(5000).start();
}
```

## 使用动画的注意事项
1. OOM问题
主要是帧动画，尤其是图片数量较多且图片较大时
2. 内存泄露
Activity退出或者View的dettachFromWindow及时清理动画
3. View动画问题
View动画是对动画的影像做动画，有时候会出现动画完成后View无法隐藏的现象，即setVisibility(View.GONE)失效，这时只需要调用view.clearAnimation()清除View动画即可。
4. 不用使用px
尽量使用dp，px会导致在不同设备上有不同的效果
5. 动画元素的交互
View动画不是改变动画的属性，导致View位置变换后，旧位置仍然可以触发单击事件
6. 硬件加速
使用动画的过程中，建议开启硬件加速，提高动画的流畅性。