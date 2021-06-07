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

public class NoticeDialog extends Dialog {

    private Context mContext;
    private ConfirmListener mListener;

    public NoticeDialog(@NonNull Context context, String titleString, String messageString, String actionButton, ConfirmListener listener) {
        super(context);
        mContext = context;
        mListener = listener;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_notice);

        int width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.9);
        getWindow().setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT);

        Button submitBtn = findViewById(R.id.btn_yes);
        TextView title = findViewById(R.id.title);
        TextView message = findViewById(R.id.question);

        title.setText(titleString);
        message.setText(messageString);
        submitBtn.setText(actionButton);

        submitBtn.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onSubmit();
            }
            dismiss();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public interface ConfirmListener {
        void onSubmit();
    }
}
