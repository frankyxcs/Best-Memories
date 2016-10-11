package com.best.memories.service;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore.Images.Media;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import com.best.memories.application.BestMemoriesApplication;
import com.best.memories.background.RunnableDrawImage;
import com.best.memories.background.RunnableDrawImage.IDrawBitmap;
import com.bestmemories.GlobalSharePreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import static com.best.memories.application.BestMemoriesApplication.TAG;
import static com.bestmemories.GlobalSharePreferences.BITMAP_POSITION;
import static com.bestmemories.GlobalSharePreferences.IMAGE_URIS;
import static com.bestmemories.GlobalSharePreferences.SHARE_PREFERENCES_TYPE.INTEGER;

/**
 * Service for operate liveWallpaper
 */
public class BestMemoriesWallpaperService extends WallpaperService {
    public static final int DEF_VALUE = 0;
    @Inject GlobalSharePreferences mPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        BestMemoriesApplication application = (BestMemoriesApplication) getApplication();
        application.getBestMemoriesComponent().inject(this);
    }

    @Override
    public Engine onCreateEngine() {
        return new BestMemoriesWallpaperEngine();
    }

    private class BestMemoriesWallpaperEngine extends Engine implements IDrawBitmap {
        static final int DELAY_MILLIS = 1;
        static final int UPDATE_OPACITY_SECOND = 2 * 1000;
        private static final long TIME_UPDATE_BITMAP = 30 * 1000;

        private final Handler mHandlerDrawBitmap = new Handler();
        private final Handler mHandlerUpdateBitmap = new Handler();
        private final Handler mHandlerUpdateOpacity = new Handler();

        private RunnableDrawImage mRunnableDrawBitmap;
        private RunnableUpdateOpacity mUpdateBitmapOpacity;
        private List<Bitmap> mArrayBitmaps = new ArrayList<>();

        BestMemoriesWallpaperEngine() {
            HashSet<String> hashSet = (HashSet<String>) mPreferences.getSharedPreferences().getStringSet(IMAGE_URIS, null);

            if (hashSet != null) {
                for (String path : hashSet) {
                    Uri uri = Uri.parse(path);
                    try {
                        Bitmap bitmap = Media.getBitmap(getContentResolver(), uri);
                        mArrayBitmaps.add(bitmap);
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            int position = mPreferences.getSharedPreferences().getInt(BITMAP_POSITION, DEF_VALUE);

            Bitmap bitmap = mArrayBitmaps.get(position);

            mRunnableDrawBitmap = new RunnableDrawImage(getSurfaceHolder());
            mRunnableDrawBitmap.setListener(this);
            mRunnableDrawBitmap.resetStates();

            mRunnableDrawBitmap.updateScreenResolution(width, height);
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
            @Override
            public void run() {
                int position = mPreferences.getSharedPreferences().getInt(BITMAP_POSITION, DEF_VALUE);

                if (position < mArrayBitmaps.size() - 1) {
                    position++;
                } else {
                    position = 0;
                }

                mPreferences.setDataToSharePreferences(BITMAP_POSITION, position, INTEGER);

                Bitmap bitmap = mArrayBitmaps.get(position);
                bitmap = mRunnableDrawBitmap.calculateScaledBitmapSize(bitmap);

                mUpdateBitmapOpacity = new RunnableUpdateOpacity(bitmap);
                mHandlerUpdateOpacity.post(mUpdateBitmapOpacity);
                mHandlerUpdateBitmap.postDelayed(mRunnableUpdateImage, TIME_UPDATE_BITMAP - UPDATE_OPACITY_SECOND);
            }
        };

        private class RunnableUpdateOpacity implements Runnable {
            private int opacity;
            private Bitmap sourceBitmap;

            private static final int OPACITY_LIMIT = 250;

            RunnableUpdateOpacity(Bitmap bitmap) {
                sourceBitmap = bitmap;
            }

            @Override
            public void run() {
                int opacityOffset = 10;
                if (opacity < OPACITY_LIMIT) {
                    opacity = opacity + opacityOffset;

                    mRunnableDrawBitmap.updateBackgroundBitmap(sourceBitmap);
                    mRunnableDrawBitmap.setShowBackgroundBitmap(true);
                    mRunnableDrawBitmap.setForwardBitmapOpacity(opacity);

                    mHandlerUpdateOpacity.postDelayed(mUpdateBitmapOpacity, (TIME_UPDATE_BITMAP - UPDATE_OPACITY_SECOND) / OPACITY_LIMIT);
                } else {
                    mHandlerUpdateOpacity.removeCallbacks(mUpdateBitmapOpacity);

                    mRunnableDrawBitmap.resetStates();
                    mRunnableDrawBitmap.setShowBackgroundBitmap(false);

                    mRunnableDrawBitmap.calculateRectanglePoints(sourceBitmap);
                    mRunnableDrawBitmap.updateBitmap(sourceBitmap);
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