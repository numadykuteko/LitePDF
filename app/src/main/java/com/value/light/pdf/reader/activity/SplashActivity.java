package com.value.light.pdf.reader.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.ads.control.Admod;
import com.ads.control.funtion.AdCallback;
import com.value.light.pdf.reader.BuildConfig;
import com.value.light.pdf.reader.R;
import com.value.light.pdf.reader.utils.NetworkUtils;
import com.value.light.pdf.reader.utils.file.RealPathUtil;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends BaseActivity {

    private boolean mIsFromOpenPdf = false;
    private String mFilePdfPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNoActionBar();

        setContentView(R.layout.activity_splash);

        precheckIntentFilter();
    }

    private void precheckIntentFilter() {
        Intent intent = getIntent();

        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();
            String filepath = null;

            if (Intent.ACTION_VIEW.equals(action) && type != null && type.endsWith("pdf")) {
                Uri fileUri = intent.getData();
                if (fileUri != null) {
                    filepath = RealPathUtil.getInstance().getRealPath(this, fileUri);
                }

                mIsFromOpenPdf = true;
            } else if (Intent.ACTION_SEND.equals(action) && type != null && type.endsWith("pdf")) {
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

        Admod.getInstance().loadSplashInterstitalAds(this,
                BuildConfig.full_splash_id,
                30000,
                new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        gotoTargetActivity();
                    }

                    @Override
                    public void onAdFailedToLoad(int i) {
                        gotoTargetActivity();
                    }
                });
    }

    private void gotoTargetActivity() {
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