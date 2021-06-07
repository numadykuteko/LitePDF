package com.pdf.reader.lite.data;

import android.net.Uri;

public class FileData {
    private String displayName;
    private Uri fileUri;
    private int dateAdded;
    private int size;
    private String filePath;

    public FileData() {

    }

    public FileData(String displayName, String filePath, Uri fileUri, int dateAdded, int size) {
        this.displayName = displayName;
        this.fileUri = fileUri;
        this.dateAdded = dateAdded;
        this.size = size;
        this.filePath = filePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public int getTimeAdded() {
        return dateAdded;
    }

    public void setDateAdded(int dateAdded) {
        this.dateAdded = dateAdded;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
