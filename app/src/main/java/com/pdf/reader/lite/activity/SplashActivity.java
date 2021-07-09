package com.pdf.reader.lite.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.pdf.reader.lite.BuildConfig;
import com.pdf.reader.lite.R;
import com.pdf.reader.lite.utils.FirebaseRemoteUtils;
import com.pdf.reader.lite.utils.NetworkUtils;
import com.pdf.reader.lite.utils.PreferenceHelper;
import com.pdf.reader.lite.utils.file.RealPathUtil;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends BaseActivity {

    private boolean mIsFromOpenPdf = false;
    private String mFilePdfPath = null;
    private InterstitialAd mInterstitialAd;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNoActionBar();

        setContentView(R.layout.activity_splash);

        FirebaseRemoteUtils firebaseRemoteUtils = new FirebaseRemoteUtils();
        firebaseRemoteUtils.fetchRemoteConfig(this, () -> {});
        precheckIntentFilter();
    }

    private void precheckIntentFilter() {
        Intent intent = getIntent();

        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();
            String filepath = null;

            if (Intent.ACTION_VIEW.equals(action) && type != null && type.toLowerCase().endsWith("pdf")) {
                Uri fileUri = intent.getData();
                if (fileUri != null) {
                    filepath = RealPathUtil.getInstance().getRealPath(this, fileUri);
                }

                mIsFromOpenPdf = true;
            } else if (Intent.ACTION_SEND.equals(action) && type != null && type.toLowerCase().endsWith("pdf")) {
                Uri fileUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (fileUri != null) {
                    filepath = RealPathUtil.getInstance().getRealPath(this, fileUri);
                }

                mIsFromOpenPdf = true;
            }

            if (filepath != null) {
                mFilePdfPath = filepath;
            }
        } else {
            mIsFromOpenPdf = false;
        }

        prepareShowAds();
    }

    private void prepareShowAds() {
        if (!NetworkUtils.isNetworkConnected(this)) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    gotoTargetActivity();
                }
            }, 1000);
            return;
        }

        int numberTimeOpenOtherApp = PreferenceHelper.getInstance(this).getOpenFromOtherAppTime();
        FirebaseRemoteUtils firebaseRemoteUtils = new FirebaseRemoteUtils();

        Log.d("duynm", numberTimeOpenOtherApp + "");
        Log.d("duynm", firebaseRemoteUtils.getNumberTimeShowAdsOnce() + "");

        if (!mIsFromOpenPdf || numberTimeOpenOtherApp >= firebaseRemoteUtils.getNumberTimeShowAdsOnce()) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        mInterstitialAd.setAdListener(new AdListener());
                        gotoTargetActivity();
                    });
                }
            }, (int) firebaseRemoteUtils.getTimeoutSplash());

            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(mIsFromOpenPdf ? BuildConfig.full_other_app_id : BuildConfig.full_splash_id);
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mInterstitialAd.show();
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    gotoTargetActivity();
                }

                @Override
                public void onAdOpened() {
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    // Code to be executed when the ad is displayed.
                }

                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                @Override
                public void onAdClosed() {
                    gotoTargetActivity();
                }

            });
            mInterstitialAd.loadAd(adRequest);
            PreferenceHelper.getInstance(this).setOpenFromOtherAppTime(1);
        } else {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    gotoTargetActivity();
                }
            }, 1000);
            PreferenceHelper.getInstance(this).setOpenFromOtherAppTime(numberTimeOpenOtherApp + 1);
        }
    }

    private void gotoTargetActivity() {
        if (mTimer != null) {
            mTimer.cancel();
        }

        Intent intent;
        if (!mIsFromOpenPdf || mFilePdfPath == null) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, ViewPdfActivity.class);
            intent.putExtra(ViewPdfActivity.EXTRA_FILE_PATH, mFilePdfPath);
            intent.putExtra(ViewPdfActivity.EXTRA_FROM_FIRST_OPEN, true);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}