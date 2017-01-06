package com.lh.fractal;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by home on 2017/1/5.
 */

public class CircleProgressBar extends View {

    private int mMaxProgress;
    private int mProgress;

    private int mHeight;
    private int mWidth;

    private int mProgressWidth;

    private Paint mCirclePaint;
    private Paint mProgressPaint;
    private TextPaint mTextPaint;

    private TextPaint.FontMetrics mFontMetrics;

    private RectF mRectF;

    public CircleProgressBar(Context context) {
        super(context);
        init(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        int mProgressBackgroundColor;
        int mProgressColor;
        int textSize = 10;
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);
            mMaxProgress = array.getInteger(R.styleable.CircleProgressBar_maxProgress, 100);
            mProgress = array.getInteger(R.styleable.CircleProgressBar_progress, 0);
            mProgressColor = array.getColor(R.styleable.CircleProgressBar_progressColor,Color.RED);
            mProgressBackgroundColor = array.getColor(R.styleable.CircleProgressBar_progressBackground,Color.TRANSPARENT);
            textSize = array.getDimensionPixelSize(R.styleable.CircleProgressBar_textSize,10);
            array.recycle();
        } else {
            mMaxProgress = 100;
            mProgress = 0;
            mProgressColor = Color.RED;
            mProgressBackgroundColor = Color.TRANSPARENT;
        }
        mProgressWidth = dp2pix(4);
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(mProgressBackgroundColor);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(mProgressWidth);

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressWidth);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mProgressColor);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setTextAlign(TextPaint.Align.CENTER);
        mFontMetrics = new Paint.FontMetrics();

        mRectF = new RectF();
    }


    public void setMaxProgress(int max) {
        mMaxProgress = max;
        postInvalidate();
    }

    public void setProgress(int progress) {
        mProgress = progress;
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mRectF.set(0,0,Math.min(w,h),Math.min(w,h));
        mRectF.offset((mWidth - mRectF.width())/2,(mHeight - mRectF.height())/2);
        mRectF.top += mProgressWidth/2f;
        mRectF.bottom -= mProgressWidth/2f;
        mRectF.left += mProgressWidth/2f;
        mRectF.right -= mProgressWidth/2f;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(mWidth,mHeight);
        canvas.drawCircle(mWidth/2,mHeight/2,(size - mProgressWidth)/2,mCirclePaint);
        canvas.drawArc(mRectF,-90,(mProgress*360f/mMaxProgress) ,false,mProgressPaint);
        String text = mProgress*100/mMaxProgress+"%";
        mTextPaint.getFontMetrics(mFontMetrics);
        canvas.drawText(text,mWidth/2,mHeight/2 - (mFontMetrics.descent + mFontMetrics.ascent)/2,mTextPaint);
    }

    private int dp2pix(float dip){
        return (int) (getResources().getDisplayMetrics().density * dip + 0.5f);
    }
}
