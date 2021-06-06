package com.value.light.pdf.reader.lib;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.value.light.pdf.reader.R;
import com.value.light.pdf.reader.utils.ColorUtils;

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
            if (viewMode == 1) {
                imageView.setBackgroundColor(ColorUtils.getColorFromResource(context, R.color.black_totally));
            } else {
                imageView.setBackgroundColor(ColorUtils.getColorFromResource(context, R.color.white));
            }
            imageView.setImageBitmap(listener.showPage(position));
            container.addView(itemView);

            return itemView;
        }

        return null;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
