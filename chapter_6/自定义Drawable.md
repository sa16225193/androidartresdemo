下面示例一个自定义Drawable的实现过程，我们通过自定义Drawable来绘制一个圆形Drawable，并且它的半径会随着View的变化而变化，这种Drawable可以作为View的通用背景。

1. 自定义Drawable——CustomDrawable
```
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class CustomDrawable extends Drawable {
    private Paint mPaint;

    public CustomDrawable(int color) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        //获取Drawable的实际区域大小,一般和View的尺寸相同
        final Rect r = getBounds();
        float cx = r.exactCenterX();
        float cy = r.exactCenterY();
        canvas.drawCircle(cx, cy, Math.min(cx, cy), mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();

    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        // not sure, so be safe
        return PixelFormat.TRANSLUCENT;
    }

}
```
在上面的代码中，draw、setAlpha、setColorFilter和getOpacity这几个方法都是必现实现的。其中draw是最主要的方法。

使用自定义Drawable
```
View testCustomDrawable = findViewById(R.id.test_custom_drawable);
            CustomDrawable customDrawable = new CustomDrawable(Color.parseColor("#0ac39e"));
testCustomDrawable.setBackgroundDrawable(customDrawable);
```

> getIntrinsicWidth和getIntrinsicHeight方法需要注意下，当自定义的Drawable有固定大小时，最好重写这两个方法，因为它会影响到View的wrap_content布局。