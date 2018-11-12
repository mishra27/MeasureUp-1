package com.example.aksha.videoRecorder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class RecordButtonView extends View {
    private Paint paint = new Paint();
    private boolean recording = false;

    private class RecordButtonOnClickListener implements OnClickListener {
        private RecordButtonView recordButtonView;

        RecordButtonOnClickListener(RecordButtonView recordButtonView) {
            this.recordButtonView = recordButtonView;
        }

        @Override
        public void onClick(View v) {
            this.recordButtonView.setRecording(!this.recordButtonView.isRecording());
            this.recordButtonView.invalidate();
        }
    }

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
        paint.setAntiAlias(true);
        // this.setOnClickListener(new RecordButtonOnClickListener(this));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight();
        int width = getWidth();
        float radius = Math.min(width, height) / 2;

        paint.setARGB(255, 255, 255, 255);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        canvas.drawCircle(width / 2, height / 2, radius - 10, paint);

        paint.setARGB(255, 255, 0, 0);
        paint.setStyle(Paint.Style.FILL);

        float innerRadius = (radius - 10) / 2f;

        if (recording) {
            canvas.drawRoundRect(width / 2 - innerRadius, height / 2 - innerRadius, width / 2 + innerRadius, height / 2 + innerRadius, innerRadius / 4, innerRadius / 4, paint);
        } else {
            canvas.drawCircle(width / 2, height / 2, (radius - 10) / 2, paint);
        }
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public boolean isRecording() {
        return this.recording;
    }
}
