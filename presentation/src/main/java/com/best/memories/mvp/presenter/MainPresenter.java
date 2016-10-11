package com.best.memories.mvp.presenter;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.best.memories.di.annotation.PerActivity;
import com.best.memories.mvp.view.activity.MainActivity;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

/**
 * Created by Terry on 10/11/2016.
 */

@PerActivity
public class MainPresenter {

    private MainActivity mView;

    @Inject
    public MainPresenter(Context context) {

    }

    public void setView(MainActivity view) {
        mView = view;
    }

    public void destroy() {
        mView = null;
    }

    public void parseOnActivityResult(Intent data) {

        ClipData clipData = data.getClipData();

        Set<String> setImageUris = new HashSet<>();

        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();
                setImageUris.add(uri.toString());
            }
        } else {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = mView.getContentResolver().query(selectedImage, filePathColumn, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                setImageUris.add(picturePath);
            }
        }
        mView.runWallpaperService(setImageUris);

    }

}
