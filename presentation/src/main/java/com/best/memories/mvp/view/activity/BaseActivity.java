package com.best.memories.mvp.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.best.memories.application.BestMemoriesApplication;
import com.best.memories.di.components.ActivityComponent;
import com.best.memories.di.components.BestMemoriesAppComponent;
import com.best.memories.di.module.ActivityModule;

/**
 * Activity with main logic
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setComponent();
    }

    public abstract void setComponent();

    /**
     * Get the Main Application component for dependency injection.
     *
     * @return {@link BestMemoriesAppComponent}
     */
    protected BestMemoriesAppComponent getApplicationComponent() {
        return ((BestMemoriesApplication) getApplication()).getBestMemoriesComponent();
    }

    /**
     * Get an Activity module for dependency injection.
     *
     * @return {@link ActivityModule}
     */
    protected ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }


    protected ActivityComponent getActivityComponent() {
        return getApplicationComponent().plus(getActivityModule());

    }
}
