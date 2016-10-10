package com.best.memories.application;

import android.app.Application;

import com.best.memories.di.components.BestMemoriesAppComponent;
import com.best.memories.di.components.DaggerBestMemoriesAppComponent;
import com.best.memories.di.module.BestMemoriesModule;

/**
 * Android main application
 */
public class BestMemoriesApplication extends Application {
    public static String TAG = "com.best.memories";
    private BestMemoriesAppComponent mBestMemoriesComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        initializeInjector();
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
