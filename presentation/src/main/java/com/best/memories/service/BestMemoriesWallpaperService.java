package com.best.memories.service;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.best.memories.R;
import com.best.memories.background.RunnableDrawImage;
import com.best.memories.background.RunnableDrawImage.IDrawBitmap;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;

/**
 * Service for operate liveWallpaper
 */
public class BestMemoriesWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new BestMemoriesWallpaperEngine();
    }

    private class BestMemoriesWallpaperEngine extends Engine implements IDrawBitmap {
        static final int DELAY_MILLIS = 5;
        static final int UPDATE_OPACITY_SECOND = 2 * 1000;
        private static final long TIME_UPDATE_BITMAP = 10 * 1000;

        private int mImagePosition = 0;

        private final Handler mHandlerDrawBitmap = new Handler();
        private final Handler mHandlerUpdateBitmap = new Handler();
        private final Handler mHandlerUpdateOpacity = new Handler();

        private RunnableDrawImage mRunnableDrawBitmap;
        private RunnableUpdateOpacity mUpdateBitmapOpacity;
        private List<Bitmap> mArrayBitmaps = new ArrayList<>();

        BestMemoriesWallpaperEngine() {
            TypedArray typedArray = getResources().obtainTypedArray(R.array.image);

            for (int i = 0; i < typedArray.length(); i++) {
                int resId = typedArray.getResourceId(i, -1);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
                mArrayBitmaps.add(bitmap);
            }
            typedArray.recycle();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            mRunnableDrawBitmap = new RunnableDrawImage(getSurfaceHolder(), width, height);
            mRunnableDrawBitmap.setListener(this);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {

                Bitmap bitmap = mArrayBitmaps.get(mImagePosition);

                mRunnableDrawBitmap.resetStates();
                mRunnableDrawBitmap.updateBitmap(bitmap);
                mRunnableDrawBitmap.calculateScaledBitmapSize();
                mRunnableDrawBitmap.calculateRectanglePoints();
                mHandlerDrawBitmap.post(mRunnableDrawBitmap);

                mHandlerUpdateBitmap.postDelayed(mRunnableUpdateImage, TIME_UPDATE_BITMAP - UPDATE_OPACITY_SECOND);
            } else {
                mHandlerDrawBitmap.removeCallbacks(mRunnableDrawBitmap);
                mHandlerUpdateBitmap.removeCallbacks(mRunnableUpdateImage);
                mHandlerUpdateOpacity.removeCallbacks(mUpdateBitmapOpacity);
            }
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

        private Runnable mRunnableUpdateImage = new Runnable() {
            @Override
            public void run() {

                if (mImagePosition < mArrayBitmaps.size() - 1) {
                    mImagePosition++;
                } else {
                    mImagePosition = 0;
                }

                Bitmap backgroundBitmap = mArrayBitmaps.get(mImagePosition);

//                    mRunnableDrawBitmap.setShowBackgroundBitmap(false);
                mRunnableDrawBitmap.resetStates();
                mRunnableDrawBitmap.updateBitmap(backgroundBitmap);
                mRunnableDrawBitmap.calculateScaledBitmapSize();
                mRunnableDrawBitmap.calculateRectanglePoints();

//                    mUpdateBitmapOpacity = new RunnableUpdateOpacity(backgroundBitmap);
//
//                    mHandlerUpdateOpacity.post(mUpdateBitmapOpacity);
                mHandlerUpdateBitmap.postDelayed(mRunnableUpdateImage, TIME_UPDATE_BITMAP - UPDATE_OPACITY_SECOND);
            }
        };

        private class RunnableUpdateOpacity implements Runnable {
            private int opacity;
            private Bitmap sourceBitmap;
            private static final int OPACITY_LIMIT = 100;

            RunnableUpdateOpacity(Bitmap bitmap) {
                this.sourceBitmap = bitmap;
            }

            @Override
            public void run() {
                int opacityOffset = 1;
                if (opacity < OPACITY_LIMIT) {
                    opacity = opacity + opacityOffset;

                    Bitmap bitmap = makeBitmapTransparent(sourceBitmap, opacity);

                    mRunnableDrawBitmap.setShowBackgroundBitmap(true);
                    mRunnableDrawBitmap.updateBackgroundBitmap(bitmap);
                    mHandlerUpdateOpacity.postDelayed(mUpdateBitmapOpacity, (TIME_UPDATE_BITMAP - UPDATE_OPACITY_SECOND) / 50);
                } else {
                    mHandlerUpdateOpacity.removeCallbacks(mUpdateBitmapOpacity);

                    mRunnableDrawBitmap.setShowBackgroundBitmap(false);
                    mRunnableDrawBitmap.updateBitmap(sourceBitmap);
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

        @Override
        public void drawBitmap() {
            if (isVisible()) {
                mHandlerDrawBitmap.postDelayed(mRunnableDrawBitmap, DELAY_MILLIS);
            }
        }
    }
}