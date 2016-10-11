package com.best.memories.application;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.best.memories.di.components.BestMemoriesAppComponent;
import com.best.memories.di.components.DaggerBestMemoriesAppComponent;
import com.best.memories.di.module.BestMemoriesModule;

import java.io.File;

/**
 * Android main application
 */
public class BestMemoriesApplication extends Application {
    public static final String BEST_MEMORIES = "Best Memories";
    public static String TAG = "com.best.memories";
    private BestMemoriesAppComponent mBestMemoriesComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        initializeInjector();
        createBestMemoriesFolder();
    }

    private void createBestMemoriesFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + BEST_MEMORIES);
        boolean dirState = folder.mkdir();

        if (dirState) {
            Log.i(TAG, " directory created");
        }
    }

    private void initializeInjector() {
        mBestMemoriesComponent = DaggerBestMemoriesAppComponent.builder()
                .bestMemoriesModule(new BestMemoriesModule(this))
                .build();
    }


    public BestMemoriesAppComponent getBestMemoriesComponent() {
        return mBestMemoriesComponent;
    }

}
