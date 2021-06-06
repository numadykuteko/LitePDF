package com.value.light.pdf.reader.utils.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.value.light.pdf.reader.R;
import com.value.light.pdf.reader.data.FileData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "FileListAdapter";
    private List<FileData> mFileList = new ArrayList<FileData>();

    private OnFileItemWithOptionClickListener mListener;

    public FileListAdapter(OnFileItemWithOptionClickListener listener) {
        this.mListener = listener;
    }

    public void setData(List<FileData> videoList) {
        mFileList = new ArrayList<>();
        mFileList.addAll(videoList);
        notifyDataSetChanged();
    }

    public void clearAllData() {
        mFileList.clear();
        notifyDataSetChanged();
    }

    public void clearData(int position) {
        if (position < 0 || position > mFileList.size())   return;
        mFileList.remove(position);
        notifyDataSetChanged();
    }

    public void updateData(int position, FileData fileData) {
        mFileList.set(position, fileData);
        notifyItemChanged(position);
    }

    public FileListAdapter() {
    }

    public List<FileData> getFileList() {
        return mFileList;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_view, parent, false);
        return new FileListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((FileListViewHolder) holder).bindView(position, mFileList.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }
}
