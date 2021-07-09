package com.pdf.reader.lite.utils;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
public class FirebaseUtils {
    public static final String EVENT_TYPE = "event_type";

    public static void sendEventFunctionUsed(Context context, String eventName, String eventType) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);

        Bundle bundle = new Bundle();
        bundle.putString(EVENT_TYPE, eventType);
        firebaseAnalytics.logEvent(eventName, bundle);
    }
}
