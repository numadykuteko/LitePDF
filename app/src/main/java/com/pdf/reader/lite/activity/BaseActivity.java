package com.pdf.reader.lite.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @SuppressLint("RestrictedApi")
    public void setNoActionBar() {
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setShowHideAnimationEnabled(false);

            actionBar.hide();
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @SuppressLint("RestrictedApi")
    public void setActionBar(String title, boolean isShowBackButton) {
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setShowHideAnimationEnabled(false);

            actionBar.show();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(title);

            actionBar.setHomeButtonEnabled(isShowBackButton);
            actionBar.setDisplayHomeAsUpEnabled(isShowBackButton);
        }
    }

    public boolean notHaveStoragePermission() {
        return (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
    }

    public boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public void requestReadStoragePermissionsSafely(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
        }
    }
}
