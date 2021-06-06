package com.value.light.pdf.reader.utils;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.value.light.pdf.reader.R;

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
