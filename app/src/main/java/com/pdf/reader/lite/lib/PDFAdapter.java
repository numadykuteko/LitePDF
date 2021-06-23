package com.pdf.reader.lite.lib;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.pdf.reader.lite.R;
import com.pdf.reader.lite.utils.ColorUtils;

public class PDFAdapter extends PagerAdapter {

    private int page_count;
    private Context context;
    private IShowPage listener;
    private int viewMode;

    public PDFAdapter(Context context, IShowPage listener, int page_count, int viewMode) {
        this.context = context;
        this.listener = listener;
        this.page_count = page_count;
        this.viewMode = viewMode;
    }

    @Override
    public int getCount() {
        return page_count;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView;
        if (inflater != null) {
            itemView = inflater.inflate(R.layout.each_page, container, false);
            PDFZoomImageView imageView = (PDFZoomImageView ) itemView.findViewById(R.id.image);
            LinearLayout reloadView = (LinearLayout) itemView.findViewById(R.id.reload);

            if (viewMode == 1) {
                imageView.setBackgroundColor(ColorUtils.getColorFromResource(context, R.color.black_totally));
            } else {
                imageView.setBackgroundColor(ColorUtils.getColorFromResource(context, R.color.white));
            }
            imageView.setVisibility(View.GONE);
            reloadView.setVisibility(View.GONE);

            listener.showPage(container, imageView, reloadView, position);
            container.addView(itemView);

            return itemView;
        }

        return null;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
