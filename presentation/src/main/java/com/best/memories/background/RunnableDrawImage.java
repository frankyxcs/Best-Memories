package com.best.memories.background;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;

import com.best.memories.application.BestMemoriesApplication;

import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Matrix.ScaleToFit.CENTER;
import static android.graphics.PorterDuff.Mode.CLEAR;

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


    public RunnableDrawImage(SurfaceHolder surfaceHolder, int width, int height) {
        mSurfaceHolder = surfaceHolder;
        mScreenWidth = width;
        mScreenHeight = height;
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

            if (mShowBackgroundBitmap) {
                paint.setAlpha(MAX_BITMAP_OPACITY - mForwardBitmapOpacity);
            }

            canvas.drawBitmap(mDrawImage, matrix, paint);

            if (mShowBackgroundBitmap && mBackgroundBitmap != null) {
                float left = 0;
                float top = 0;

                RectF rectImage = new RectF(0, mBackgroundBitmap.getHeight(), 0, mBackgroundBitmap.getHeight());
                RectF rectScreen = new RectF(left, top, mScreenWidth, mScreenHeight);

                Matrix matrixBackground = new Matrix();
                matrix.setRectToRect(rectImage, rectScreen, CENTER);

                canvas.drawBitmap(mBackgroundBitmap, matrixBackground, null);
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
                Log.e(BestMemoriesApplication.TAG, e.toString());
            }
        }
    }

    public Bitmap calculateScaledBitmapSize(Bitmap bitmap) {
        int outWidth = bitmap.getWidth();
        int outHeight = bitmap.getHeight();

        if (mScreenWidth > outWidth || mScreenHeight > outHeight) {
            outWidth = bitmap.getWidth() * 3;
            outHeight = bitmap.getHeight() * 3;

            bitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, true);
        }

        return bitmap;
    }

    public int[] getWidthHeight(Bitmap bitmap) {
        int[] arrayParams = new int[2];
        int outWidth = bitmap.getWidth();
        int outHeight = bitmap.getHeight();

        if (mScreenWidth > outWidth || mScreenHeight > outHeight) {
            outWidth = bitmap.getWidth() * 3;
            outHeight = bitmap.getHeight() * 3;

        }
        arrayParams[0] = outWidth;
        arrayParams[1] = outHeight;

        return arrayParams;
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

    public void setShowBackgroundBitmap(boolean mShowBackgroundBitmap) {
        this.mShowBackgroundBitmap = mShowBackgroundBitmap;
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

    public interface IDrawBitmap {
        void drawBitmap();
    }
}

