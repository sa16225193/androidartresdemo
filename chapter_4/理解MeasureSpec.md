MeasureSpec是一个32位int值，高2位代表SpecMode，低30位代表SpecSize，SpecMode是指测量模式，而SpecSize是指某种测量模式下的大小。

#### MeasureSpec常用常量及方法
```
private static final int MODE_SHIFT = 30;
private static final int MODE_MASK = 0x3 << MODE_SHIFT;
public static final int UNSPECIFIED = 0 << MODE_SHIFT;
public static final int EXACTLY = 1 << MODE_SHIFT;
public static final int AT_MOST = 2 << MODE_SHIFT;


public static int makeMeasureSpec(int size, int mode) {
    if (sUseBrokenMakeMeasureSpec) {
        return size + mode;
    } else {
        return (size & ~MODE_MASK) | (mode & MODE_MASK);
    }
}

public static int getMode(int measureSpec) {
    return (measureSpec & MODE_MASK);
}

public static int getSize(int measureSpec) {
    return (measureSpec & ~MODE_MASK);
}
```

#### SpecMode
1. EXACTLY
父容器已经检测出View所需要的精确大小，这个时候View的最终大小就是SpecSize所指定的值。对应LayoutParams中的match_parent和具体的数值两种模式。

2. UNSPECIFIED
父容器不对View有任何限制，要多大给多大，一般用于系统内部，表示一种测量的状态。

3. AT_MOST
父容器指定了一个可用大小的SpecSize，View的大小不能大于这个值。对应于LayoutParams的wrap_content。

#### MeasureSpec和LayoutParams的对应关系
在测量的时候，系统会将LayoutParams在父容器的约束下转换成对应的MeasureSpec，然后再根据这个MeasureSpec来确定View测量后的宽/高。需要注意的是,MeasureSpec不是唯一由LayoutParams确定的,需要和父容器的MeasureSpec一起才能决定View的MeasureSpec。  
UNSPECIFIED模式主要用于系统内部多次Measure的情形，一般来说，我们不需要关注此模式。

##### 普通View的MeasureSpec创建规则
childLayoutParams\parentLayoutParams | EXACTLY |AT_MOST | UNSPECIFIED
--|--|--|--
dp/px|EXACTLY(childSize)|EXACTLY(childSize)|EXACTLY(childSize)
match_parent|EXACTLY(parentSize)|AT_MOST(parentSize)|UNSPECIFIED(0)
wrap_content|AT_MOST(parentSize)|AT_MOST(parentSize)|UNSPECIFIED(0)




