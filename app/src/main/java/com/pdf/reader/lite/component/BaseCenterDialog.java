package com.pdf.reader.lite.component;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.pdf.reader.lite.utils.CommonUtils;

public class BaseCenterDialog extends Dialog {
    public BaseCenterDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public void dismiss() {
        hideKeyboard();
        super.dismiss();
    }

    private void hideKeyboard() {
        CommonUtils.hideKeyboard(getOwnerActivity());
    }

}
