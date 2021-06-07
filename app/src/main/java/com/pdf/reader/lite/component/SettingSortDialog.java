package com.pdf.reader.lite.component;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.DrawableCompat;

import com.pdf.reader.lite.R;
import com.pdf.reader.lite.utils.ColorUtils;
import com.pdf.reader.lite.utils.file.FileUtils;

public class SettingSortDialog extends Dialog {
    private static final String TAG = "SettingSortDialog";
    private Context mContext;

    private SettingSortSubmit mListener;
    private int mCurrentSort;

    private ConstraintLayout mSortNameLayout;
    private ConstraintLayout mSortSizeLayout;
    private ConstraintLayout mSortDateLayout;
    private ConstraintLayout mSortAscLayout;
    private ConstraintLayout mSortDescLayout;

    private TextView mSortNameTv;
    private TextView mSortSizeTv;
    private TextView mSortDateTv;
    private TextView mSortAscTv;
    private TextView mSortDescTv;

    private ImageView mSortNameImg;
    private ImageView mSortSizeImg;
    private ImageView mSortDateImg;
    private ImageView mSortAscImg;
    private ImageView mSortDescImg;

    private RadioButton mSortNameCheckbox;
    private RadioButton mSortSizeCheckbox;
    private RadioButton mSortDateCheckbox;
    private RadioButton mSortAscCheckbox;
    private RadioButton mSortDescCheckbox;

    private Button mCancelButton;
    private Button mSubmitButton;

    private int mActiveColor, mDeactiveColor;

    public SettingSortDialog(@NonNull Context context, SettingSortSubmit listener, int currentSort) {
        super(context);
        mListener = listener;
        mContext = context;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.gravity = Gravity.CENTER;
        getWindow().setAttributes(wlp);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_sort);

        int width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.9);
        int height = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.8);
        getWindow().setLayout(width, height);

        mSortNameLayout = findViewById(R.id.sort_by_layout_name);
        mSortSizeLayout = findViewById(R.id.sort_by_layout_file_size);
        mSortDateLayout = findViewById(R.id.sort_by_layout_create_date);
        mSortAscLayout = findViewById(R.id.sort_by_layout_asc);
        mSortDescLayout = findViewById(R.id.sort_by_layout_desc);

        mSortNameCheckbox = findViewById(R.id.sort_by_checked_name);
        mSortSizeCheckbox = findViewById(R.id.sort_by_checked_file_size);
        mSortDateCheckbox = findViewById(R.id.sort_by_checked_create_date);
        mSortAscCheckbox = findViewById(R.id.sort_by_checked_asc);
        mSortDescCheckbox = findViewById(R.id.sort_by_checked_desc);

        mSortNameTv = findViewById(R.id.sort_by_textview_name);
        mSortSizeTv = findViewById(R.id.sort_by_textview_file_size);
        mSortDateTv = findViewById(R.id.sort_by_textview_create_date);
        mSortAscTv = findViewById(R.id.sort_by_textview_asc);
        mSortDescTv = findViewById(R.id.sort_by_textview_desc);

        mSortNameImg = findViewById(R.id.sort_by_imageview_name);
        mSortSizeImg = findViewById(R.id.sort_by_imageview_file_size);
        mSortDateImg = findViewById(R.id.sort_by_imageview_create_date);
        mSortAscImg = findViewById(R.id.sort_by_imageview_asc);
        mSortDescImg = findViewById(R.id.sort_by_imageview_desc);

        mCancelButton = findViewById(R.id.sort_btn_cancel);
        mSubmitButton = findViewById(R.id.sort_btn_ok);

        mActiveColor = ColorUtils.getColorFromResource(getContext(), R.color.gntRed);
        mDeactiveColor = ColorUtils.getColorFromResource(getContext(), R.color.black);

        mCurrentSort = currentSort;
        fillDataDialog();

        mSortNameLayout.setOnClickListener(v -> {
            resetCheckBoxSortBy();
            mSortNameCheckbox.setChecked(true);

            mSortNameTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortNameImg.getDrawable(), mActiveColor);
        });

        mSortNameCheckbox.setOnClickListener(v -> {
            resetCheckBoxSortBy();
            mSortNameCheckbox.setChecked(true);

            mSortNameTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortNameImg.getDrawable(), mActiveColor);
        });

        mSortDateLayout.setOnClickListener(v -> {
            resetCheckBoxSortBy();
            mSortDateCheckbox.setChecked(true);

            mSortDateTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortDateImg.getDrawable(), mActiveColor);
        });

        mSortDateCheckbox.setOnClickListener(v -> {
            resetCheckBoxSortBy();
            mSortDateCheckbox.setChecked(true);

            mSortDateTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortDateImg.getDrawable(), mActiveColor);
        });

        mSortSizeLayout.setOnClickListener(v -> {
            resetCheckBoxSortBy();
            mSortSizeCheckbox.setChecked(true);

            mSortSizeTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortSizeImg.getDrawable(), mActiveColor);
        });

        mSortSizeCheckbox.setOnClickListener(v -> {
            resetCheckBoxSortBy();
            mSortSizeCheckbox.setChecked(true);

            mSortSizeTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortSizeImg.getDrawable(), mActiveColor);
        });

        mSortAscLayout.setOnClickListener(v -> {
            resetCheckBoxSortType();
            mSortAscCheckbox.setChecked(true);

            mSortAscTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortAscImg.getDrawable(), mActiveColor);
        });

        mSortAscCheckbox.setOnClickListener(v -> {
            resetCheckBoxSortType();
            mSortAscCheckbox.setChecked(true);

            mSortAscTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortAscImg.getDrawable(), mActiveColor);
        });

        mSortDescLayout.setOnClickListener(v -> {
            resetCheckBoxSortType();
            mSortDescCheckbox.setChecked(true);

            mSortDescTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortDescImg.getDrawable(), mActiveColor);
        });

        mSortDescCheckbox.setOnClickListener(v -> {
            resetCheckBoxSortType();
            mSortDescCheckbox.setChecked(true);

            mSortDescTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortDescImg.getDrawable(), mActiveColor);
        });

        mCancelButton.setOnClickListener(v -> dismiss());

        mSubmitButton.setOnClickListener(v -> {
            int newSort;
            int sortType = 0;
            int sortBy = 0;

            if (mSortNameCheckbox.isChecked()) {
                sortBy = 1;
            } else if (mSortSizeCheckbox.isChecked()) {
                sortBy = 2;
            }

            if (mSortDescCheckbox.isChecked()) {
                sortType = 1;
            }

            newSort = sortBy * 2 + sortType;
            if (newSort != mCurrentSort) {
                if (mListener != null) {
                    mListener.updateNewSort(newSort);
                }
            }

            dismiss();
        });
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @SuppressLint("SetTextI18n")
    private void fillDataDialog() {
        resetCheckBoxSortBy();
        resetCheckBoxSortType();

        if (mCurrentSort == FileUtils.SORT_BY_DATE_ASC) {
            mSortDateCheckbox.setChecked(true);
            mSortAscCheckbox.setChecked(true);

            mSortDateTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortDateImg.getDrawable(), mActiveColor);

            mSortAscTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortAscImg.getDrawable(), mActiveColor);
        } else if (mCurrentSort == FileUtils.SORT_BY_DATE_DESC) {
            mSortDateCheckbox.setChecked(true);
            mSortDescCheckbox.setChecked(true);

            mSortDescTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortDateImg.getDrawable(), mActiveColor);

            mSortDateTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortDescImg.getDrawable(), mActiveColor);
        } else if (mCurrentSort == FileUtils.SORT_BY_NAME_ASC) {
            mSortNameCheckbox.setChecked(true);
            mSortAscCheckbox.setChecked(true);

            mSortNameTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortNameImg.getDrawable(), mActiveColor);

            mSortAscTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortAscImg.getDrawable(), mActiveColor);
        } else if (mCurrentSort == FileUtils.SORT_BY_NAME_DESC) {
            mSortNameCheckbox.setChecked(true);
            mSortDescCheckbox.setChecked(true);

            mSortNameTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortNameImg.getDrawable(), mActiveColor);

            mSortDescTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortDescImg.getDrawable(), mActiveColor);
        } else if (mCurrentSort == FileUtils.SORT_BY_SIZE_ASC) {
            mSortSizeCheckbox.setChecked(true);
            mSortAscCheckbox.setChecked(true);

            mSortSizeTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortSizeImg.getDrawable(), mActiveColor);

            mSortAscTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortAscImg.getDrawable(), mActiveColor);
        } else if (mCurrentSort == FileUtils.SORT_BY_SIZE_DESC) {
            mSortSizeCheckbox.setChecked(true);
            mSortDescCheckbox.setChecked(true);

            mSortSizeTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortSizeImg.getDrawable(), mActiveColor);

            mSortDescTv.setTextColor(mActiveColor);
            DrawableCompat.setTint(mSortDescImg.getDrawable(), mActiveColor);
        }
    }

    private void resetCheckBoxSortBy() {
        mSortNameCheckbox.setChecked(false);
        mSortSizeCheckbox.setChecked(false);
        mSortDateCheckbox.setChecked(false);

        mSortNameTv.setTextColor(mDeactiveColor);
        DrawableCompat.setTint(mSortNameImg.getDrawable(), mDeactiveColor);

        mSortDateTv.setTextColor(mDeactiveColor);
        DrawableCompat.setTint(mSortDateImg.getDrawable(), mDeactiveColor);

        mSortSizeTv.setTextColor(mDeactiveColor);
        DrawableCompat.setTint(mSortSizeImg.getDrawable(), mDeactiveColor);
    }

    private void resetCheckBoxSortType() {
        mSortAscCheckbox.setChecked(false);
        mSortDescCheckbox.setChecked(false);

        mSortAscTv.setTextColor(mDeactiveColor);
        DrawableCompat.setTint(mSortAscImg.getDrawable(), mDeactiveColor);

        mSortDescTv.setTextColor(mDeactiveColor);
        DrawableCompat.setTint(mSortDescImg.getDrawable(), mDeactiveColor);
    }

    public interface SettingSortSubmit {
        public void updateNewSort(int newSort);
    }
}

