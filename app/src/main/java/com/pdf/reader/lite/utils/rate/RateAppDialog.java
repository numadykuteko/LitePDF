package com.pdf.reader.lite.utils.rate;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.pdf.reader.lite.R;
import com.ymb.ratingbar_lib.RatingBar;

public class RateAppDialog extends Dialog {
    private Handler handler;
    private OnCallback callback;
    private EditText edtContent;
    private Runnable rd;

    public void setCallback(OnCallback callback) {
        this.callback = callback;
    }

    public RateAppDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_rate);
    }

    @Override
    public void show() {
        super.show();
        initView();
    }

    private void initView() {
        setCancelable(false);
        RatingBar rating = findViewById(R.id.rating);
        edtContent = findViewById(R.id.edt_content);
        this.findViewById(R.id.tv_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                callback.onSubmit(edtContent.getText().toString());
            }
        });
        findViewById(R.id.ln_later).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                callback.onMaybeLater();
            }
        });
        rating.setOnRatingChangedListener(new RatingBar.OnRatingChangedListener() {
            @Override
            public void onRatingChange(final float v, final float v1) {
                if (handler != null && rd != null) {
                    handler.removeCallbacks(rd);
                }
                handler = new Handler();
                rd = new Runnable() {
                    @Override
                    public void run() {
                            if (v1 < 4.0) {
                                findViewById(R.id.ln_feedback).setVisibility(View.VISIBLE);
                                findViewById(R.id.ln_later).setVisibility(View.GONE);
                                return;
                            }
                            dismiss();
                            callback.onRate();
                    }
                };
                handler.postDelayed(rd,200);
            }
        });

    }
}
