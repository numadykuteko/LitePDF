package com.pdf.reader.lite.utils;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.pdf.reader.lite.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FirebaseRemoteUtils {
    private static final String VERSION_NAME = "version_name";
    private static final String VERSION_CODE = "version_code";
    private static final String REQUIRED = "required";
    private static final String STATUS = "status";
    private static final String NEW_PACKAGE = "new_package";
    private static final String VERSION_CODE_UPDATE_REQUIRED = "version_code_update_required";
    private static final String CONFIG_UPDATE_PARAM = "config_update_version";
    private static final String FULL_APP_PARAM = "full_app_package_name";
    private static final String NUMBER_PAGE_TO_MAX_PARAM = "number_page_to_max";

    private static final String NUMBER_TIME_SHOW_ADS_ONCE = "number_time_show_ads_once";
    private static final int NUMBER_TIME_SHOW_ADS_ONCE_DEFAULT = 1;
    private static final String FULL_APP_PACKAGE_DEFAULT = "com.pdfreader.scanner.pdfviewer";

    private static final String TIMEOUT_SPLASH = "timeout_splash";
    private static final int TIMEOUT_SPLASH_DEFAULT = 10000;

    private final FirebaseRemoteConfig mFirebaseRemoteConfig;

    public FirebaseRemoteUtils() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        if (BuildConfig.DEBUG) {
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(0)
                    .build();
            mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        }
    }

    public void fetchRemoteConfig(Activity context, Runnable afterRunnable) {

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(context, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            afterRunnable.run();
                        } else {
                            afterRunnable.run();
                        }
                    }
                })
                .addOnFailureListener(context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        afterRunnable.run();
                    }
                });
    }

    public UpdateInfo getUpdateInfo() {
        String contentConfig = mFirebaseRemoteConfig.getString(CONFIG_UPDATE_PARAM);

//        contentConfig = "{'version_code':10, 'version_name':'1.9','required':true,'version_code_update_required':'1,2,3'}";
        if (contentConfig == null || contentConfig.length() == 0) {
            return null;
        }

        String versionName;
        double versionCode;
        boolean isRequired;
        boolean status;
        String newPackage;
        String requiredListString;
        try {
            JSONObject info = new JSONObject(contentConfig);
            versionName = info.getString(VERSION_NAME);
            isRequired = info.getBoolean(REQUIRED);
            status = info.getBoolean(STATUS);
            versionCode = info.getDouble(VERSION_CODE);
            newPackage = info.getString(NEW_PACKAGE);
            requiredListString = info.getString(VERSION_CODE_UPDATE_REQUIRED);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        List<Integer> requiredList = new ArrayList<>();

        String[] splitList = requiredListString.split(",");
        if (splitList.length > 0) {
            for (String s : splitList) {
                try {
                    requiredList.add(Integer.parseInt(s));
                } catch (Exception ignored) {
                }
            }
        }

        UpdateInfo updateInfo = new UpdateInfo();
        updateInfo.setIsRequired(isRequired);
        updateInfo.setRequiredUpdateList(requiredList);
        updateInfo.setVersionCode(versionCode);
        updateInfo.setVersionName(versionName);
        updateInfo.setNewPackage(newPackage);
        updateInfo.setStatus(status);

        Log.d("duynm", updateInfo.getNewPackage());

        return updateInfo;
    }

    public double getNumberTimeShowAdsOnce() {
        double numberTimeShowAdsOnce = mFirebaseRemoteConfig.getDouble(NUMBER_TIME_SHOW_ADS_ONCE);
        if (numberTimeShowAdsOnce == 0) {
            return NUMBER_TIME_SHOW_ADS_ONCE_DEFAULT;
        }

        return numberTimeShowAdsOnce;
    }

    public double getTimeoutSplash() {
        double timeoutSplash = mFirebaseRemoteConfig.getDouble(TIMEOUT_SPLASH);
        if (timeoutSplash == 0) {
            return TIMEOUT_SPLASH_DEFAULT;
        }

        return timeoutSplash;
    }

    public double getNumberPageToMax() {
        double numberPageToMax = mFirebaseRemoteConfig.getDouble(NUMBER_PAGE_TO_MAX_PARAM);
        if (numberPageToMax == 0) {
            return 20;
        }

        return numberPageToMax;
    }

    public String getFullAppPackage() {
        String fullApp = mFirebaseRemoteConfig.getString(FULL_APP_PARAM);
        if (fullApp == null || fullApp.length() == 0) {
            return FULL_APP_PACKAGE_DEFAULT;
        }

        return fullApp;
    }
}
