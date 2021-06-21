package com.pdf.reader.lite.utils.adapter;

import android.view.View;

public interface OnFileItemWithOptionClickListener {
    void onClickItem(int position);
    void onOptionItem(View view, int position);
}
