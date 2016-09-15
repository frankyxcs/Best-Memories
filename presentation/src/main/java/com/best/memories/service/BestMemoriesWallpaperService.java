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
import android.view.SurfaceHolder;

import com.best.memories.R;

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
        private Bitmap bitmap;
        private MyRunnableImage drawRunner;
        private int centerBitmapY;
        private Bitmap bitmapNew;
        private float leftSide;
        private float topSide;
        private float rightSide;
        private float bottomSide;
        public static final int DELAY_MILLIS = 5;
        private final Handler handlerDrawBitmap = new Handler();
        private final Handler handlerAnimateDrawable = new Handler();
        private TypedArray list = null;
        private int screenHeight;
        private int screenWidth;
        private int centerBitmapX;

        private boolean moveLeft;
        private boolean moveRight = true;
        private boolean mLastZoomIn;
        private float baseLeftSide;
        private float baseTopSide;
        private float baseRightSide;
        private float baseBottom;
        private int alpha = 100;

        public BestMemoriesWallpaperEngine() {
            list = getResources().obtainTypedArray(R.array.image);
//
//             centerBitmapX = bitmap.getWidth() / 2;
//            centerBitmapY = bitmap.getHeight() / 2;
//
//            drawRunner = new MyRunnableImage(bitmap);
//
//            handlerDrawBitmap.post(drawRunner);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {

            super.onSurfaceCreated(holder);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                handlerDrawBitmap.postDelayed(drawRunner, DELAY_MILLIS);
            } else {
                handlerDrawBitmap.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            screenWidth = width;
            screenHeight = height;

            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);

            int outWidth = bitmap.getWidth();
            int outHeight = bitmap.getHeight();

            if (screenWidth > bitmap.getWidth()) {
                outWidth = outWidth + screenWidth / 2;
                outHeight = outHeight + screenHeight / 2;
            } else if (screenHeight > bitmap.getHeight()) {
                outHeight = outHeight + screenHeight / 2;
            }

            bitmapNew = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, true);

            centerBitmapX = bitmapNew.getWidth() / 2;
            centerBitmapY = bitmapNew.getHeight() / 2;

            drawRunner = new MyRunnableImage(bitmapNew);

            handlerDrawBitmap.post(drawRunner);

            calculateRectanglePoints();
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

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            handlerDrawBitmap.removeCallbacks(drawRunner);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            handlerDrawBitmap.removeCallbacks(drawRunner);
        }

        private class MyRunnableImage implements Runnable {
            private Bitmap mImage;
            private boolean mScaleImage;


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

                    float offset = 2;
                    float borderMargin = 0;

                    if (!mScaleImage && rightSide <= mImage.getWidth() && moveRight) {
                        moveImageRight(offset);
                    } else if (!mScaleImage && leftSide > borderMargin && moveLeft) {
                        moveImageLeft(offset);
                    } else if (leftSide <= borderMargin) {
                        moveRight = true;
                    } else if (rightSide >= mImage.getWidth()) {
                        moveLeft = true;
                    }

                    if (mLastZoomIn) {
                        zoomImageIn(offset);
                    } else if (mScaleImage) {
                        zoomImageOut(offset);
                    }

                    Matrix matrix = getMatrix();

                    Paint paint = new Paint();
                    paint.setAlpha(alpha);

                    canvas.drawBitmap(mImage, matrix, null);

                    if (isVisible()) {
                        handlerDrawBitmap.postDelayed(drawRunner, DELAY_MILLIS);
                    }
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }

            private void moveImageRight(float offset) {
                moveLeft = false;

                if (leftSide < baseLeftSide) {
                    leftSide = leftSide + offset;

                    if (leftSide == baseLeftSide) {
                        mScaleImage = true;
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

                if (topSide <= baseTopSide) {
                    mScaleImage = false;
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

        }

    }
}