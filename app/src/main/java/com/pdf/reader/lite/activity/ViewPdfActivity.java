package com.pdf.reader.lite.activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.pdf.reader.lite.BuildConfig;
import com.pdf.reader.lite.R;
import com.pdf.reader.lite.component.ConfirmDialog;
import com.pdf.reader.lite.component.LoadingDialog;
import com.pdf.reader.lite.component.NoticeDialog;
import com.pdf.reader.lite.lib.IShowPage;
import com.pdf.reader.lite.lib.PDFAdapter;
import com.pdf.reader.lite.lib.PDFViewPager;
import com.pdf.reader.lite.utils.FirebaseRemoteUtils;
import com.pdf.reader.lite.utils.ToastUtils;
import com.pdf.reader.lite.utils.file.FileUtils;
import com.pdf.reader.lite.utils.rate.OnCallback;
import com.pdf.reader.lite.utils.rate.RateAppDialog;
import com.pdf.reader.lite.utils.rate.RateUtils;
import com.pdfview.PDFView;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static com.pdf.reader.lite.utils.file.FileUtils.AUTHORITY_APP;

public class ViewPdfActivity extends BaseActivity implements IShowPage {

    public static final String EXTRA_FILE_PATH = "EXTRA_FILE_PATH";
    public static final String EXTRA_FROM_FIRST_OPEN = "EXTRA_FROM_FIRST_OPEN";

    private static final int REQUEST_EXTERNAL_PERMISSION_FOR_OPEN_LOCAL_FILE = 2;

    private boolean mIsFromSplash = false;

    private String mFilePath = null;

    private ConstraintLayout mToolBar;
    private ImageView mBackBtn;
    private TextView mNameView;
    private ImageView mFullScreenBtn;
    private ImageView mShareBtn;

    private boolean mIsViewFull = false;
    private boolean mLoadAdsDone = true;

    private RelativeLayout mBannerAds;

    private PDFViewPager mPdfViewpager;
    private PDFView mPdfViewScroll;

    private ParcelFileDescriptor mFileDescriptor;
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;
    private int mPageIndex;
    private Bundle savedBundle;

    private static final String STATE_CURRENT_PAGE_INDEX = "current_page_index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNoActionBar();

        setContentView(R.layout.activity_view_pdf);

        initView();

        String extraFilePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (extraFilePath != null && extraFilePath.length() > 0 && FileUtils.checkFileExist(extraFilePath)) {
            mIsFromSplash = getIntent().getBooleanExtra(EXTRA_FROM_FIRST_OPEN, false);
            String fileName = FileUtils.getFileName(extraFilePath);
            mNameView.setText(fileName);
            mFilePath = extraFilePath;

            if (FileUtils.getNumberPages(extraFilePath) == 0) {
                suggestDownloadFullApp("Because file has 0 page or encrypted");
            } else {
                savedBundle = savedInstanceState;
                setupPdfViewer();
            }
        } else {
            showErrorDialog();
        }
    }

    @Override
    public void onBackPressed() {
        if (mIsViewFull) {
            mIsViewFull = false;
            setForFullView();
            return;
        } else if (!RateUtils.isUserRated(this)) {
            RateAppDialog rateApp2Dialog = new RateAppDialog(this);
            rateApp2Dialog.setCallback(new OnCallback() {
                @Override
                public void onMaybeLater() {
                    if (mIsFromSplash) {
                        Intent intent = new Intent(ViewPdfActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        ViewPdfActivity.super.onBackPressed();
                        finish();
                    }
                }

                @Override
                public void onSubmit(String review) {
                    ToastUtils.showMessageShort(getApplicationContext(), "Thank you for your rating");
                    RateUtils.setRateDone(getApplicationContext());
                    ViewPdfActivity.super.onBackPressed();
                    finish();
                }

                @Override
                public void onRate() {
                    RateUtils.setRateDone(getApplicationContext());
                    final String appPackageName = getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    } catch (Exception e) {
                        ToastUtils.showMessageShort(getApplicationContext(), "Thank you for your rating");
                    }

                    if (mIsFromSplash) {
                        Intent intent = new Intent(ViewPdfActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        ViewPdfActivity.super.onBackPressed();
                        finish();
                    }
                }
            });
            rateApp2Dialog.show();
            return;
        } else if (mIsFromSplash) {
            Intent intent = new Intent(ViewPdfActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        super.onBackPressed();
    }

    @SuppressLint("CutPasteId")
    private void initView() {
        mToolBar = findViewById(R.id.layout_toolbar);
        mBackBtn = findViewById(R.id.toolbar_btn_back);
        mNameView = findViewById(R.id.toolbar_name_tv);
        mFullScreenBtn = findViewById(R.id.toolbar_action_full_screen);
        mShareBtn = findViewById(R.id.toolbar_action_share);

        mBannerAds = findViewById(R.id.banner_ads);

        AdView mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.SMART_BANNER);
        mAdView.setAdUnitId(BuildConfig.banner_id);
        mBannerAds.addView(mAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                mBannerAds.setVisibility(View.GONE);
                mLoadAdsDone = false;
                super.onAdFailedToLoad(loadAdError);
            }
        });
        mAdView.loadAd(adRequest);

        mIsViewFull = false;
        setForFullView();
        mFullScreenBtn.setOnClickListener(v -> {
            mIsViewFull = !mIsViewFull;
            if (mIsViewFull) {
                ToastUtils.showMessageShort(this, "Press back to exit view full screen");
            }
            setForFullView();
        });

        mBackBtn.setOnClickListener(v -> onBackPressed());

        mShareBtn.setOnClickListener(v -> {
            if (mFilePath != null) {
                FileUtils.shareFile(this, new File(mFilePath));
            }
        });
    }

    private void openByFullApp() {
        FirebaseRemoteUtils firebaseRemoteUtils = new FirebaseRemoteUtils();
        String fullAppPackage = firebaseRemoteUtils.getFullAppPackage();
        if (isAppAvailable(fullAppPackage)) {
            File file = new File(mFilePath);
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            try {
                Uri uri = FileProvider.getUriForFile(this, AUTHORITY_APP, file);

                target.setDataAndType(uri, "application/pdf");
                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                target.setPackage(fullAppPackage);
                startActivity(Intent.createChooser(target, "Open file"));
            } catch (Exception e) {
                ToastUtils.showMessageShort(this, "Sorry we can't open this file now");
            }
        } else {
            Uri uri = Uri.parse("market://details?id=" + fullAppPackage);
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(goToMarket);
            } catch (Exception e) {
                Toast.makeText(this, "Sorry we can't open Google Play now.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isAppAvailable(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void setForFullView() {
        if (mIsViewFull) {
            mToolBar.setVisibility(View.GONE);
            mBannerAds.setVisibility(View.GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            mToolBar.setVisibility(View.VISIBLE);
            if (mLoadAdsDone) {
                mBannerAds.setVisibility(View.VISIBLE);
            }
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void suggestDownloadFullApp(String message) {
        ConfirmDialog confirmDialog = new ConfirmDialog(this, "App not support", "Sorry we can not open this file. Would you like to try with full function application?\n\nError: " + message, new ConfirmDialog.ConfirmListener() {
            @Override
            public void onSubmit() {
                openByFullApp();
                ViewPdfActivity.super.onBackPressed();
                finish();
            }

            @Override
            public void onCancel() {
                ViewPdfActivity.super.onBackPressed();
                finish();
            }
        });
        confirmDialog.setCancelable(false);
        confirmDialog.setCanceledOnTouchOutside(false);
        confirmDialog.show();
    }

    private void showErrorDialog() {
        NoticeDialog noticeDialog = new NoticeDialog(this, "Error message", "File is not valid. Please try again later.", "Exit",
                ViewPdfActivity.super::onBackPressed);
        noticeDialog.setCancelable(false);
        noticeDialog.setCanceledOnTouchOutside(false);
        noticeDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_PERMISSION_FOR_OPEN_LOCAL_FILE) {
            if (!notHaveStoragePermission()) {
                setupPdfViewer();
            } else {
                showErrorDialog();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setupPdfViewer() {
        if (notHaveStoragePermission()) {
            requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_OPEN_LOCAL_FILE);
            return;
        }

        int numberPage = FileUtils.getNumberPages(mFilePath);
        setupTypePdf(numberPage);

        if (numberPage > 0) {
            float waitSecond;
            if (numberPage < 10) {
                waitSecond = 0.5f;
            } else if (numberPage < 30) {
                waitSecond = 0.7f;
            } else if (numberPage < 50) {
                waitSecond = 1.2f;
            } else if (numberPage < 100) {
                waitSecond = 1.6f;
            } else if (numberPage < 150) {
                waitSecond = 2.0f;
            } else {
                waitSecond = 2.3f;
            }

            LoadingDialog loadingDialog = new LoadingDialog(this);
            loadingDialog.show();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(loadingDialog::dismiss);
                }
            }, (int) (waitSecond * 1000));
        }
    }

    private void setupTypePdf(int numberPage) {
        mPdfViewpager = findViewById(R.id.pdfviewfpager);
        mPdfViewScroll = findViewById(R.id.pdfviewscroll);

        FirebaseRemoteUtils remoteUtils = new FirebaseRemoteUtils();
        if (numberPage > remoteUtils.getNumberPageToMax()) {
            mPdfViewpager.setVisibility(View.VISIBLE);
            mPdfViewScroll.setVisibility(View.GONE);
            mPdfViewpager.setSwipeOrientation(1);
            mPdfViewpager.setOffscreenPageLimit(2);

            mPageIndex = 0;
            // If there is a savedInstanceState (screen orientations, etc.), we restore the page index.
            if (null != savedBundle) {
                mPageIndex = savedBundle.getInt(STATE_CURRENT_PAGE_INDEX, 0);
            }

            try {
                openRenderer();
                setUpViewPager();
            } catch (Exception e) {
                suggestDownloadFullApp("Because can not init renderer");
            } catch (OutOfMemoryError error) {
                suggestDownloadFullApp("Because out of memory");
            }
        } else {
            mPdfViewpager.setVisibility(View.GONE);
            mPdfViewScroll.setVisibility(View.VISIBLE);
            mPdfViewScroll.fromFile(mFilePath).show();
        }
    }


    @SuppressLint("SetTextI18n")
    private void setUpViewPager() {
        PDFAdapter adapter = new PDFAdapter(this, this, mPdfRenderer.getPageCount());
        mPdfViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onPageSelected(int position) {
                mPageIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mPdfViewpager.setAdapter(adapter);
        mPdfViewpager.setCurrentItem(mPageIndex);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mCurrentPage) {
            outState.putInt(STATE_CURRENT_PAGE_INDEX, mCurrentPage.getIndex());
        }
    }


    private void openRenderer() throws Exception {
        File file = new File(mFilePath);
        mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        if (mFileDescriptor != null) {
            mPdfRenderer = new PdfRenderer(mFileDescriptor);

            if (mPdfRenderer.getPageCount() == 0) {
                throw new Exception();
            }
        } else {
            throw new Exception();
        }
    }


    private void closeRenderer() throws IOException {
        try {
            if (null != mCurrentPage)
                mCurrentPage.close();

            if (null != mPdfRenderer)
                mPdfRenderer.close();

            if (null != mFileDescriptor)
                mFileDescriptor.close();
        } catch (Exception e) {
            // nothing
        }

    }

    private Bitmap toRightBitmap(int index) {
        try {
            // Make sure to close the current page before opening another one.
            try {
                if (null != mCurrentPage) {
                    mCurrentPage.close();
                }
            } catch (Exception e) {
            }

            mCurrentPage = mPdfRenderer.openPage(index);
            Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                    Bitmap.Config.ARGB_8888);

            mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            return bitmap;
        } catch (Exception | OutOfMemoryError e) {
            return null;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void showPage(ViewGroup container, ImageView imageView, LinearLayout reloadView, int index) {
        try {
            if (mPdfRenderer.getPageCount() <= index) {
                return;
            }

            AsyncTask asyncTask = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    checkGetBitmap(container, imageView, reloadView, index);

                    return null;
                }
            };

            asyncTask.execute();

        } catch (Exception ignored) {
        }
    }

    private void checkGetBitmap(ViewGroup container, ImageView imageView, LinearLayout reloadView, int index) {
        Bitmap bitmap = toRightBitmap(index);

        if (bitmap != null) {
            runOnUiThread(() -> {
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
                reloadView.setVisibility(View.GONE);
                reloadView.setOnClickListener(view -> {
                });
            });
        } else {
            runOnUiThread(() -> {
                reloadView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                reloadView.setOnClickListener(view -> {
                    reloadView.setVisibility(View.GONE);
                    checkGetBitmap(container, imageView, reloadView, index);
                });
            });
        }
    }

    @Override
    protected void onDestroy() {
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }
}