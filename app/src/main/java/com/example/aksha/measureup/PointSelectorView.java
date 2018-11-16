package com.example.aksha.measureup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.MotionEventCompat;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class PointSelectorView extends View {
    private int mActivePointerId = INVALID_POINTER_ID;
    private float initialTouchX = 0;
    private float initialTouchY = 0;

    private Paint outlinePaint;
    private Paint fillPaint;

    public PointSelectorView(Context context) {
        super(context);
        init();
    }

    public PointSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PointSelectorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        outlinePaint = new Paint();
        outlinePaint.setAntiAlias(true);

        outlinePaint.setARGB(255, 255, 255, 255);
        outlinePaint.setStrokeWidth(2);
        outlinePaint.setStyle(Paint.Style.STROKE);

        fillPaint = new Paint();
        fillPaint.setARGB(127, 0, 0, 0);
        fillPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight();
        int width = getWidth();
        float radius = Math.min(width, height) / 2f;

        canvas.drawCircle(width / 2, height / 2, radius - 2 * outlinePaint.getStrokeWidth(), fillPaint);
        canvas.drawCircle(width / 2, height / 2, radius - 2 * outlinePaint.getStrokeWidth(), outlinePaint);

        float crossSize = (radius - 1) / 2f;
        canvas.drawLine(width / 2 - crossSize, height / 2, width / 2 + crossSize, height / 2, outlinePaint);
        canvas.drawLine(width / 2, height / 2 - crossSize, width / 2, height / 2 + crossSize, outlinePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                // Remember where we started (for dragging)
                initialTouchX = x;
                initialTouchY = y;
                // Save the ID of this pointer (for dragging)
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // Find the index of the active pointer and fetch its position
                final int pointerIndex =
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                // getX and getY returns points relative to current view position
                // see https://stackoverflow.com/questions/17530589/jumping-imageview-while-dragging-getx-and-gety-values-are-jumping
                // since we are updating the x and y, getX and getY will return displacement from origin
                // need to keep track of initial touch to maintain the place where touched as 'origin'
                // reposition displacement so that initial touch is origin
                final float dx = MotionEventCompat.getX(ev, pointerIndex) - initialTouchX;
                final float dy = MotionEventCompat.getY(ev, pointerIndex) - initialTouchY;

                View parent = (View) this.getParent();

                // bound x to being halfway off of parent
                float x = Math.max(-this.getWidth() / 2, this.getX() + dx);
                x = Math.min(x, parent.getWidth() - this.getWidth() / 2);

                // bound y to being halfway off of parent
                float y = Math.max(-this.getHeight() / 2, this.getY() + dy);
                y = Math.min(y, parent.getHeight() - this.getHeight() / 2);

                this.setX(x);
                this.setY(y);

                invalidate();

                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                // handles multitouch on up, uncomment and modify if needed
                /*final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    initialTouchX = MotionEventCompat.getX(ev, newPointerIndex);
                    initialTouchY = MotionEventCompat.getY(ev, newPointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }*/
                break;
            }
        }
        return true;
    }
}
