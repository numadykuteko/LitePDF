package com.pdf.reader.lite.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.pdf.reader.lite.BuildConfig;
import com.pdf.reader.lite.R;
import com.pdf.reader.lite.component.ConfirmDialog;
import com.pdf.reader.lite.component.ExitConfirmDialog;
import com.pdf.reader.lite.component.RenameFileDialog;
import com.pdf.reader.lite.component.SettingSortDialog;
import com.pdf.reader.lite.component.UpdateDialog;
import com.pdf.reader.lite.data.FileData;
import com.pdf.reader.lite.utils.AdsShowCountMyPdfManager;
import com.pdf.reader.lite.utils.CommonUtils;
import com.pdf.reader.lite.utils.DateTimeUtils;
import com.pdf.reader.lite.utils.FirebaseRemoteUtils;
import com.pdf.reader.lite.utils.ToastUtils;
import com.pdf.reader.lite.utils.UpdateInfo;
import com.pdf.reader.lite.utils.adapter.FileListAdapter;
import com.pdf.reader.lite.utils.adapter.OnFileItemWithOptionClickListener;
import com.pdf.reader.lite.utils.file.FileUtilAsyncTask;
import com.pdf.reader.lite.utils.file.FileUtils;
import com.pdf.reader.lite.utils.file.RealPathUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements OnFileItemWithOptionClickListener, SettingSortDialog.SettingSortSubmit {
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE = 1;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR = 2;
    protected static final int TAKE_FILE_REQUEST = 2365;

    private int mCurrentSortBy = FileUtils.SORT_BY_DATE_DESC;
    private boolean mIsLoading;
    private List<FileData> mListFile = new ArrayList<>();
    private FileListAdapter mFileListAdapter;

    private LinearLayout mDataArea;
    private SwipeRefreshLayout mPullToRefresh;
    private RecyclerView mDataView;

    private LinearLayout mNoDataArea;

    private LinearLayout mNoPermissionArea;

    private LinearLayout mLoadingArea;

    private SearchView mSearchView;

    private RelativeLayout mBannerAds;

    private InterstitialAd mMyPdfInterstitialAd;
    private boolean mAdsLoading = false;
    private boolean mAdsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar(getString(R.string.app_name), false);
        setContentView(R.layout.activity_main);

        initView();
        preloadAds();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        final MenuItem sortItem = menu.findItem(R.id.menu_sort);

        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        mSearchView.setOnSearchClickListener(v -> sortItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER));
        mSearchView.setOnCloseListener(() -> {
            sortItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            return false;
        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!mIsLoading) {
                    searchForFile(newText.trim());
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        ExitConfirmDialog exitConfirmDialog = new ExitConfirmDialog(this, new ExitConfirmDialog.ConfirmListener() {
            @Override
            public void onSubmit() {
                MainActivity.super.onBackPressed();
            }

            @Override
            public void onCancel() {

            }
        });
        exitConfirmDialog.show();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort:
                showSortPopup();
                break;
            case R.id.menu_pick_file:
                startPickFile();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    reloadData(true);
                } else {
                    ToastUtils.showMessageShort(this, "Can not perform permission request");
                }
                break;

            case REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startPickFile();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadData(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == TAKE_FILE_REQUEST) {
            if (data == null) {
                return;
            }
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }

            if (RealPathUtil.getInstance().isDriveFile(uri)) {
                startDownloadFromGoogleDrive(uri);
                return;
            }

            //Getting Absolute Path
            String filePath = RealPathUtil.getInstance().getRealPath(this, uri);
            checkFilePathGet(uri, filePath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void startDownloadFromGoogleDrive(Uri uri) {
        AsyncTask.execute(() -> {
            try {
                @SuppressLint("Recycle")
                Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);

                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                String originalName = (returnCursor.getString(nameIndex));

                if (originalName == null) {
                    originalName = "Temp_file_pdf_" + DateTimeUtils.currentTimeToNaming();
                }

                File rootDir = getFilesDir();
                File file = File.createTempFile(originalName, ".pdf", rootDir);
                InputStream inputStream = getContentResolver().openInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(file);
                int read;
                int maxBufferSize = 1024 * 1024;
                int bytesAvailable = inputStream.available();

                int bufferSize = Math.min(bytesAvailable, maxBufferSize);

                final byte[] buffers = new byte[bufferSize];
                while ((read = inputStream.read(buffers)) != -1) {
                    outputStream.write(buffers, 0, read);
                }
                inputStream.close();
                outputStream.close();

                runOnUiThread(() -> checkFilePathGet(uri, file.getPath()));

            } catch (Exception e) {
                runOnUiThread(() -> checkFilePathGet(uri, null));
            }
        });
    }

    protected void checkFilePathGet(Uri uri, String filePath) {
        if (uri != null && filePath != null && filePath.length() > 0 && FileUtils.checkFileExist(filePath)) {

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(MainActivity.this, ViewPdfActivity.class);
                        intent.putExtra(ViewPdfActivity.EXTRA_FILE_PATH, filePath);
                        startActivity(intent);
                    });
                }
            }, 500);
        } else {
            ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file));
        }
    }

    private void initView() {

        mDataArea = findViewById(R.id.data_area);
        mPullToRefresh = findViewById(R.id.pull_to_refresh);
        mDataView = findViewById(R.id.data_list_area);
        mNoDataArea = findViewById(R.id.no_data_error_area);
        mNoPermissionArea = findViewById(R.id.no_permission_area);
        mLoadingArea = findViewById(R.id.loading_area);

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

        checkPermissionOnMain();

        mPullToRefresh.setOnRefreshListener(() -> reloadData(false));

        mFileListAdapter = new FileListAdapter(this);
        mDataView.setLayoutManager(new LinearLayoutManager(this));
        mDataView.setAdapter(mFileListAdapter);

        checkForUpdateApp();
    }

    private void checkForUpdateApp() {
        FirebaseRemoteUtils firebaseRemoteUtils = new FirebaseRemoteUtils();
        firebaseRemoteUtils.fetchRemoteConfig(this, () -> getUpdateInfo(firebaseRemoteUtils));
    }

    private void getUpdateInfo(FirebaseRemoteUtils firebaseRemoteUtils) {
        UpdateInfo updateInfo = firebaseRemoteUtils.getUpdateInfo();

        if (updateInfo != null) {
            if (updateInfo.getRequiredUpdateList().contains(BuildConfig.VERSION_CODE) && updateInfo.isIsRequired()) {
                if (updateInfo.getVersionCode() == BuildConfig.VERSION_CODE && (updateInfo.getNewPackage().length() == 0 || updateInfo.getNewPackage().equals(BuildConfig.APPLICATION_ID))) {
                    return;
                }

                UpdateDialog updateDialog = new UpdateDialog(this, updateInfo.getVersionName(), () -> gotoUpdateApp(updateInfo), true);
                updateDialog.show();
            } else {
                if (updateInfo.getVersionCode() > BuildConfig.VERSION_CODE && updateInfo.getStatus()) {
                    UpdateDialog updateDialog = new UpdateDialog(this, updateInfo.getVersionName(), () -> gotoUpdateApp(updateInfo), false);
                    updateDialog.show();
                }
            }
        }
    }

    private void gotoUpdateApp(UpdateInfo updateInfo) {
        String packageName = updateInfo.getNewPackage();
        if (!packageName.equals(BuildConfig.APPLICATION_ID)) {
            openByFullApp(packageName);
        } else {
            gotoMarket(packageName);
        }
    }

    private void openByFullApp(String packageName) {
        if (isAppAvailable(packageName)) {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                try {
                    startActivity(launchIntent);
                } catch (Exception e) {
                    gotoMarket(packageName);
                }
            } else {
                gotoMarket(packageName);
            }
        } else {
            gotoMarket(packageName);
        }
    }

    private void gotoMarket(String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (Exception e) {
            Toast.makeText(this, "Sorry we can't open Google Play now.", Toast.LENGTH_SHORT).show();
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

    private void preloadAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mMyPdfInterstitialAd = new InterstitialAd(this);
        mMyPdfInterstitialAd.setAdUnitId(BuildConfig.full_splash_id);
        mMyPdfInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdsLoading = false;
                mAdsLoaded = true;
                super.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                mAdsLoading = false;
                mAdsLoaded = false;
                super.onAdFailedToLoad(adError);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                super.onAdLoaded();
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                super.onAdClicked();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }
        });

        mAdsLoading = true;
        mAdsLoaded = false;
        mMyPdfInterstitialAd.loadAd(adRequest);
    }

    private void checkPermissionOnMain() {
        if (notHaveStoragePermission()) {
            requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE);
        }
    }

    public void reloadData(boolean isForceReload) {
        if (notHaveStoragePermission()) {
            mPullToRefresh.setRefreshing(false);

            showPermissionIssueArea();
            mIsLoading = false;
            return;
        }

        if (mIsLoading) return;
        mIsLoading = true;

        if (mListFile == null || mListFile.size() == 0 || isForceReload) {
            showLoadingArea();
        }

        if (mSearchView != null) {
            mSearchView.setQuery("", false);
        }

        getFileList();
    }

    private void searchForFile(String keyword) {
        if (mListFile != null && mListFile.size() > 0) {
            ArrayList<FileData> result = new ArrayList<>();

            for (FileData fileData : mListFile) {
                if (fileData.getDisplayName().toLowerCase().contains(keyword.toLowerCase())) {
                    result.add(fileData);
                }
            }

            mDataView.scrollToPosition(0);
            mFileListAdapter.setData(result);
        }
    }

    private void getFileList() {
        FileUtilAsyncTask asyncTask = new FileUtilAsyncTask(getApplication(),
                result -> runOnUiThread(() -> updateData(result)), mCurrentSortBy);
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateData(List<FileData> fileDataList) {
        if (fileDataList != null && fileDataList.size() > 0) {
            if (fileDataList.equals(mListFile)) {
                mIsLoading = false;
                mPullToRefresh.setRefreshing(false);

                return;
            }

            mListFile = new ArrayList<>();
            mListFile.addAll(fileDataList);

            Parcelable oldPosition = null;
            if (mDataView.getLayoutManager() != null) {
                oldPosition = mDataView.getLayoutManager().onSaveInstanceState();
            }
            mFileListAdapter.setData(mListFile);
            if (oldPosition != null) {
                mDataView.getLayoutManager().onRestoreInstanceState(oldPosition);
            }
            showDataArea();
        } else {
            showNoDataArea();
        }

        mIsLoading = false;
        mPullToRefresh.setRefreshing(false);
    }
    
    private void startRequestPermission() {
        if (notHaveStoragePermission()) {
            requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE);
        }
    }

    private void showNoDataArea() {
        mDataArea.setVisibility(View.GONE);
        mNoDataArea.setVisibility(View.VISIBLE);
        mNoPermissionArea.setVisibility(View.GONE);
        mLoadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mNoPermissionArea.setOnClickListener(v -> startRequestPermission());
        mDataArea.setVisibility(View.GONE);
        mNoDataArea.setVisibility(View.GONE);
        mNoPermissionArea.setVisibility(View.VISIBLE);
        mLoadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mDataArea.setVisibility(View.VISIBLE);
        mNoDataArea.setVisibility(View.GONE);
        mNoPermissionArea.setVisibility(View.GONE);
        mLoadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mDataArea.setVisibility(View.GONE);
        mNoDataArea.setVisibility(View.GONE);
        mLoadingArea.setVisibility(View.VISIBLE);
        mNoPermissionArea.setVisibility(View.GONE);
    }

    private void showSortPopup() {
        if (mIsLoading) {
            ToastUtils.showMessageShort(this, getString(R.string.sort_not_available));
            return;
        }

        SettingSortDialog dialog = new SettingSortDialog(this, this, mCurrentSortBy);
        dialog.show();
    }

    private void startPickFile() {
        if (notHaveStoragePermission()) {
            requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR);
        } else {
            Uri uri = Uri.parse(Environment.getRootDirectory() + "/");
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setDataAndType(uri, "application/pdf");
            try {
                startActivityForResult(Intent.createChooser(intent, "Select a File"), TAKE_FILE_REQUEST);
            } catch (android.content.ActivityNotFoundException ex) {
                ToastUtils.showMessageLong(this, "Can not open file manager");
            }
        }
    }

    @Override
    public void onClickItem(int position) {
        if (!AdsShowCountMyPdfManager.getInstance().checkShowAdsForClickItem() || mAdsLoading || (mMyPdfInterstitialAd != null && mMyPdfInterstitialAd.isLoading())) {
            openPdf(position);
        } else {
            if (mAdsLoaded && mMyPdfInterstitialAd != null && mMyPdfInterstitialAd.isLoaded()) {
                mMyPdfInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        openPdf(position);
                        preloadAds();
                    }
                });
                mMyPdfInterstitialAd.show();
            } else {
                openPdf(position);
                preloadAds();
            }
        }
        AdsShowCountMyPdfManager.getInstance().increaseCountForClickItem();
    }

    private void openPdf(int position) {
        if (position >= 0 && position < mFileListAdapter.getFileList().size()) {
            FileData fileData = mFileListAdapter.getFileList().get(position);

            Intent intent = new Intent(MainActivity.this, ViewPdfActivity.class);
            intent.putExtra(ViewPdfActivity.EXTRA_FILE_PATH, fileData.getFilePath());
            startActivity(intent);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onOptionItem(View view, int position) {
        if (position >= 0 && position < mFileListAdapter.getFileList().size()) {
            if (mSearchView != null) {
                mSearchView.clearFocus();
                CommonUtils.hideKeyboard(this);
            }

            PopupMenu popup = new PopupMenu(this, view);
            popup.inflate(R.menu.file_option_menu);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menu_open_file:
                        openPdfFile(position);
                        return true;
                    case R.id.menu_share_file:
                        sharePdfFile(position);
                        return true;
                    case R.id.menu_print_file:
                        printPdfFile(position);
                        return true;
                    case R.id.menu_upload_file:
                        uploadPdfFile(position);
                        return true;
                    case R.id.menu_rename_file:
                        renamePdfFile(position);
                        return true;
                    case R.id.menu_delete_file:
                        deletePdfFile(position);
                        return true;
                    default:
                        return false;
                }
            });
            //displaying the popup
            popup.show();
        }
    }

    @Override
    public void updateNewSort(int newSort) {
        mCurrentSortBy = newSort;
        reloadData(true);
    }

    private void openPdfFile(int position) {
        onClickItem(position);
    }

    private void renamePdfFile(int position) {
        FileData fileData = mFileListAdapter.getFileList().get(position);
        int oldPosition = mListFile.indexOf(fileData);

        String displayName;
        try {
            displayName = fileData.getDisplayName().substring(0, fileData.getDisplayName().lastIndexOf("."));
        } catch (Exception e) {
            return;
        }

        RenameFileDialog renameFileDialog = new RenameFileDialog(this, displayName, new RenameFileDialog.RenameFileListener() {
            @Override
            public void onSubmitName(String name) {
                String newName = name + ".pdf";
                int result = FileUtils.renameFile(fileData, newName);

                if (result == -2 || result == 0) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.can_not_edit_file_name));
                } else if (result == -1) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.duplicate_video_name) + ": " + name);
                } else {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.rename_file_success));
                    fileData.setFilePath(fileData.getFilePath().replace(fileData.getDisplayName(), newName));
                    fileData.setDisplayName(newName);

                    if (oldPosition >= 0 && oldPosition < mListFile.size()) {
                        mListFile.set(oldPosition, fileData);
                    }

                    mFileListAdapter.updateData(position, fileData);
                }
            }

            @Override
            public void onCancel() {

            }
        });

        renameFileDialog.show();
    }

    private void deletePdfFile(int position) {
        FileData fileData = mFileListAdapter.getFileList().get(position);
        int oldPosition = mListFile.indexOf(fileData);

        ConfirmDialog confirmDialog = new ConfirmDialog(this, getString(R.string.delete_file), getString(R.string.delete_file_question), new ConfirmDialog.ConfirmListener() {
            @Override
            public void onSubmit() {
                if (!notHaveStoragePermission()) {
                    FileUtils.deleteFileOnExist(fileData.getFilePath());

                    if (oldPosition >= 0 && oldPosition < mListFile.size()) {
                        mListFile.remove(oldPosition);
                    }

                    mFileListAdapter.clearData(position);
                    if (mListFile.size() == 0) {
                        showNoDataArea();
                    }
//                    SnackBarUtils.getSnackbar(MainActivity.this, getString(R.string.delete_success_text)).show();
                }
            }

            @Override
            public void onCancel() {

            }
        });
        confirmDialog.show();
    }

    private void printPdfFile(int position) {
        FileData fileData = mFileListAdapter.getFileList().get(position);

        if (FileUtils.getNumberPages(fileData.getFilePath()) == 0) {
            ToastUtils.showMessageShort(this, getString(R.string.can_not_print_this_file));
            return;
        }
        FileUtils.printFile(this, new File(fileData.getFilePath()));
    }

    private void sharePdfFile(int position) {
        FileData fileData = mFileListAdapter.getFileList().get(position);
        FileUtils.shareFile(this, new File(fileData.getFilePath()));
    }

    private void uploadPdfFile(int position) {
        FileData fileData = mFileListAdapter.getFileList().get(position);
        FileUtils.uploadFile(MainActivity.this, new File(fileData.getFilePath()));
    }
}