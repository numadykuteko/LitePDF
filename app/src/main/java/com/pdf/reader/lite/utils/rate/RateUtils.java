package com.pdf.reader.lite.utils.rate;

import android.content.Context;
import android.content.SharedPreferences;

public class RateUtils {
    private static final String PREF_NAME = "pdf_reader_application";
    private static final String PREF_NAME_RATE_US = "PREF_NAME_RATE_US";

    public static boolean isUserRated(Context context) {
        SharedPreferences mPrefs;
        mPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int rateStatus = mPrefs.getInt(PREF_NAME_RATE_US, 0);
        return rateStatus != 0;
    }

    public static void setRateDone(Context context) {
        SharedPreferences mPrefs;
        mPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mPrefs.edit().putInt(PREF_NAME_RATE_US, 1).apply();
    }
}
