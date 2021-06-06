package com.value.light.pdf.reader;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

public class PdfApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(
                this,
                initializationStatus -> {});
    }
}
