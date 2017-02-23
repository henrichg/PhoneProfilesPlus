package com.andraskindler.quickscroll;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class Pin extends View {

    private static final int mPinColor = Color.argb(224, 66, 66, 66);
    private Paint mPaint;
    private Path mPath;

    public Pin(Context context) {
        super(context);
        init();
    }

    public Pin(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Pin(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    private void init() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        setColor(mPinColor);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            mPath.reset();
            mPath.moveTo(0, getHeight());
            mPath.lineTo(getWidth(), getHeight() / 2);
            mPath.lineTo(0, 0);
            mPath.close();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
        super.onDraw(canvas);
    }

}
