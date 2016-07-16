package com.best.memories.di.module;

import com.best.memories.application.BestMemoriesApplication;

import dagger.Module;

/**
 * Dagger module that provides objects which will live during the application lifecycle.
 */
@Module
public class BestMemoriesModule {
    private final BestMemoriesApplication mApplication;

    public BestMemoriesModule(BestMemoriesApplication bestMemoriesApplication) {
        mApplication = bestMemoriesApplication;
    }
}
