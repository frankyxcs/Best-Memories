package com.best.memories.di.components;

import com.best.memories.di.module.BestMemoriesModule;
import com.best.memories.service.BestMemoriesWallpaperService;

import javax.inject.Singleton;

import dagger.Component;

/**
 * A component with whole lifetime is the life of the application
 */
@Singleton
@Component(modules = BestMemoriesModule.class)
public interface BestMemoriesAppComponent {
    void inject(BestMemoriesWallpaperService service);
}
