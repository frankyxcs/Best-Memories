package com.best.memories.mvp.view.activity;

import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;

import com.best.memories.R;
import com.best.memories.di.HasComponent;
import com.best.memories.di.components.ActivityComponent;
import com.best.memories.mvp.presenter.MainPresenter;
import com.best.memories.service.BestMemoriesWallpaperService;
import com.bestmemories.GlobalSharePreferences;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER;
import static android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT;
import static android.content.Intent.ACTION_GET_CONTENT;
import static android.content.Intent.EXTRA_ALLOW_MULTIPLE;
import static com.bestmemories.GlobalSharePreferences.IMAGE_URIS;
import static com.bestmemories.GlobalSharePreferences.SHARE_PREFERENCES_TYPE.SET;

public class MainActivity extends BaseActivity implements HasComponent<ActivityComponent> {

    public static final int REQUEST_CODE_GALLERY = 0;
    public static final String IMAGE_TYPE = "image/*";

    @Inject GlobalSharePreferences mPreferences;
    @Inject MainPresenter mPresenter;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPresenter.setView(this);
    }

    @Override
    public void setComponent() {
        getActivityComponent().inject(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    @OnClick(R.id.bt_gallery)
    public void choiceFromGallery() {
        Intent intent = new Intent();
        intent.setType(IMAGE_TYPE);
        intent.putExtra(EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.gallery_title)), REQUEST_CODE_GALLERY);
    }

    @OnClick(R.id.bt_social_network)
    public void choiceFromSocialNetwork() {
        Intent intent = new Intent(ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, BestMemoriesWallpaperService.class));
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_GALLERY) {
            mPresenter.parseOnActivityResult(data);
        }
    }

    @Override
    public ActivityComponent getComponent() {
        return null;
    }

    public void runWallpaperService(Set<String> setImageUris) {
        mPreferences.setDataToSharePreferences(IMAGE_URIS, setImageUris, SET);

        Intent intent = new Intent(ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, BestMemoriesWallpaperService.class));
        startActivity(intent);
    }
}
