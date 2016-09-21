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

import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Matrix.ScaleToFit.CENTER;
import static android.graphics.PorterDuff.Mode.CLEAR;

/**
 * Service for operate liveWallpaper
 */
public class BestMemoriesWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new BestMemoriesWallpaperEngine();
    }

    private class BestMemoriesWallpaperEngine extends Engine {
        public static final int DELAY_MILLIS = 5;
        public static final int UPDATE_OPACITY_SECOND = 2 * 1000;
        private static final long TIME_UPDATE_BITMAP = 10 * 1000;

        private boolean moveLeft;
        private boolean moveRight = true;
        private boolean mLastZoomIn;

        private int screenHeight;
        private int screenWidth;
        private int centerBitmapX;
        private int mImagePosition = 1;



        private Bitmap bitmap;
        private Bitmap mTransparentBitmap;
        private MyRunnableImage mDrawBitmap;
        private int centerBitmapY;
        private Bitmap bitmapNew;

        private final Handler handlerDrawBitmap = new Handler();
        private final Handler handlerUpdateBitmap = new Handler();
        private final Handler handlerBitmapOpacity = new Handler();
        private final Handler handlerUpdateOpacity = new Handler();

        private TypedArray listBitmaps = null;

        public BestMemoriesWallpaperEngine() {
            listBitmaps = getResources().obtainTypedArray(R.array.image);

            int resId = listBitmaps.getResourceId(0, -1);
            bitmap = BitmapFactory.decodeResource(getResources(), resId);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                handlerDrawBitmap.postDelayed(mDrawBitmap, DELAY_MILLIS);
                handlerUpdateBitmap.postDelayed(mUpdateImage, TIME_UPDATE_BITMAP);
                handlerBitmapOpacity.postDelayed(mBitmapOpacity, TIME_UPDATE_BITMAP - UPDATE_OPACITY_SECOND);
            } else {
                handlerUpdateBitmap.removeCallbacks(mBitmapOpacity);
                handlerDrawBitmap.removeCallbacks(mDrawBitmap);
                handlerUpdateBitmap.removeCallbacks(mUpdateImage);
            //    handlerUpdateOpacity.removeCallbacks(mUpdateOpacity);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            screenWidth = width;
            screenHeight = height;

            calculateScaledBitmapSize();
//            calculateRectanglePoints();

            mDrawBitmap = new MyRunnableImage(bitmapNew);

            handlerDrawBitmap.post(mDrawBitmap);
            handlerUpdateBitmap.post(mUpdateImage);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);

            handlerDrawBitmap.removeCallbacks(mDrawBitmap);
            handlerUpdateBitmap.removeCallbacks(mUpdateImage);
            handlerUpdateBitmap.removeCallbacks(mBitmapOpacity);
        //    handlerUpdateOpacity.removeCallbacks(mUpdateOpacity);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            handlerDrawBitmap.removeCallbacks(mDrawBitmap);
            handlerUpdateBitmap.removeCallbacks(mUpdateImage);
            handlerUpdateBitmap.removeCallbacks(mBitmapOpacity);
        //    handlerUpdateOpacity.removeCallbacks(mUpdateOpacity);
        }

        private void calculateScaledBitmapSize() {
            int outWidth = bitmap.getWidth();
            int outHeight = bitmap.getHeight();

            if (screenWidth > bitmap.getWidth()) {
                outWidth = screenWidth * 2;
                outHeight = screenHeight * 2;
            } else if (screenHeight > bitmap.getHeight()) {
                outHeight = screenHeight * 2;
            }

            bitmapNew = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, true);

            centerBitmapX = bitmapNew.getWidth() / 2;
            centerBitmapY = bitmapNew.getHeight() / 2;
        }

        public Bitmap makeBitmapTransparent(Bitmap src, int value) {
            int width = src.getWidth();
            int height = src.getHeight();
            Bitmap transBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(transBitmap);
            canvas.drawARGB(0, 0, 0, 0);
            // config paint
            final Paint paint = new Paint();
            paint.setAlpha(value);
            canvas.drawBitmap(src, 0, 0, paint);

            return transBitmap;
        }

        private class RunableUpdateOpacity implements Runnable {
            private int opacity;
            private Bitmap bitmap;

            public RunableUpdateOpacity(int opacity, Bitmap bitmap) {
                this.opacity = opacity;
                this.bitmap = bitmap;
            }

            @Override
            public void run() {
                mTransparentBitmap = makeBitmapTransparent(bitmap, opacity);
            }
        }

        private Runnable mBitmapOpacity = new Runnable() {
            @Override
            public void run() {
                int localImage = mImagePosition + 1;
                int resId = listBitmaps.getResourceId(localImage, -1);

                Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resId);
                int opacity = 70;
                mTransparentBitmap = makeBitmapTransparent(imageBitmap, opacity);
            }
        };

        private Runnable mUpdateImage = new Runnable() {
            @Override
            public void run() {
                int localImage = mImagePosition;
                int resId = listBitmaps.getResourceId(localImage, -1);

                bitmap = BitmapFactory.decodeResource(getResources(), resId);

                mDrawBitmap.updateBitmap(bitmap);

                calculateScaledBitmapSize();
            //    calculateRectanglePoints();

                mDrawBitmap = new MyRunnableImage(bitmapNew);
                handlerDrawBitmap.post(mDrawBitmap);

                if (mImagePosition < listBitmaps.length() - 1) {
                    mImagePosition++;
                } else {
                    mImagePosition = 0;
                }

                handlerBitmapOpacity.postDelayed(mBitmapOpacity, TIME_UPDATE_BITMAP - UPDATE_OPACITY_SECOND);
                handlerUpdateBitmap.postDelayed(mUpdateImage, TIME_UPDATE_BITMAP);
            }
        };

        private class MyRunnableImage implements Runnable {
            private Bitmap mImage;
            private boolean mScaleImageOut;

            private float baseLeftSide;
            private float baseTopSide;
            private float baseRightSide;
            private float baseBottom;

            private float leftSide;
            private float topSide;
            private float rightSide;
            private float bottomSide;

            public MyRunnableImage(Bitmap bitmap) {
                mImage = bitmap;
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

                    canvas.drawBitmap(mImage, matrix, paint);
                    canvas.drawBitmap(mTransparentBitmap, 100, 100f, null);

                    if (isVisible()) {
                        handlerDrawBitmap.postDelayed(mDrawBitmap, DELAY_MILLIS);
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

            private void calculateRectanglePoints() {
                leftSide = Math.abs(centerBitmapX - screenWidth / 2);
                topSide = Math.abs(centerBitmapY - screenHeight / 2);
                rightSide = Math.abs(centerBitmapX + screenWidth / 2);
                bottomSide = Math.abs(centerBitmapY + screenHeight / 2);

                baseLeftSide = leftSide;
                baseTopSide = topSide;
                baseRightSide = rightSide;
                baseBottom = bottomSide;
            }

            private void calculateOffsetDirection() {
                float offset = 2;
                float borderMargin = 0;

                if (!mScaleImageOut && rightSide <= mImage.getWidth() && moveRight) {
                    moveImageRight(offset);
                } else if (!mScaleImageOut && leftSide > borderMargin && moveLeft) {
                    moveImageLeft(offset);
                } else if (leftSide <= borderMargin) {
                    moveRight = true;
                } else if (rightSide >= mImage.getWidth()) {
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
                RectF rectScreen = new RectF(left, top, screenWidth, screenHeight);

                Matrix matrix = new Matrix();
                matrix.setRectToRect(rectImage, rectScreen, CENTER);
                return matrix;
            }

            public void updateBitmap(Bitmap bitmap) {
                mImage = bitmap;
            }
        }

    }
}