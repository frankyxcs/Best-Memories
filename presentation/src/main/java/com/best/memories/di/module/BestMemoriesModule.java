package com.best.memories.di.module;

import android.content.Context;

import com.best.memories.application.BestMemoriesApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module that provides objects which will live during the application lifecycle.
 */
@Module
public class BestMemoriesModule {
    private final BestMemoriesApplication mApplication;

    public BestMemoriesModule(BestMemoriesApplication bestMemoriesApplication) {
        mApplication = bestMemoriesApplication;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return mApplication;
    }

}
