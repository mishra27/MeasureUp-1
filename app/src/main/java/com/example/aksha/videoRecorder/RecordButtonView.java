package com.example.aksha.videoRecorder;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class RecordButtonView extends View {
    private Paint paint = new Paint();
    private boolean recording = false;

    private ValueAnimator animation;

    public RecordButtonView(Context context) {
        super(context);
        init();
    }

    public RecordButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecordButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        animation = ValueAnimator.ofFloat(0, 1);
        animation.setDuration(250);

        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RecordButtonView.this.invalidate();
            }
        });

        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight();
        int width = getWidth();
        float radius = Math.min(width, height) / 2f;
        float strokeWidth = radius / 10;
        float buttonRadius = (radius - strokeWidth) * 0.75f / (float) Math.sqrt(2);

        // draw white outline
        paint.setARGB(255, 255, 255, 255);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawCircle(width / 2, height / 2, radius - strokeWidth / 2, paint);

        // draw red record/stop button
        paint.setARGB(255, 255, 0, 0);
        paint.setStyle(Paint.Style.FILL);

        float left = width / 2 - buttonRadius;
        float right = width / 2 + buttonRadius;
        float top = height / 2 - buttonRadius;
        float bottom = height / 2 + buttonRadius;

        float animatedFraction = animation.getAnimatedFraction();
        float roundingRadius = (1 - animatedFraction) * buttonRadius + animatedFraction * buttonRadius / 4;

        canvas.drawRoundRect(left, top, right, bottom, roundingRadius, roundingRadius, paint);
    }

    /**
     * Sets if the view should represent the recording state or not. Starts the animation if the
     * new recording value is different than the old.
     * @param recording
     */
    public void setRecording(boolean recording) {
        if (recording != this.recording) {
            if (recording) {
                animation.start();
            } else {
                animation.reverse();
            }
        }

        this.recording = recording;
    }

    public boolean isRecording() {
        return this.recording;
    }
}
