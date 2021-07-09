package com.pdf.reader.lite.component;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pdf.reader.lite.R;

public class UpdateDialog extends Dialog {

    private Context mContext;
    private ConfirmListener mListener;

    public UpdateDialog(@NonNull Context context, String versionName, ConfirmListener listener, boolean isRequired) {
        super(context);
        mContext = context;
        mListener = listener;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_update_require);

        int width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.96);
        getWindow().setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT);

        Button submitBtn = findViewById(R.id.btn_ok);
        TextView versionNameTv = findViewById(R.id.version_info);

        versionNameTv.setText(getContext().getString(R.string.update_version_info, versionName));

        submitBtn.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onSubmit();
            }
        });

        setCancelable(!isRequired);
        setCanceledOnTouchOutside(!isRequired);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public interface ConfirmListener {
        void onSubmit();
    }
}
