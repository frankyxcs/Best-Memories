package com.best.memories.service;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.best.memories.mvp.SampleImages;
import com.best.memories.mvp.view.KenBurnsView;
import com.best.memories.mvp.view.LoopViewPager;

import java.util.Arrays;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Service for operate liveWallpaper
 */
public class BestMemoriesWallpaperService extends WallpaperService {
    private boolean mVisible;

    @Override
    public Engine onCreateEngine() {
        return new BestMemoriesWallpaperEngine();
    }

    private class BestMemoriesWallpaperEngine extends Engine {
        private final Handler handler = new Handler();

        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                drawImage();
            }
        };

        public BestMemoriesWallpaperEngine() {
            handler.post(drawRunner);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            mVisible = visible;

            if (visible) {
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
       }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            handler.removeCallbacks(drawRunner);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mVisible = false;
            handler.removeCallbacks(drawRunner);
        }

        private void drawImage() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();

                if (canvas != null) {
                    initializeKenBurnsView(canvas);
                }

            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas);
            }
        }

        private void initializeKenBurnsView(final Canvas canvas) {
            final KenBurnsView kenBurnsView = new KenBurnsView(getApplicationContext());
            kenBurnsView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            kenBurnsView.setSwapMs(3750);
            kenBurnsView.setFadeInOutMs(750);

            List<Integer> resourceIDs = Arrays.asList(SampleImages.IMAGES_RESOURCE);
            kenBurnsView.loadResourceIDs(resourceIDs);

            LoopViewPager.LoopViewPagerListener listener = new LoopViewPager.LoopViewPagerListener() {
                @Override
                public View OnInstantiateItem(int page) {
                    return new TextView(getApplicationContext());
                }

                @Override
                public void onPageScroll(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    kenBurnsView.forceSelected(position);
                //    BitmapDrawable drawable = (BitmapDrawable) kenBurnsView.getBackground();
                 //   canvas.drawBitmap(drawable.getBitmap(), MATCH_PARENT, MATCH_PARENT, null);
                }

                @Override
                public void onPageScrollChanged(int page) {
                }
            };

            LoopViewPager loopViewPager = new LoopViewPager(getApplicationContext(), resourceIDs.size(), listener);

            FrameLayout viewPagerFrame = new FrameLayout(getApplicationContext());
            viewPagerFrame.addView(loopViewPager);

            kenBurnsView.setPager(loopViewPager);


        }

    }
}
