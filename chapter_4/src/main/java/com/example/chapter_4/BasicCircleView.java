package com.example.chapter_4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 不支持padding的自定义View，同时wrap_content与match_parent效果一样
 */
public class BasicCircleView extends View {

    private int mColor = Color.RED;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BasicCircleView(Context context) {
        this(context, null);
    }

    public BasicCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasicCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint.setColor(mColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2;
        canvas.drawCircle(width / 2, height / 2,
                radius, mPaint);
    }
}
