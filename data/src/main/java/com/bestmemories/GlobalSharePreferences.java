package com.bestmemories;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import static android.content.Context.MODE_PRIVATE;

/**
 * SharePreferences for store data to local storage
 */
@Singleton
public class GlobalSharePreferences {
    private static final String BEST_MEMORIES_SHARED_PREFERENCES = "com.best.memories";
    public static final String BITMAP_POSITION = BEST_MEMORIES_SHARED_PREFERENCES + ".bitmap.position";
    public static final String IMAGE_URIS = BEST_MEMORIES_SHARED_PREFERENCES + ".image.uris";

    private Context mContext;

    @Inject
    public GlobalSharePreferences(Context context) {
        mContext = context;
    }

    public SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(BEST_MEMORIES_SHARED_PREFERENCES, MODE_PRIVATE);
    }

    @SuppressWarnings(value = "unchecked")
    public void setDataToSharePreferences(String key, Object value, SHARE_PREFERENCES_TYPE type) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();

        switch (type) {
            case STRING:
                String valueString = (String) value;
                editor.putString(key, valueString);
                break;

            case INTEGER:
                Integer valueInteger = (Integer) value;
                editor.putInt(key, valueInteger);
                break;

            case BOOLEAN:
                Boolean valueBoolean = (Boolean) value;
                editor.putBoolean(key, valueBoolean);
                break;

            case SET:
                Set<String> setImagePath = (Set<String>) value;
                editor.putStringSet(key, setImagePath);
        }
        editor.apply();
    }

    public enum SHARE_PREFERENCES_TYPE {
        STRING, INTEGER, BOOLEAN, SET
    }
}
