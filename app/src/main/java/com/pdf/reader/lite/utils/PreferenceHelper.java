package com.pdf.reader.lite.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {
    private final SharedPreferences mPrefs;
    public static final String PREF_NAME = "pdf_reader_application";
    public static final String PREF_NAME_OPEN_FROM_OTHER_APP = "PREF_NAME_OPEN_FROM_OTHER_APP";
    private static PreferenceHelper mInstance;

    private PreferenceHelper(Context context) {
        mPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PreferenceHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PreferenceHelper(context);
        }
        return mInstance;
    }

    public void setOpenFromOtherAppTime(int time) {
        mPrefs.edit().putInt(PREF_NAME_OPEN_FROM_OTHER_APP, time).apply();
    }

    public int getOpenFromOtherAppTime() {
        return mPrefs.getInt(PREF_NAME_OPEN_FROM_OTHER_APP, 1);
    }
}
