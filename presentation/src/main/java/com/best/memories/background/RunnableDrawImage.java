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

import org.greenrobot.eventbus.EventBus;

import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Matrix.ScaleToFit.CENTER;
import static android.graphics.PorterDuff.Mode.CLEAR;

/**
 * Created by Terry on 10/4/2016.
 */

public class RunnableDrawImage implements Runnable {
    private final SurfaceHolder mSurfaceHolder;
    private boolean mMoveLeft;
    private boolean mMoveRight = true;
    private boolean mScaleImageOut;
    private boolean mLastZoomIn;

    private int mCenterBitmapX;
    private int mCenterBitmapY;

    private int mScreenHeight;
    private int mScreenWidth;

    private float mBaseLeftSide;
    private float mBaseTopSide;
    private float mBaseRightSide;
    private float mBaseBottom;

    private float mLeftSide;
    private float mTopSide;
    private float mRightSide;
    private float mBottomSide;

    private boolean mShowBackgroundBitmap;

    private Bitmap mDrawImage;
    private Bitmap mBackgroundBitmap;

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
                canvas.drawBitmap(mBackgroundBitmap, 100f, 100f, null);
            }

            canvas.drawBitmap(mDrawImage, matrix, paint);

            DrawBitmap drawBitmap = new DrawBitmap();
            EventBus.getDefault().post(drawBitmap);

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

        if (mScreenWidth > mDrawImage.getWidth()) {
            outWidth = mScreenWidth * 2;
            outHeight = mScreenHeight * 2;
        } else if (mScreenHeight > mDrawImage.getHeight()) {
            outHeight = mScreenHeight * 2;
        }

        mDrawImage = Bitmap.createScaledBitmap(mDrawImage, outWidth, outHeight, true);

        mCenterBitmapX = mDrawImage.getWidth() / 2;
        mCenterBitmapY = mDrawImage.getHeight() / 2;
    }

    public void calculateRectanglePoints() {
        mLeftSide = Math.abs(mCenterBitmapX - mScreenWidth / 2);
        mTopSide = Math.abs(mCenterBitmapY - mScreenHeight / 2);
        mRightSide = Math.abs(mCenterBitmapX + mScreenWidth / 2);
        mBottomSide = Math.abs(mCenterBitmapY + mScreenHeight / 2);

        mBaseLeftSide = mLeftSide;
        mBaseTopSide = mTopSide;
        mBaseRightSide = mRightSide;
        mBaseBottom = mBottomSide;
    }

    private void calculateOffsetDirection() {
        float offset = 2;
        float borderMargin = 0;

        if (!mScaleImageOut && mRightSide <= mDrawImage.getWidth() && mMoveRight) {
            moveImageRight(offset);
        } else if (!mScaleImageOut && mLeftSide > borderMargin && mMoveLeft) {
            moveImageLeft(offset);
        } else if (mLeftSide <= borderMargin) {
            mMoveRight = true;
        } else if (mRightSide >= mDrawImage.getWidth()) {
            mMoveLeft = true;
        }

        if (mLastZoomIn) {
            zoomImageIn(offset);
        } else if (mScaleImageOut) {
            zoomImageOut(offset);
        }
    }

    private void moveImageRight(float offset) {
        mMoveLeft = false;

        if (mLeftSide < mBaseLeftSide) {
            mLeftSide = mLeftSide + offset;

            if (mLeftSide == mBaseLeftSide) {
                mScaleImageOut = true;
            }

        } else {
            mRightSide = mRightSide + offset;
        }
    }

    private void moveImageLeft(float offset) {
        mMoveRight = false;

        if (mRightSide > mBaseRightSide) {
            mRightSide = mRightSide - offset;
        } else {
            mLeftSide = mLeftSide - offset;
        }
    }

    private void zoomImageOut(float offset) {
        mTopSide = mTopSide - offset;
        mBottomSide = mBottomSide + offset;

        if (mTopSide < mBaseTopSide) {
            mTopSide = mTopSide - offset;
        }
        if (mBottomSide > mBaseBottom) {
            mBottomSide = mBottomSide + offset;
        }

        if (mTopSide >= 0) {
            mScaleImageOut = false;
            mLastZoomIn = true;
        }
    }

    private void zoomImageIn(float offset) {
        if (mTopSide < mBaseTopSide) {
            mTopSide = mTopSide + offset;
        }
        if (mBottomSide > mBaseBottom) {
            mBottomSide = mBottomSide - offset;
        }

        if (mTopSide == mBaseTopSide) {
            mLastZoomIn = false;
        }
    }

    @NonNull
    private Matrix getMatrix() {
        RectF rectImage = new RectF(mLeftSide, mTopSide, mRightSide, mBottomSide);
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

    public static class DrawBitmap {

    }
}

