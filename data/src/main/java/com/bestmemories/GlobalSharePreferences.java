package com.bestmemories;

import android.content.Context;
import android.content.SharedPreferences;

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
    private Context mContext;

    @Inject
    public GlobalSharePreferences(Context context) {
        mContext = context;
    }

    public SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(BEST_MEMORIES_SHARED_PREFERENCES, MODE_PRIVATE);
    }

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
        }
        editor.apply();
    }

    public enum SHARE_PREFERENCES_TYPE {
        STRING, INTEGER, BOOLEAN
    }
}
