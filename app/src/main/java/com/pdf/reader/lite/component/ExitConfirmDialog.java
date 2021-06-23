package com.pdf.reader.lite.component;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pdf.reader.lite.BuildConfig;
import com.pdf.reader.lite.R;
import com.pdf.reader.lite.utils.AdsUtils;

public class ExitConfirmDialog extends Dialog {

    private Context mContext;
    private ConfirmListener mListener;

    public ExitConfirmDialog(@NonNull Context context, ConfirmListener listener) {
        super(context);
        mContext = context;
        mListener = listener;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_confirm_exit);

        int width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.95);
        getWindow().setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT);

        Button cancelBtn = findViewById(R.id.btn_no);
        Button submitBtn = findViewById(R.id.btn_yes);

        cancelBtn.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCancel();
            }
            dismiss();
        });
        submitBtn.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onSubmit();
            }
            dismiss();
        });

        setOnShowListener(dialogInterface -> {
            FrameLayout adsContainer = findViewById(R.id.fl_adplaceholder);
            AdsUtils.loadNative(getContext(), adsContainer, BuildConfig.native_id, R.layout.native_admob_ad);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public interface ConfirmListener {
        void onSubmit();
        void onCancel();
    }
}
