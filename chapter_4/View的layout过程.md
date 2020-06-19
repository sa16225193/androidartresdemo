layout方法的大致流程如下：
1. 通过setFrame方法来设定View的四个顶点的位置，即初始化mLeft、mRight、mTop和mBottom四个值
2. 调用onLayout方法，父容器确定子元素的位置

比如LinearLayout的onLayout方法如下
```
protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (mOrientation == VERTICAL) {
        layoutVertical(l, t, r, b);
    } else {
        layoutHorizontal(l, t, r, b);
    }
}

void layoutVertical(int left, int top, int right, int bottom) {
    final int count = getVirtualChildrenCount();
    for (int i = 0; i < count; i++) {
        final View child = getVirtualChildAt(i);
        if (child == null) {
            childTop += measureNullChild(i);
        } else if (child.getVisibility() != GONE) {
            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();
            
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
            ...
            if (hasDividerBeforeChildAt(i)) {
                childTop += mDividerHeight;
            }
            
            childTop += lp.topMargin;
            setChildFrame(child, childLeft, childTop + getLocationOffset(child), childWidth, childHeight);
            childTop += childHeight + lp.bottomMargin + getNextLocationOffset(child);
            
            i += getChildrenSkipCount(child, i);
        }
    }
}

private void setChildFrame(View child, int left, int top, int width, int height) {
    child.layout(left, top, left + width, top + height);
}
```