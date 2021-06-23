package com.pdf.reader.lite.lib;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public interface IShowPage {

    void showPage(ViewGroup container, ImageView imageView, LinearLayout reloadView, int index);
}
