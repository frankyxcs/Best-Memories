package com.best.memories.service;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;

import com.best.memories.R;
import com.best.memories.application.BestMemoriesApplication;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Matrix.ScaleToFit.CENTER;
import static android.graphics.PorterDuff.Mode.CLEAR;

/**
 * Service for operate liveWallpaper
 */
public class BestMemoriesWallpaperService extends WallpaperService {

    public static final int OPACITY_LIMIT = 100;

    @Override
    public Engine onCreateEngine() {
        return new BestMemoriesWallpaperEngine();
    }

    private class BestMemoriesWallpaperEngine extends Engine {
        public static final int DELAY_MILLIS = 5;
        public static final int UPDATE_OPACITY_SECOND = 2 * 1000;
        private static final long TIME_UPDATE_BITMAP = 10 * 1000;

        private boolean mShowBackgroundBitmap;

        private int mImagePosition = 1;
        private int mScreenHeight;
        private int mScreenWidth;

        private final Handler mHandlerDrawBitmap = new Handler();
        private final Handler mHandlerUpdateBitmap = new Handler();
        private final Handler mHandlerUpdateOpacity = new Handler();

        private Bitmap mTargetBitmap;
        private Bitmap mBackgroundBitmap;

        private DrawRunnableImage mRunnableDrawBitmap;
        private RunnableUpdateOpacity mUpdateBitmapOpacity;
        private TypedArray mListBitmaps = null;

        public BestMemoriesWallpaperEngine() {
            mListBitmaps = getResources().obtainTypedArray(R.array.image);

            int resId = mListBitmaps.getResourceId(0, -1);
            mTargetBitmap = BitmapFactory.decodeResource(getResources(), resId);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                mHandlerDrawBitmap.postDelayed(mRunnableDrawBitmap, DELAY_MILLIS);
                mHandlerUpdateOpacity.postDelayed(mUpdateBitmapOpacity, TIME_UPDATE_BITMAP);
            } else {
                mHandlerDrawBitmap.removeCallbacks(mRunnableDrawBitmap);
                mHandlerUpdateOpacity.removeCallbacks(mUpdateBitmapOpacity);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mScreenWidth = width;
            mScreenHeight = height;

            mRunnableDrawBitmap = new DrawRunnableImage(mTargetBitmap);

            mRunnableDrawBitmap.calculateScaledBitmapSize();
            mRunnableDrawBitmap.calculateRectanglePoints();

            mHandlerDrawBitmap.post(mRunnableDrawBitmap);
            mHandlerUpdateBitmap.post(mRunnableUpdateImage);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);

            mHandlerDrawBitmap.removeCallbacks(mRunnableDrawBitmap);
            mHandlerUpdateBitmap.removeCallbacks(mRunnableUpdateImage);
            mHandlerUpdateOpacity.removeCallbacks(mUpdateBitmapOpacity);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            mHandlerDrawBitmap.removeCallbacks(mRunnableDrawBitmap);
            mHandlerUpdateBitmap.removeCallbacks(mRunnableUpdateImage);
            mHandlerUpdateOpacity.removeCallbacks(mUpdateBitmapOpacity);
        }

        private class RunnableUpdateOpacity implements Runnable {
            private int opacity;
            private Bitmap bitmap;

            public RunnableUpdateOpacity(Bitmap bitmap) {
                this.bitmap = bitmap;
            }

            @Override
            public void run() {
                if (opacity < OPACITY_LIMIT) {
                    opacity = opacity + 1;

                    mBackgroundBitmap = makeBitmapTransparent(bitmap, opacity);

                    mHandlerUpdateOpacity.postDelayed(mUpdateBitmapOpacity, (TIME_UPDATE_BITMAP - UPDATE_OPACITY_SECOND) / 100);
                } else {
                    mShowBackgroundBitmap = false;
                    mHandlerUpdateOpacity.removeCallbacks(mUpdateBitmapOpacity);

                    mTargetBitmap = mBackgroundBitmap;
                    mRunnableDrawBitmap.updateBitmap(mTargetBitmap);

                    mRunnableDrawBitmap.calculateScaledBitmapSize();
                    mRunnableDrawBitmap.calculateRectanglePoints();
                }
            }

            private Bitmap makeBitmapTransparent(Bitmap src, int value) {
                int width = src.getWidth();
                int height = src.getHeight();

                Bitmap transBitmap = Bitmap.createBitmap(width, height, ARGB_8888);
                Canvas canvas = new Canvas(transBitmap);
                canvas.drawARGB(0, 0, 0, 0);

                final Paint paint = new Paint();
                paint.setAlpha(value);
                canvas.drawBitmap(src, 0, 0, paint);

                return transBitmap;
            }
        }

        private Runnable mRunnableUpdateImage = new Runnable() {
            @Override
            public void run() {
                int localImage = mImagePosition;
                int resId = mListBitmaps.getResourceId(localImage, -1);

                mTargetBitmap = BitmapFactory.decodeResource(getResources(), resId);

                if (mImagePosition < mListBitmaps.length() - 1) {
                    mImagePosition++;
                } else {
                    mImagePosition = 0;
                }

                if (isVisible()) {
                    mHandlerDrawBitmap.post(mRunnableDrawBitmap);
                    mHandlerUpdateBitmap.postDelayed(mRunnableUpdateImage, TIME_UPDATE_BITMAP);
                }
            }
        };

        private class DrawRunnableImage implements Runnable {
            private boolean moveLeft;
            private boolean moveRight = true;
            private boolean mScaleImageOut;
            private boolean mLastZoomIn;

            private int centerBitmapX;
            private int centerBitmapY;

            private float baseLeftSide;
            private float baseTopSide;
            private float baseRightSide;
            private float baseBottom;

            private float leftSide;
            private float topSide;
            private float rightSide;
            private float bottomSide;

            private Bitmap mDrawImage;

            public DrawRunnableImage(Bitmap bitmap) {
                mDrawImage = bitmap;
            }

            @Override
            public void run() {
                Canvas canvas = null;
                SurfaceHolder holder = getSurfaceHolder();

                try {
                    canvas = holder.lockCanvas();
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

                    if (isVisible()) {
                        mHandlerDrawBitmap.postDelayed(mRunnableDrawBitmap, DELAY_MILLIS);
                    }
                } finally {
                    try {
                        if (canvas != null) {
                            holder.unlockCanvasAndPost(canvas);
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

                centerBitmapX = mDrawImage.getWidth() / 2;
                centerBitmapY = mDrawImage.getHeight() / 2;
            }

            public void calculateRectanglePoints() {
                leftSide = Math.abs(centerBitmapX - mScreenWidth / 2);
                topSide = Math.abs(centerBitmapY - mScreenHeight / 2);
                rightSide = Math.abs(centerBitmapX + mScreenWidth / 2);
                bottomSide = Math.abs(centerBitmapY + mScreenHeight / 2);

                baseLeftSide = leftSide;
                baseTopSide = topSide;
                baseRightSide = rightSide;
                baseBottom = bottomSide;
            }

            private void calculateOffsetDirection() {
                float offset = 2;
                float borderMargin = 0;

                if (!mScaleImageOut && rightSide <= mDrawImage.getWidth() && moveRight) {
                    moveImageRight(offset);
                } else if (!mScaleImageOut && leftSide > borderMargin && moveLeft) {
                    moveImageLeft(offset);
                } else if (leftSide <= borderMargin) {
                    moveRight = true;
                } else if (rightSide >= mDrawImage.getWidth()) {
                    moveLeft = true;
                }

                if (mLastZoomIn) {
                    zoomImageIn(offset);
                } else if (mScaleImageOut) {
                    zoomImageOut(offset);
                }
            }

            private void moveImageRight(float offset) {
                moveLeft = false;

                if (leftSide < baseLeftSide) {
                    leftSide = leftSide + offset;

                    if (leftSide == baseLeftSide) {
                        mScaleImageOut = true;
                    }

                } else {
                    rightSide = rightSide + offset;
                }
            }

            private void moveImageLeft(float offset) {
                moveRight = false;

                if (rightSide > baseRightSide) {
                    rightSide = rightSide - offset;
                } else {
                    leftSide = leftSide - offset;
                }
            }

            private void zoomImageOut(float offset) {
                topSide = topSide - offset;
                bottomSide = bottomSide + offset;

                if (topSide < baseTopSide) {
                    topSide = topSide - offset;
                }
                if (bottomSide > baseBottom) {
                    bottomSide = bottomSide + offset;
                }

                if (topSide >= 0) {
                    mScaleImageOut = false;
                    mLastZoomIn = true;
                }
            }

            private void zoomImageIn(float offset) {
                if (topSide < baseTopSide) {
                    topSide = topSide + offset;
                }
                if (bottomSide > baseBottom) {
                    bottomSide = bottomSide - offset;
                }

                if (topSide == baseTopSide) {
                    mLastZoomIn = false;
                }
            }

            @NonNull
            private Matrix getMatrix() {
                RectF rectImage = new RectF(leftSide, topSide, rightSide, bottomSide);
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
        }

    }
}