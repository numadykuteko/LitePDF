package com.pdf.reader.lite.utils.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.pdf.reader.lite.R;
import com.pdf.reader.lite.data.FileData;
import com.pdf.reader.lite.utils.DateTimeUtils;
import com.pdf.reader.lite.utils.file.FileUtils;

public class FileListViewHolder extends RecyclerView.ViewHolder {
    private ConstraintLayout mContentView;
    private ImageView mImageView;
    private ImageView mMoreView;
    private TextView mNameView;
    private TextView mDateTextView;

    public FileListViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mImageView = itemView.findViewById(R.id.item_image_view);
        mMoreView = itemView.findViewById(R.id.item_more_view);
        mNameView = itemView.findViewById(R.id.item_name_view);
        mDateTextView = itemView.findViewById(R.id.item_date_text_view);
    }

    @SuppressLint({"StaticFieldLeak", "UseCompatLoadingForDrawables", "SetTextI18n"})
    public void bindView(int position, FileData fileData, OnFileItemWithOptionClickListener listener) {

        if (fileData.getFilePath() != null) {
            mNameView.setText(fileData.getDisplayName());
        }

        mDateTextView.setVisibility(View.VISIBLE);

        if (fileData.getTimeAdded() > 0) {
            String text = itemView.getContext().getString(R.string.full_detail_file, DateTimeUtils.fromTimeUnixToDateTimeString(fileData.getTimeAdded()),
                    FileUtils.getFormattedSize(fileData.getSize()));
            mDateTextView.setText(text);
        } else {
            mDateTextView.setText(FileUtils.getFormattedSize(fileData.getSize()));
        }

        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });

        mMoreView.setOnClickListener(view -> {
            listener.onOptionItem(mMoreView, position);
        });

        mContentView.setLongClickable(true);
        mContentView.setOnLongClickListener(v -> {
            listener.onOptionItem(mMoreView, position);
            return true;
        });
    }
}
