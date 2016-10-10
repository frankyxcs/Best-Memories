package com.best.memories.background;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;

import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Color.WHITE;
import static android.graphics.Matrix.ScaleToFit.CENTER;
import static android.graphics.PorterDuff.Mode.CLEAR;
import static com.best.memories.application.BestMemoriesApplication.TAG;

/**
 * Created by Terry on 10/4/2016.
 * Drawing image on canvas
 */

public class RunnableDrawImage implements Runnable {
    private static final int MAX_BITMAP_OPACITY = 250;
    private boolean mZoomIn;
    private boolean mShowBackgroundBitmap;

    private int mScreenHeight;
    private int mScreenWidth;

    private float mBaseTopForwardSide;
    private float mBaseBottomForwardSide;
    private float mLeftForwardSide;
    private float mTopForwardSide;
    private float mRightForwardSide;
    private float mBottomForwardSide;

    private final SurfaceHolder mSurfaceHolder;
    private Bitmap mDrawImage;
    private Bitmap mBackgroundBitmap;

    private IDrawBitmap mListener;
    private int mForwardBitmapOpacity;


    public RunnableDrawImage(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        try {
            canvas = mSurfaceHolder.lockCanvas();
            canvas.drawColor(TRANSPARENT, CLEAR);

            calculateOffsetDirection();

            Matrix matrix = getMatrix();

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setDither(true);
            paint.setColor(WHITE);

            if (mShowBackgroundBitmap) {
                paint.setAlpha(MAX_BITMAP_OPACITY - mForwardBitmapOpacity);

            } else {
                paint.setAlpha(MAX_BITMAP_OPACITY);
            }

            canvas.drawBitmap(mDrawImage, matrix, paint);

            if (mShowBackgroundBitmap) {
                float left = 0;
                float top = 0;

                int centerBitmapX = mBackgroundBitmap.getWidth() / 2;
                int centerBitmapY = mBackgroundBitmap.getHeight() / 2;

                int leftForwardSide = Math.abs(centerBitmapX - mScreenWidth / 2);
                int topForwardSide = Math.abs(centerBitmapY - mScreenHeight / 2);
                int rightForwardSide = Math.abs(centerBitmapX + mScreenWidth / 2);
                int bottomForwardSide = Math.abs(centerBitmapY + mScreenHeight / 2);

                RectF rectImage = new RectF(leftForwardSide, topForwardSide, rightForwardSide, bottomForwardSide);
                RectF rectScreen = new RectF(left, top, mScreenWidth, mScreenHeight);

                Matrix matrixBackground = new Matrix();
                matrixBackground.setRectToRect(rectImage, rectScreen, CENTER);

                paint.setAlpha(mForwardBitmapOpacity);

                canvas.drawBitmap(mBackgroundBitmap, matrixBackground, paint);
            }

            if (mListener != null) {
                mListener.drawBitmap();
            }

        } finally {
            try {
                if (canvas != null) {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }

            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    public Bitmap calculateScaledBitmapSize(Bitmap bitmap) {
        int outWidth = bitmap.getWidth();
        int outHeight = bitmap.getHeight();

        if (mScreenWidth > outWidth || mScreenHeight > outHeight) {
            int proportionValueWidth = 30;
            int proportionValueHeight = 30;
            int proportionValueTwo = 100;

            outWidth = outWidth + (outWidth * proportionValueWidth) / proportionValueTwo;
            outHeight = outHeight + (outHeight * proportionValueHeight) / proportionValueTwo;

            bitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, true);

            return calculateScaledBitmapSize(bitmap);
        } else {
            return bitmap;
        }
    }

    public void calculateRectanglePoints(Bitmap bitmap) {
        int centerBitmapX = bitmap.getWidth() / 2;
        int centerBitmapY = bitmap.getHeight() / 2;

        mLeftForwardSide = Math.abs(centerBitmapX - mScreenWidth / 2);
        mTopForwardSide = Math.abs(centerBitmapY - mScreenHeight / 2);
        mRightForwardSide = Math.abs(centerBitmapX + mScreenWidth / 2);
        mBottomForwardSide = Math.abs(centerBitmapY + mScreenHeight / 2);

        mBaseTopForwardSide = mTopForwardSide;
        mBaseBottomForwardSide = mBottomForwardSide;
    }

    private void calculateOffsetDirection() {
        float offset = 2;

        if (mZoomIn) {
            zoomImageIn(offset);
        } else {
            zoomImageOut(offset);
        }
    }

    private void zoomImageOut(float offset) {
        if (mTopForwardSide <= mBaseTopForwardSide) {
            mTopForwardSide = mTopForwardSide - offset;
        }
        if (mBottomForwardSide >= mBaseBottomForwardSide) {
            mBottomForwardSide = mBottomForwardSide + offset;
        }

        if (mTopForwardSide == 0) {
            mZoomIn = true;
        }
    }

    private void zoomImageIn(float offset) {
        if (mTopForwardSide <= mBaseTopForwardSide) {
            mTopForwardSide = mTopForwardSide + offset;
        }
        if (mBottomForwardSide >= mBaseBottomForwardSide) {
            mBottomForwardSide = mBottomForwardSide - offset;
        }

        if (mTopForwardSide == mBaseTopForwardSide) {
            mZoomIn = false;
        }
    }

    public void resetStates() {
        mZoomIn = false;
    }

    @NonNull
    private Matrix getMatrix() {
        RectF rectImage = new RectF(mLeftForwardSide, mTopForwardSide, mRightForwardSide, mBottomForwardSide);
        float left = 0;
        float top = 0;
        RectF rectScreen = new RectF(left, top, mScreenWidth, mScreenHeight);

        Matrix matrix = new Matrix();
        matrix.setRectToRect(rectImage, rectScreen, CENTER);
        return matrix;
    }

    public void updateBitmap(Bitmap bitmap) {
        mDrawImage = bitmap;
    }

    public void setShowBackgroundBitmap(boolean showBackgroundBitmap) {
        mShowBackgroundBitmap = showBackgroundBitmap;
    }

    public void updateBackgroundBitmap(Bitmap bitmap) {
        mBackgroundBitmap = bitmap;
    }

    public void setListener(IDrawBitmap listener) {
        mListener = listener;
    }

    public void setForwardBitmapOpacity(int forwardBitmapOpacity) {
        mForwardBitmapOpacity = forwardBitmapOpacity;
    }

    public void updateScreenResolution(int width, int height) {
        mScreenWidth = width;
        mScreenHeight = height;
    }

    public interface IDrawBitmap {
        void drawBitmap();
    }

}

