package com.pdf.reader.lite.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    public static void showMessageShort(Context context, String message) {
        if (context == null)    return;
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showMessageLong(Context context, String message) {
        if (context == null)    return;
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
