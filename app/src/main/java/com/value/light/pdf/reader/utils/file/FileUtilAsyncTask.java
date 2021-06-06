package com.value.light.pdf.reader.utils.file;

import android.content.Context;
import android.os.AsyncTask;

import com.value.light.pdf.reader.data.FileData;

import java.util.ArrayList;
import java.util.List;

public class FileUtilAsyncTask extends AsyncTask<Object, Object, Object> {

    private final Context mContext;
    private FileListener mListener;
    private int mOrder;

    public FileUtilAsyncTask(Context context, FileListener listener, int order) {
        mContext = context;
        mListener = listener;
        this.mOrder = order;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected Object doInBackground(Object... objects) {
        try {
            ArrayList<FileData> allData;
            allData = FileUtils.getAllExternalFileList(mContext, mOrder);

            if (!isCancelled() && mListener != null) {
                mListener.loadDone(allData);
            }
        } catch (Exception e) {
            mListener.loadDone(new ArrayList<>());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }

    public interface FileListener {
        void loadDone(List<FileData> result);
    }
}
