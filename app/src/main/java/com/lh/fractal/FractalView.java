package com.lh.fractal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by home on 2017/1/3.
 * 显示分形的View
 */

public class FractalView extends TextureView {

    private static final int COUNT = 8;

                private int mPeerRectXSize;
                private int mPeerRectYSize;

                private Complex mComplex;

                private int mWidth;
                private int mHeight;

                private int mIterateTimes = 256;

                private float mMinX;
                private float mMaxX;
                private float mMinY;
                private float mMaxY;

                private Surface mSurface;
                private CompositeSubscription mWorksSubscriptions;
                private Bitmap mBitmap;
                private final Object mLock = new Object();

                private Matrix mMatrix;

                private boolean mShouldBeginDraw;

                private int mShowIndex = 1;

                private int mTotalProgress;
                private int mProgress;

                private GestureDetector mGestureDetector;
                private ScaleGestureDetector mScaleGestureDetector;

                private OnProgressChangeListener mOnProgressChangeListener;

                public FractalView(Context context) {
                    super(context);
                    init();
                }

                public FractalView(Context context, AttributeSet attrs) {
                    super(context, attrs);
                    init();
                }

                public FractalView(Context context, AttributeSet attrs, int defStyleAttr) {
                    super(context, attrs, defStyleAttr);
                    init();
                }

            public void setIterateTimes(int times) {
                mIterateTimes = times;
                beginDrawBitmap();
            }

            public void setOnProgressChangeListener(OnProgressChangeListener listener) {
                mOnProgressChangeListener = listener;
            }

            private void init() {
                mMatrix = new Matrix();
                setSurfaceTextureListener(TVListener);
                mGestureDetector = new GestureDetector(getContext(), mGestureListener);
                mScaleGestureDetector = new ScaleGestureDetector(getContext(), mScaleGestureListener);
            }

            public void restSize() {
                mMinX = -2.5f;
                mMaxX = 2.5f;
                mMaxY = mMaxX * mHeight / mWidth;
                mMinY = -mMaxY;
                beginDrawBitmap();
            }

            private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    mShouldBeginDraw = true;
                    translateBitmap(distanceX, distanceY);
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            };

            private ScaleGestureDetector.SimpleOnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                private float preFocusX;
                private float preFocusY;

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    preFocusX = detector.getFocusX();
                    preFocusY = detector.getFocusY();
                    return true;
                }

                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    scaleAndMoveBitmap(1 / detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY(), preFocusX - detector.getFocusX(), preFocusY - detector.getFocusY());
                    preFocusX = detector.getFocusX();
                    preFocusY = detector.getFocusY();
                    mShouldBeginDraw = true;
                    return true;
                }
            };

//    private float[] mLastX = new float[2];
//    private float[] mLastY = new float[2];
//    private boolean mIsDraging;
//    private int mFristPointId;
//    private int mSecondPointId;
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        System.out.println("mFirstId:" + mFristPointId + "-mSecondId:" + mSecondPointId + "count:" + event.getPointerCount());
//        switch (event.getActionMasked()) {
//            case MotionEvent.ACTION_DOWN:
//                int firstIndex = event.getActionIndex();
//                mFristPointId = event.getPointerId(firstIndex);
//                mSecondPointId = MotionEvent.INVALID_POINTER_ID;
//                mLastX[0] = event.getX(firstIndex);
//                mLastY[0] = event.getY(firstIndex);
//                mIsDraging = false;
//                break;
//            case MotionEvent.ACTION_POINTER_DOWN:
//                if (mSecondPointId == MotionEvent.INVALID_POINTER_ID) {
//                    int secondIndex = event.getActionIndex();
//                    mSecondPointId = event.getPointerId(secondIndex);
//                    mLastX[1] = event.getX(secondIndex);
//                    mLastY[1] = event.getY(secondIndex);
//                }
//                break;
//            case MotionEvent.ACTION_POINTER_UP:
//                int leaveIndex = event.getActionIndex();
//                int leaveId = event.getPointerId(leaveIndex);
//                if (leaveId == mFristPointId) {
//                    int remainIndex = event.findPointerIndex(mSecondPointId);
//                    if (remainIndex < 0) {
//                        Log.e("onTouchEvent", "invalid index");
////                        mFristPointId = MotionEvent.INVALID_POINTER_ID;
////                        mSecondPointId = MotionEvent.INVALID_POINTER_ID;
//                        break;
//                    }
//                    mFristPointId = mSecondPointId;
//                    mLastX[0] = event.getX(remainIndex);
//                    mLastY[0] = event.getY(remainIndex);
//                    mSecondPointId = MotionEvent.INVALID_POINTER_ID;
//                } else if (leaveId == mSecondPointId) {
//                    mSecondPointId = MotionEvent.INVALID_POINTER_ID;
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                firstIndex = event.findPointerIndex(mFristPointId);
//                if (firstIndex < 0) {
//                    mFristPointId = event.getPointerId(0);
//                    mLastX[0] = event.getX(0);
//                    mLastY[0] = event.getY(0);
//                    Log.e("onTouchEvent", "first id is invalid");
//                }
//                float currentX = event.getX(firstIndex);
//
//                float currentY = event.getY(firstIndex);
//                float dx = currentX - mLastX[0];
//                float dy = currentY - mLastY[0];
//                if (Math.abs(dx) > mTouchSlop || Math.abs(dy) > mTouchSlop) {
//                    mIsDraging = true;
//                }
//                if (mIsDraging) {
//                    if (mSecondPointId == MotionEvent.INVALID_POINTER_ID) {
//                        translateBitmap(-dx, -dy);
//                        mLastX[0] = currentX;
//                        mLastY[0] = currentY;
//                    } else {
//                        int secondIndex = event.findPointerIndex(mSecondPointId);
//                        float currentX1 = event.getX(secondIndex);
//                        float currentY1 = event.getY(secondIndex);
//                        if (distance(mLastX[0], mLastY[0], mLastX[1], mLastY[1]) < mMinScaleSpan) {
//                            break;
//                        }
//
//                        float scale = distance(mLastX[0], mLastY[0], mLastX[1], mLastY[1]) / distance(currentX, currentY, currentX1, currentY1);
//
//                        float centerX = (currentX + currentX1) / 2;
//                        float centerY = (currentY + currentY1) / 2;
//                        float centerXLast = (mLastX[0] + mLastX[1]) / 2;
//                        float centerYLast = (mLastY[0] + mLastY[1]) / 2;
//
//                        scaleAndMoveBitmap(scale, centerX, centerY, centerXLast - centerX, centerYLast - centerY);
//                        mLastX[0] = currentX;
//                        mLastY[0] = currentY;
//                        mLastX[1] = currentX1;
//                        mLastY[1] = currentY1;
//                    }
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                if (mIsDraging) {
//                    beginDrawBitmap();
//                }
//                break;
//        }
//        return true;
//    }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                boolean consume = mScaleGestureDetector.onTouchEvent(event);
                if (!mScaleGestureDetector.isInProgress()){
                    consume = mGestureDetector.onTouchEvent(event);
                }
                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (mShouldBeginDraw) {
                        mShouldBeginDraw = false;
                        beginDrawBitmap();
                    }
                }
                return consume || super.onTouchEvent(event);
            }

            private void scale(float scale, float x, float y) {
                float tx = x * (mMaxX - mMinX) / mWidth + mMinX;
                float ty = y * (mMinY - mMaxY) / mHeight + mMaxY;
                translate(tx, ty);
                scale(scale);
                translate(-tx, -ty);
            }

            private void scale(float scale) {
                mMaxX *= scale;
                mMinX *= scale;
                mMaxY *= scale;
                mMinY *= scale;
            }

            private void translate(float dx, float dy) {
                mMaxY -= dy;
                mMinY -= dy;
                mMaxX -= dx;
                mMinX -= dx;
            }

            private void translateBitmap(float distanceX, float distanceY) {
                float diffY = distanceY * (mMaxY - mMinY) / mHeight;
                float diffX = distanceX * (mMaxX - mMinX) / mWidth;
                translate(-diffX, diffY);
                mMatrix.postTranslate(-distanceX, -distanceY);
                syncDraw(mBitmap, mMatrix, true);
            }

            private void scaleAndMoveBitmap(float scale, float x, float y, float dx, float dy) {
                float diffY = dy * (mMaxY - mMinY) / mHeight;
                float diffX = dx * (mMaxX - mMinX) / mWidth;
                translate(-diffX, diffY);
                scale(scale, x, y);
                mMatrix.postTranslate(-dx, -dy);
                mMatrix.postScale(1 / scale, 1 / scale, x, y);
                syncDraw(mBitmap, mMatrix, true);
            }

            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                mWidth = w;
                mHeight = h;
                mPeerRectXSize = mWidth / COUNT;
                mPeerRectYSize = mHeight / COUNT;
                mMinX = -2.5f;
                mMaxX = 2.5f;
                mMaxY = mMaxX * mHeight / mWidth;
                mMinY = -mMaxY;
                mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            }


            private TextureView.SurfaceTextureListener TVListener = new TextureView.SurfaceTextureListener() {

                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    mSurface = new Surface(surface);
                    if (mBitmap != null && !mBitmap.isRecycled()) {
                        Canvas canvas = mSurface.lockCanvas(null);
                        canvas.drawBitmap(mBitmap, 0, 0, null);
                        mSurface.unlockCanvasAndPost(canvas);
            }
            if (mShouldBeginDraw) {
                mShouldBeginDraw = false;
                beginDrawBitmap();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (mSurface != null) {
                mSurface.release();
                mSurface = null;
            }
            if (mWorksSubscriptions != null && !mWorksSubscriptions.isUnsubscribed()) {
                mWorksSubscriptions.unsubscribe();
            }
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    public void beginDraw(float real, float im, @IntRange(from = 1, to = 2) int mode) {
        mShowIndex = mode;
        mComplex = new Complex(real, im);
        if (mSurface != null) {
            beginDrawBitmap();
        } else {
            mShouldBeginDraw = true;
        }
    }

    private void beginDrawBitmap() {
        mMatrix.reset();
        Canvas canvas = new Canvas(mBitmap);
        canvas.drawColor(Color.WHITE);
        if (mWorksSubscriptions != null && !mWorksSubscriptions.isUnsubscribed()) {
            mWorksSubscriptions.unsubscribe();
        }

        final ArrayList<Rect> list = new ArrayList<>(COUNT * COUNT);
        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < COUNT; j++) {
                Rect r = new Rect(i * mPeerRectXSize, j * mPeerRectYSize, (i + 1) * mPeerRectXSize, (j + 1) * mPeerRectYSize);
                list.add(r);
            }
        }
        mTotalProgress = list.size();
        mProgress = 0;
//        Collections.shuffle(list);
        mWorksSubscriptions = new CompositeSubscription();
        for (final Rect rect : list)
            mWorksSubscriptions.add(Schedulers.computation().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    synchronized (mLock) {
                        calculateBitmap(rect, mComplex.re, mComplex.im);
                    }
                    syncDraw(mBitmap, true);
                    mProgress++;
                    post(new Runnable() {
                        @Override
                        public void run() {
                            if (mOnProgressChangeListener != null) {
                                mOnProgressChangeListener.onProgressChange(mTotalProgress, mProgress);
                            }
                        }
                    });
                }
            }));
    }


    private void syncDraw(Bitmap bitmap, @Nullable Matrix matrix, boolean clear) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        synchronized (mLock) {
            if (mSurface != null) {
                Canvas canvas = mSurface.lockCanvas(null);
                if (clear) {
                    canvas.drawColor(Color.WHITE);
                }
                if (matrix == null) {
                    canvas.drawBitmap(bitmap, 0, 0, null);
                } else {
                    canvas.drawBitmap(bitmap, matrix, null);
                }
                mSurface.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void syncDraw(Bitmap bitmap, boolean clear) {
        syncDraw(bitmap, null, clear);
    }

    private void calculateBitmap(Rect r, float re, float im) {
        Complex z = new Complex(0f, 0f);
        Complex c = new Complex(re, im);
        for (int i = r.left; i < r.right; i++) {
            for (int j = r.top; j < r.bottom; j++) {
                if (mShowIndex == 1) {
                    z.re = i * (mMaxX - mMinX) / mWidth + mMinX;
                    z.im = j * (mMinY - mMaxY) / mHeight + mMaxY;
                } else {
                    c.re = i * (mMaxX - mMinX) / mWidth + mMinX;
                    c.im = j * (mMinY - mMaxY) / mHeight + mMaxY;
                    z.re = 0;
                    z.im = 0;
                }
                int k = 0;
                int color = Color.BLACK;
                for (; k < mIterateTimes; k++) {
                    if (z.abs() > 4) {
                        color = generateColor(k);
                        break;
                    }
                    z.mul(z);
                    z.add(c);
                }
                mBitmap.setPixel(i, j, color);
            }
        }
    }

    /**
     * map iterate times to rgb color
     *
     * @param k iterate times
     * @return Color
     */
    protected int generateColor(int k) {
        int r, g, b;
        r = k * 8 % 256;
        g = k * 12 % 256;
        b = 100;
        return Color.argb(255, r, g, b);
    }

    public void saveToFile(File file) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.close();
    }

    public interface OnProgressChangeListener {
        void onProgressChange(int max, int current);
    }
}
