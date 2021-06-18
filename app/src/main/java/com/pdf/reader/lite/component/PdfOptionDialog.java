//package com.pdf.reader.lite.component;
//
//import android.annotation.SuppressLint;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.constraintlayout.widget.ConstraintLayout;
//
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
//import com.pdf.reader.lite.R;
//import com.pdf.reader.lite.utils.DateTimeUtils;
//
//public class PdfOptionDialog extends Dialog  {
//    private FileOptionListener mListener;
//    private String mNameFile;
//    private int mPosition;
//    private long mDate;
//
//    public PdfOptionDialog() {
//        // Required empty public constructor
//    }
//
//    public PdfOptionDialog(String nameFile, long date, int position, FileOptionListener listener) {
//        this.mListener = listener;
//        mNameFile = nameFile;
//        mPosition = position;
//        mDate = date;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.sheet_dialog_style);
//    }
//
//    @SuppressLint("UseCompatLoadingForDrawables")
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.dialog_pdf_option, container, false);
//        TextView nameFile = v.findViewById(R.id.more_name);
//        nameFile.setText(mNameFile);
//        nameFile.setOnClickListener(view -> {
//            if (mListener != null) {
//                mListener.openFile(mPosition);
//            }
//            dismiss();
//        });
//
//        TextView dateFile = v.findViewById(R.id.more_date);
//        if (mDate != -1) {
//            dateFile.setText(DateTimeUtils.fromTimeUnixToDateString(mDate));
//        } else {
//            dateFile.setText("NA time");
//        }
//
//        ImageView upload = v.findViewById(R.id.more_upload);
//        upload.setOnClickListener(v1 -> {
//            if (mListener != null) {
//                mListener.uploadFile(mPosition);
//            }
//            dismiss();
//        });
//
//        ImageView print = v.findViewById(R.id.more_print);
//        print.setOnClickListener(v1 -> {
//            if (mListener != null) {
//                mListener.printFile(mPosition);
//            }
//            dismiss();
//        });
//
//        ConstraintLayout shareOption = v.findViewById(R.id.more_layout_share);
//        shareOption.setOnClickListener(v1 -> {
//            if (mListener != null) {
//                mListener.shareFile(mPosition);
//            }
//            dismiss();
//        });
//
//        ConstraintLayout renameOption = v.findViewById(R.id.more_layout_rename);
//        renameOption.setOnClickListener(v1 -> {
//            if (mListener != null) {
//                mListener.renameFile(mPosition);
//            }
//            dismiss();
//        });
//
//        ConstraintLayout deleteOption = v.findViewById(R.id.more_layout_delete);
//        deleteOption.setOnClickListener(v1 -> {
//            if (mListener != null) {
//                mListener.deleteFile(mPosition);
//            }
//            dismiss();
//        });
//
//        return v;
//    }
//
//    public interface FileOptionListener {
//        void openFile(int position);
//        void shareFile(int position);
//        void printFile(int position);
//        void uploadFile(int position);
//        void renameFile(int position);
//        void deleteFile(int position);
//    }
//}
