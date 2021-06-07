package com.pdf.reader.lite.utils.file;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DirectoryUtils {
    private Context mContext;
    private ArrayList<String> mFilePaths;

    public DirectoryUtils(Context context) {
        mContext = context;
        mFilePaths = new ArrayList<>();
    }

    ArrayList<String> getAllPDFsOnDevice() {
        mFilePaths = new ArrayList<>();
        walkDir(Environment.getExternalStorageDirectory(), Collections.singletonList(".pdf"));
        return mFilePaths;
    }

    private void walkDir(File dir, List<String> extensions) {
        File[] listFile = dir.listFiles();
        if (listFile != null) {
            for (File aListFile : listFile) {

                if (aListFile.isDirectory()) {
                    walkDir(aListFile, extensions);
                } else {
                    for (String extension: extensions) {
                        if (aListFile.getName().toLowerCase().endsWith(extension)) {
                            //Do what ever u want
                            mFilePaths.add(aListFile.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }
}
