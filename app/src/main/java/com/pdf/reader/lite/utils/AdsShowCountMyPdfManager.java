package com.pdf.reader.lite.utils;

public class AdsShowCountMyPdfManager {
    private static final String LOG = "AdsShowCountMyPdfManager";

    private static AdsShowCountMyPdfManager mInstance;
    private int mCountForClickItem;

    private AdsShowCountMyPdfManager() {
        mCountForClickItem = 1;
    }

    public static AdsShowCountMyPdfManager getInstance() {
        if (mInstance == null) {
            mInstance = new AdsShowCountMyPdfManager();
        }

        return mInstance;
    }


    public boolean checkShowAdsForClickItem() {
        FirebaseRemoteUtils firebaseRemoteUtils = new FirebaseRemoteUtils();
        return mCountForClickItem >= firebaseRemoteUtils.getNumberTimeShowAdsOnce();
    }

    public void increaseCountForClickItem() {
        FirebaseRemoteUtils firebaseRemoteUtils = new FirebaseRemoteUtils();
        if (mCountForClickItem >= firebaseRemoteUtils.getNumberTimeShowAdsOnce()) {
            mCountForClickItem = 1;
        } else {
            mCountForClickItem ++;
        }
    }

}
