package com.best.memories.service;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import com.best.memories.R;
import com.best.memories.application.BestMemoriesApplication;

import java.util.Random;

import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Color.alpha;
import static android.graphics.Color.red;
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
        private final Bitmap bitmap;
        private final MyRunnableImage drawRunner;
        private final int centerBitmapY;
        private float left;
        private float top;
        private float right;
        private float bottom;
        public static final int DELAY_MILLIS = 5;
        private final Handler handlerDrawBitmap = new Handler();
        private final Handler handlerAnimateDrawable = new Handler();
        private TypedArray list = null;
        private int screenHeight;
        private int screenWidth;
        private int centerBitmapX;

        private boolean moveLeft;
        private boolean moveRight = true;
        private boolean mLastZoomOut;
        private float baseLeft;
        private float baseTop;
        private float baseRight;
        private float baseBottom;
        private int alpha = 100;

        public BestMemoriesWallpaperEngine() {
            list = getResources().obtainTypedArray(R.array.image);

            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
            centerBitmapX = bitmap.getWidth() / 2;
            centerBitmapY = bitmap.getHeight() / 2;

            drawRunner = new MyRunnableImage(bitmap);

            handlerDrawBitmap.post(drawRunner);
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
            calculateRectanglePoints();
        }

        private void calculateRectanglePoints() {
            left = Math.abs(centerBitmapX - screenWidth / 2);
            top = Math.abs(centerBitmapY - screenHeight / 2);
            right = Math.abs(centerBitmapX + screenWidth / 2);
            bottom = Math.abs(centerBitmapY + screenHeight / 2);

            baseLeft = left;
            baseTop = top;
            baseRight = right;
            baseBottom = bottom;
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

                    float offset = 1;
                    float borderMargin = 0;

                    if (!mScaleImage && right <= mImage.getWidth() && moveRight) {
                        moveLeft = false;

                        if (left < baseLeft) {
                            left = left + offset;
                        } else {
                            right = right + offset;
                        }
                    } else {
                        if (!mScaleImage && left > borderMargin && moveLeft) {
                            moveRight = false;

                            if (right > baseRight) {
                                right = right - offset;
                            } else {
                                left = left - offset;
                            }

                        } else if (left <= borderMargin) {
                            moveRight = true;
                        } else if (right >= mImage.getWidth()) {
                            moveLeft = true;
                        }
                    }

                    ///////////////////////////////////////////////////
                    if (moveRight && left == baseLeft - 1) {
                        mScaleImage = true;
                    }

                    if (mScaleImage && top > 0 && bottom < mImage.getHeight()) {
                        top = top - offset;
                        bottom = bottom + offset;

                        if (top == 1) {
                            mLastZoomOut = true;
                        }
                    } else if (mLastZoomOut) {
                        mScaleImage = false;

                        if (top == baseTop) {
                            mImage = BitmapFactory.decodeResource(getResources(), R.drawable.image_2);
                            mLastZoomOut = false;
                        }

                        if (top < baseTop) {
                            top = top + offset;
                        }
                        if (bottom > baseBottom) {
                            bottom = bottom - offset;
                        }
                    }


                    RectF rectImage = new RectF(left, top, right, bottom);
                    float left = 0;
                    float top = 0;
                    RectF rectScreen = new RectF(left, top, screenWidth, screenHeight);

                    Matrix matrix = new Matrix();
                    matrix.setRectToRect(rectImage, rectScreen, CENTER);

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
        }

    }
}