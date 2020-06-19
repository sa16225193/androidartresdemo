Draw的作用是将View绘制到屏幕上面，遵循以下几步：
1. 绘制背景background.draw(canvas)
2. 绘制自己(onDraw)
3. 绘制children(dispatchDraw)
4. 绘制装饰(onDrawScrollBars)

```
public void draw(Canvas canvas) {
    final int privateFlags = mPrivateFlags;
    final boolean dirtyOpaque = (privateFlags & PFLAG_DIRTY_MASK) == PFLAG_DIRTY_OPAQUE && (mAttachInfo == null || !mAttachInfo.mIgnoreDirtyState);
    mPrivateFlags = (privateFlags & ~PFlAG_DIRTY_MASK) | PFLAG_DRAWN;
    
    int saveCount;
    
    if (!dirtyOpaque) {
        drawBackground(canvas);
    }
    
    final int viewFlags = mViewFlags;
    boolean horizontalEdges = (viewFlags & FADING_EDGE_HORIZONTAL) != 0;
    boolean verticalEdges = (viewFlags & FADING_EDGE_VERTICAL) != 0;
    if (!verticalEdge && !horizontalEdges) {
        if (!dirtyOpaque) onDraw(canvas);
        
        dispatchDraw(canvas);
        
        onDrawScrollBars(canvas);
        
        if (mOverlay != null && !mOverlay.isEmpty()) {
            mOverlay.getOverlayView().dispatchDraw(canvas);
        }
        
        return;
    }
}
```
View绘制过程的传递是通过dispatchDraw来实现的，dispatchDraw会遍历调用所有子元素的draw方法，如此draw事件就一层层地传递了下去。

#### setWillNotDraw
```
public void setWillNotDraw(boolean willNotDraw) {
    setFlags(willNotDraw ? WILL_NOT_DRAW : 0, DRAW_MASK);
}
```
如果一个View不需要绘制任何内容，那么这个标记位设置为true后，系统会进行相应的优化。