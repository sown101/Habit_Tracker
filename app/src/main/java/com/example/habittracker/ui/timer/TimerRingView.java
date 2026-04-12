package com.example.habittracker.ui.timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class TimerRingView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF arcRect = new RectF();

    private float progress = 0f; // 0 -> 1
    private float strokeWidth;
    private float dotRadius;

    public TimerRingView(Context context) {
        super(context);
        init();
    }

    public TimerRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimerRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        strokeWidth = dp(16);
        dotRadius = dp(9);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setColor(0xFF392C63);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setColor(0xFF8F76F9);

        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(0xFFD9C8FF);
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height);

        float halfStroke = strokeWidth / 2f;
        float left = (width - size) / 2f + halfStroke;
        float top = (height - size) / 2f + halfStroke;
        float right = left + size - strokeWidth;
        float bottom = top + size - strokeWidth;

        arcRect.set(left, top, right, bottom);

        // vòng nền
        canvas.drawArc(arcRect, -90, 360, false, trackPaint);

        // vòng tiến độ
        float sweepAngle = 360f * progress;
        canvas.drawArc(arcRect, -90, sweepAngle, false, progressPaint);

        // chấm sáng ở đầu progress
        if (progress > 0f) {
            float cx = arcRect.centerX();
            float cy = arcRect.centerY();
            float radius = arcRect.width() / 2f;

            double angleRad = Math.toRadians(sweepAngle - 90f);
            float dotX = cx + (float) (radius * Math.cos(angleRad));
            float dotY = cy + (float) (radius * Math.sin(angleRad));

            canvas.drawCircle(dotX, dotY, dotRadius, dotPaint);
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}