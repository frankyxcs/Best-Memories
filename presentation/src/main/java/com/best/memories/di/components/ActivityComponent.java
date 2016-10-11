package com.best.memories.di.components;

import com.best.memories.di.annotation.PerActivity;
import com.best.memories.di.module.ActivityModule;
import com.best.memories.mvp.view.activity.MainActivity;

import dagger.Subcomponent;

/**
 * A base component upon which fragment's components may depend.
 * Activity-level components should extend this component.
 */
@PerActivity
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {
    void inject(MainActivity fragment);
}
