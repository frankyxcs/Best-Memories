package com.best.memories.service;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.best.memories.R;
import com.best.memories.background.RunnableDrawImage;
import com.best.memories.background.RunnableDrawImage.IDrawBitmap;

import java.util.ArrayList;
import java.util.List;

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
        private static final long TIME_UPDATE_BITMAP = 20 * 1000;

        private final Handler mHandlerDrawBitmap = new Handler();
        private final Handler mHandlerUpdateBitmap = new Handler();
        private final Handler mHandlerUpdateOpacity = new Handler();

        private RunnableDrawImage mRunnableDrawBitmap;
        private RunnableUpdateOpacity mUpdateBitmapOpacity;
        private List<Bitmap> mArrayBitmaps = new ArrayList<>();

        BestMemoriesWallpaperEngine() {
            TypedArray typedArray = getResources().obtainTypedArray(R.array.image);
            int defaultValue = -1;

            for (int i = 0; i < typedArray.length(); i++) {
                int resId = typedArray.getResourceId(i, defaultValue);
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

            int defPosition = 0;
            Bitmap bitmap = mArrayBitmaps.get(defPosition);

            mRunnableDrawBitmap.resetStates();

            Bitmap scaledBitmap = mRunnableDrawBitmap.calculateScaledBitmapSize(bitmap);
            mRunnableDrawBitmap.calculateRectanglePoints(scaledBitmap);
            mRunnableDrawBitmap.updateBitmap(scaledBitmap);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
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
            private int imagePosition;

            @Override
            public void run() {

                if (imagePosition < mArrayBitmaps.size() - 1) {
                    imagePosition++;
                } else {
                    imagePosition = 0;
                }

                Bitmap bitmap = mArrayBitmaps.get(imagePosition);

                mUpdateBitmapOpacity = new RunnableUpdateOpacity(bitmap);
                mHandlerUpdateOpacity.post(mUpdateBitmapOpacity);
                mHandlerUpdateBitmap.postDelayed(mRunnableUpdateImage, TIME_UPDATE_BITMAP - UPDATE_OPACITY_SECOND);
            }
        };

        private class RunnableUpdateOpacity implements Runnable {
            private int opacity;
            private Bitmap sourceBitmap;
            private Bitmap scaledBitmap;
            private static final int OPACITY_LIMIT = 250;

            RunnableUpdateOpacity(Bitmap bitmap) {
                this.sourceBitmap = bitmap;
            }

            @Override
            public void run() {
                int opacityOffset = 15;
                if (opacity < OPACITY_LIMIT) {
                    opacity = opacity + opacityOffset;

                    scaledBitmap = mRunnableDrawBitmap.calculateScaledBitmapSize(sourceBitmap);

                    mRunnableDrawBitmap.updateBackgroundBitmap(scaledBitmap);
                    mRunnableDrawBitmap.setShowBackgroundBitmap(true);
                    mRunnableDrawBitmap.setForwardBitmapOpacity(opacity);

                    mHandlerUpdateOpacity.postDelayed(mUpdateBitmapOpacity, (TIME_UPDATE_BITMAP - UPDATE_OPACITY_SECOND) / OPACITY_LIMIT);
                } else {
                    mHandlerUpdateOpacity.removeCallbacks(mUpdateBitmapOpacity);

                    mRunnableDrawBitmap.resetStates();
                    mRunnableDrawBitmap.setShowBackgroundBitmap(false);

                    mRunnableDrawBitmap.calculateRectanglePoints(scaledBitmap);
                    mRunnableDrawBitmap.updateBitmap(scaledBitmap);
                }
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