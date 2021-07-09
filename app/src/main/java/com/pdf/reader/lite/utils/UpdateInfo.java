package com.pdf.reader.lite.utils;

import java.util.ArrayList;
import java.util.List;

public class UpdateInfo {
    private String mVersionName;
    private double mVersionCode;
    private boolean mIsRequired;
    private boolean mStatus;
    private String mNewPackage;
    private List<Integer> mRequiredUpdateList = new ArrayList<>();

    public UpdateInfo() {

    }

    public String getVersionName() {
        return mVersionName;
    }

    public void setVersionName(String mVersionName) {
        this.mVersionName = mVersionName;
    }

    public double getVersionCode() {
        return mVersionCode;
    }

    public void setVersionCode(double mVersionCode) {
        this.mVersionCode = mVersionCode;
    }

    public boolean isIsRequired() {
        return mIsRequired;
    }

    public void setIsRequired(boolean mIsRequired) {
        this.mIsRequired = mIsRequired;
    }

    public List<Integer> getRequiredUpdateList() {
        return mRequiredUpdateList;
    }

    public void setRequiredUpdateList(List<Integer> mRequiredUpdateList) {
        this.mRequiredUpdateList = mRequiredUpdateList;
    }

    public boolean getStatus() {
        return mStatus;
    }

    public void setStatus(boolean mStatus) {
        this.mStatus = mStatus;
    }

    public String getNewPackage() {
        return mNewPackage;
    }

    public void setNewPackage(String mNewPackage) {
        this.mNewPackage = mNewPackage;
    }
}
