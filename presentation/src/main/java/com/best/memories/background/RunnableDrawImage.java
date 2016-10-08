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
    private boolean mMoveLeft;
    private boolean mScaleImageOut;
    private boolean mZoomIn;
    private boolean mMoveRight = true;
    private boolean mShowBackgroundBitmap;

    private int mCenterBitmapX;
    private int mCenterBitmapY;
    private int mScreenHeight;
    private int mScreenWidth;

    private float mBaseLeftForwardSide;
    private float mBaseTopForwardSide;
    private float mBaseRightForwardSide;
    private float mBaseBottomForwardSide;
    private float mLeftForwardSide;
    private float mTopForwardSide;
    private float mRightForwardSide;
    private float mBottomForwardSide;

    private final SurfaceHolder mSurfaceHolder;
    private Bitmap mDrawImage;
    private Bitmap mBackgroundBitmap;

    private IDrawBitmap mListener;

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

            canvas.drawBitmap(mDrawImage, matrix, paint);

//            if (mShowBackgroundBitmap) {
//                float left = 0;
//                float top = 0;
//                RectF rectScreen = new RectF(left, top, mScreenWidth, mScreenHeight);
//
//                canvas.drawBitmap(mBackgroundBitmap, null, rectScreen, null);
//            }

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

    public void calculateScaledBitmapSize() {
        int outWidth = mDrawImage.getWidth();
        int outHeight = mDrawImage.getHeight();

        if (mScreenWidth > outWidth || mScreenHeight > outHeight) {
            outWidth = mDrawImage.getWidth() * 3;
            outHeight = mDrawImage.getHeight() * 3;

            mDrawImage = Bitmap.createScaledBitmap(mDrawImage, outWidth, outHeight, true);
        }

        mCenterBitmapX = outWidth / 2;
        mCenterBitmapY = outHeight / 2;
    }

    public void calculateRectanglePoints() {
        mLeftForwardSide = Math.abs(mCenterBitmapX - mScreenWidth / 2);
        mTopForwardSide = Math.abs(mCenterBitmapY - mScreenHeight / 2);
        mRightForwardSide = Math.abs(mCenterBitmapX + mScreenWidth / 2);
        mBottomForwardSide = Math.abs(mCenterBitmapY + mScreenHeight / 2);

        mBaseLeftForwardSide = mLeftForwardSide;
        mBaseTopForwardSide = mTopForwardSide;
        mBaseRightForwardSide = mRightForwardSide;
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

    public interface IDrawBitmap {
        void drawBitmap();
    }
}

