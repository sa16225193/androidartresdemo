package com.example.chapter_7;

import android.view.View;

/**
 * Created by liuyong on 2020/6/29
 */
public class ViewWrapper {

    private View mTarget;

    public ViewWrapper(View target) {
        this.mTarget = target;
    }

    public int getWidth() {
        return mTarget.getLayoutParams().width;
    }

    public void setWidth(int width) {
        mTarget.getLayoutParams().width = width;
        mTarget.requestLayout();
    }
}
