package com.best.memories.mvp.view.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.best.memories.R;
import com.best.memories.service.BestMemoriesWallpaperService;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER;
import static android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btSetWallpaper)
    public void clickWallpaper() {
        Intent intent = new Intent(ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, BestMemoriesWallpaperService.class));
        startActivity(intent);
    }

}
