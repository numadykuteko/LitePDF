package com.pdf.reader.lite.data;

import java.io.Serializable;

public class ViewPdfOption implements Serializable {
    private int mViewMode;
    private int mOrientation;

    public ViewPdfOption(int mViewMode, int mOrientation) {
        this.mViewMode = mViewMode;
        this.mOrientation = mOrientation;
    }

    public int getViewMode() {
        return mViewMode;
    }

    public void setViewMode(int mViewMode) {
        this.mViewMode = mViewMode;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(int mOrientation) {
        this.mOrientation = mOrientation;
    }
}
