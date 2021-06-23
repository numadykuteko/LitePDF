package com.pdf.reader.lite.activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.pdf.reader.lite.component.NoticeDialog;
import com.pdf.reader.lite.data.ViewPdfOption;
import com.pdf.reader.lite.lib.IShowPage;
import com.pdf.reader.lite.lib.PDFAdapter;
import com.pdf.reader.lite.lib.PDFViewPager;
import com.pdf.reader.lite.utils.ColorUtils;
import com.pdf.reader.lite.utils.rate.OnCallback;
import com.pdf.reader.lite.utils.rate.RateAppDialog;
import com.pdf.reader.lite.utils.rate.RateUtils;
import com.pdf.reader.lite.utils.ToastUtils;
import com.pdf.reader.lite.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;

import static com.pdf.reader.lite.utils.file.FileUtils.AUTHORITY_APP;

public class ViewPdfActivity extends BaseActivity implements IShowPage {

    public static final String EXTRA_FILE_PATH = "EXTRA_FILE_PATH";
    public static final String EXTRA_FROM_FIRST_OPEN = "EXTRA_FROM_FIRST_OPEN";

    private static final int REQUEST_EXTERNAL_PERMISSION_FOR_OPEN_LOCAL_FILE = 2;
    private static final String STATE_CURRENT_PAGE_INDEX = "current_page_index";

    private static final String PREF_NAME_VIEW_MODE_PDF = "PREF_NAME_VIEW_MODE_PDF";
    private static final String PREF_NAME_VIEW_ORIENTATION_PDF = "PREF_NAME_VIEW_ORIENTATION_PDF";
    private static final String PREF_NAME = "pdf_reader_application";

    private static final String FULL_APP_PACKAGE = "com.pdfreader.scanner.pdfviewer";

    private static final int VIEW_MODE_DAY = 0;
    private static final int VIEW_MODE_NIGHT = 1;
    private static final int VIEW_ORIENTATION_HORIZONTAL = 0;
    private static final int VIEW_ORIENTATION_VERTICAL = 1;

    private boolean mIsFromSplash = false;
    private boolean mIsViewFull = false;

    private String mFilePath = null;

    private ViewPdfOption mViewOption;
    private SharedPreferences mPrefs;

    private ConstraintLayout mToolBar;
    private ImageView mBackBtn;
    private TextView mNameView;
    private ImageView mFullScreenBtn;
    private ImageView mShareBtn;
    private View mLineView;

    private LinearLayout mViewContainer;

    private LinearLayout mOptionView;
    private TextView mPageInfoTextView;
    private ImageView mOrientationImage;
    private ImageView mTypeImage;

    private RelativeLayout mBannerAds;

    private PDFViewPager mPdfViewpager;

    private ParcelFileDescriptor mFileDescriptor;
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;
    private int mPageIndex;
    private Bundle savedBundle;

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
                suggestDownloadFullApp();
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
                    }
                }

                @Override
                public void onSubmit(String review) {
                    ToastUtils.showMessageShort(getApplicationContext(), "Thank you for your rating");
                    RateUtils.setRateDone(getApplicationContext());
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

        getOldViewOption();

        mToolBar = findViewById(R.id.layout_toolbar);
        mBackBtn = findViewById(R.id.toolbar_btn_back);
        mNameView = findViewById(R.id.toolbar_name_tv);
        mFullScreenBtn = findViewById(R.id.toolbar_action_full_screen);
        mShareBtn = findViewById(R.id.toolbar_action_share);
        mLineView = findViewById(R.id.separator);

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
                super.onAdFailedToLoad(loadAdError);
            }
        });
        mAdView.loadAd(adRequest);

        mViewContainer = findViewById(R.id.pdf_view_container);

        mOptionView = findViewById(R.id.option_view);
        mPageInfoTextView = findViewById(R.id.page_info);
        LinearLayout orientationBtnView = findViewById(R.id.option_view_orientation);
        mOrientationImage = findViewById(R.id.option_view_orientation_img);
        LinearLayout typeBtnView = findViewById(R.id.option_view_mode);
        mTypeImage = findViewById(R.id.option_view_mode_img);

        mIsViewFull = false;
        setForFullView();
        mFullScreenBtn.setOnClickListener(v -> {
            mIsViewFull = !mIsViewFull;
            if (mIsViewFull) {
                ToastUtils.showMessageShort(this, "Press back to exit view full screen");
            }
            setForFullView();
        });

        setForViewType(true);
        typeBtnView.setOnClickListener(v -> {
            if (mViewOption.getViewMode() == VIEW_MODE_DAY) {
                mViewOption.setViewMode(VIEW_MODE_NIGHT);
                ToastUtils.showMessageShort(getApplicationContext(), "Changed to night mode");
            } else {
                mViewOption.setViewMode(VIEW_MODE_DAY);
                ToastUtils.showMessageShort(getApplicationContext(), "Changed to day mode");
            }

            setForViewType(false);
            saveOldViewOption();
        });

        setForOrientation();
        orientationBtnView.setOnClickListener(v -> {
            if (mViewOption.getOrientation() == VIEW_ORIENTATION_VERTICAL) {
                mViewOption.setOrientation(VIEW_ORIENTATION_HORIZONTAL);
                ToastUtils.showMessageShort(getApplicationContext(), "Changed to horizontal mode");
            } else {
                mViewOption.setOrientation(VIEW_ORIENTATION_VERTICAL);
                ToastUtils.showMessageShort(getApplicationContext(), "Changed to vertical mode");
            }
            setForOrientation();
        });

        mBackBtn.setOnClickListener(v -> onBackPressed());

        mShareBtn.setOnClickListener(v -> {
            if (mFilePath != null) {
                FileUtils.shareFile(this, new File(mFilePath));
            }
        });
    }

    private void setForFullView() {
        if (mIsViewFull) {
            mToolBar.setVisibility(View.GONE);
            mBannerAds.setVisibility(View.GONE);
            mOptionView.setVisibility(View.GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            mToolBar.setVisibility(View.VISIBLE);
            mBannerAds.setVisibility(View.VISIBLE);
            mOptionView.setVisibility(View.VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setForViewType(boolean isFirstTime) {
        mToolBar.setBackgroundColor(getViewOptionColor());
        mBackBtn.setColorFilter(getIconColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
        mFullScreenBtn.setColorFilter(getIconColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
        mShareBtn.setColorFilter(getIconColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
        mNameView.setTextColor(getViewTextColor());
        mLineView.setBackgroundColor(getViewTextColor());

        mViewContainer.setBackgroundColor(getViewPdfContainerColor());

        mOptionView.setBackgroundColor(getViewOptionColor());
        mPageInfoTextView.setTextColor(getViewTextColor());

        mOrientationImage.setColorFilter(getIconColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
        mTypeImage.setColorFilter(getIconColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getViewOptionColor());

        if (mViewOption.getViewMode() == VIEW_MODE_DAY) {
            mTypeImage.setImageDrawable(getDrawable(R.drawable.ic_view_day_mode));
        } else {
            mTypeImage.setImageDrawable(getDrawable(R.drawable.ic_view_night_mode));
        }

        if (mPdfViewpager != null) {
            setupPdfViewer();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setForOrientation() {
        if (mViewOption.getOrientation() == VIEW_ORIENTATION_HORIZONTAL) {
            mOrientationImage.setImageDrawable(getDrawable(R.drawable.ic_view_horizontal));
        } else {
            mOrientationImage.setImageDrawable(getDrawable(R.drawable.ic_view_vertical));
        }
        if (mPdfViewpager != null) {
            setupPdfViewer();
        }
    }

    private int getIconColor() {
        if (mViewOption.getViewMode() == VIEW_MODE_DAY) {
            return ColorUtils.getColorFromResource(this, R.color.icon_type_day_mode);
        } else {
            return ColorUtils.getColorFromResource(this, R.color.icon_type_night_mode);
        }
    }

    private int getViewPdfContainerColor() {
        if (mViewOption.getViewMode() == VIEW_MODE_DAY) {
            return ColorUtils.getColorFromResource(this, R.color.background_type_day_mode);
        } else {
            return ColorUtils.getColorFromResource(this, R.color.background_type_night_mode);
        }
    }

    private int getViewOptionColor() {
        if (mViewOption.getViewMode() == VIEW_MODE_DAY) {
            return ColorUtils.getColorFromResource(this, R.color.option_view_type_day_mode);
        } else {
            return ColorUtils.getColorFromResource(this, R.color.option_view_type_night_mode);
        }
    }

    private int getViewTextColor() {
        if (mViewOption.getViewMode() == VIEW_MODE_DAY) {
            return ColorUtils.getColorFromResource(this, R.color.text_type_day_mode);
        } else {
            return ColorUtils.getColorFromResource(this, R.color.text_type_night_mode);
        }
    }

    private void getOldViewOption() {
        mPrefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int oldViewType = mPrefs.getInt(PREF_NAME_VIEW_MODE_PDF, VIEW_MODE_DAY);
        int oldViewOrientation = mPrefs.getInt(PREF_NAME_VIEW_ORIENTATION_PDF, VIEW_ORIENTATION_VERTICAL);
        mViewOption = new ViewPdfOption(oldViewType, oldViewOrientation);
    }

    private void saveOldViewOption() {
        mPrefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mPrefs.edit().putInt(PREF_NAME_VIEW_MODE_PDF, mViewOption.getViewMode()).apply();
        mPrefs.edit().putInt(PREF_NAME_VIEW_ORIENTATION_PDF, mViewOption.getOrientation()).apply();
    }

    private void suggestDownloadFullApp() {
        NoticeDialog noticeDialog = new NoticeDialog(this, "App not support", "Sorry we can not open this file. This file may be interrupted or encrypted.", "Exit", new NoticeDialog.ConfirmListener() {
            @Override
            public void onSubmit() {
                ViewPdfActivity.super.onBackPressed();
            }
        });
        noticeDialog.setCancelable(false);
        noticeDialog.setCanceledOnTouchOutside(false);
        noticeDialog.show();
    }

    private void openByFullApp() {
        if (isAppAvailable()) {
            File file = new File(mFilePath);
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            try {
                Uri uri = FileProvider.getUriForFile(this, AUTHORITY_APP, file);

                target.setDataAndType(uri, "application/pdf");
                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                target.setPackage(FULL_APP_PACKAGE);
                startActivity(Intent.createChooser(target, "Open file"));
            } catch (Exception e) {
                ToastUtils.showMessageShort(this, "Sorry we can't open this file now");
            }
        } else {
            Uri uri = Uri.parse("market://details?id=" + FULL_APP_PACKAGE);
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(goToMarket);
            } catch (Exception e) {
                Toast.makeText(this, "Sorry we can't open Google Play now.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isAppAvailable() {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(FULL_APP_PACKAGE, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void showErrorDialog() {
        NoticeDialog noticeDialog = new NoticeDialog(this, "Error message", "File is not valid. Please try again later.", "Exit",
                this::onBackPressed);
        noticeDialog.setCancelable(false);
        noticeDialog.setCanceledOnTouchOutside(false);
        noticeDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_PERMISSION_FOR_OPEN_LOCAL_FILE) {
            if (!notHaveStoragePermission()) {
                setUpViewPager();
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

        mPdfViewpager = findViewById(R.id.pdfviewfpager);
        mPdfViewpager.setSwipeOrientation(mViewOption.getOrientation());
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
            suggestDownloadFullApp();
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

    @SuppressLint("SetTextI18n")
    private void setUpViewPager() {
        PDFAdapter adapter = new PDFAdapter(this, this, mPdfRenderer.getPageCount(), mViewOption.getViewMode());
        mPdfViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onPageSelected(int position) {
                mPageIndex = position;
                mPageInfoTextView.setText((mPageIndex + 1) + " / " + mPdfRenderer.getPageCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mPdfViewpager.setAdapter(adapter);
        mPdfViewpager.setCurrentItem(mPageIndex);

        mPageInfoTextView.setText((mPageIndex + 1) + " / " + mPdfRenderer.getPageCount());
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
        if (null != mCurrentPage)
            mCurrentPage.close();

        if (null != mPdfRenderer)
            mPdfRenderer.close();

        if (null != mFileDescriptor)
            mFileDescriptor.close();
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

            if (mViewOption.getViewMode() == VIEW_MODE_DAY) {
                return bitmap;
            }

            int width, height;
            height = bitmap.getHeight();
            width = bitmap.getWidth();

            Bitmap nightModeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(nightModeBitmap);
            Paint paint = new Paint();

            ColorMatrix grayScaleMatrix = new ColorMatrix();
            grayScaleMatrix.setSaturation(0);
            ColorMatrix invertMatrix =
                    new ColorMatrix(new float[] {
                            -1,  0,  0,  0, 255,
                            0, -1,  0,  0, 255,
                            0,  0, -1,  0, 255,
                            0,  0,  0,  1,   0});

            ColorMatrix nightModeMatrix = new ColorMatrix();
            nightModeMatrix.postConcat(grayScaleMatrix);
            nightModeMatrix.postConcat(invertMatrix);

            paint.setColorFilter(new ColorMatrixColorFilter(nightModeMatrix));
            c.drawBitmap(bitmap, 0, 0, paint);

            return nightModeBitmap;
        } catch (Exception e) {
            Log.d("duynm", e.getMessage());
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
}