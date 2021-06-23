package com.pdf.reader.lite.utils.rate;

public interface OnCallback {

    void onMaybeLater();

    void onSubmit(String review);

    void onRate();

}
