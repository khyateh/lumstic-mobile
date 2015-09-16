package com.lumstic.ashoka.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.lumstic.ashoka.utils.TypefaceLoader;

public class RobotoBlackTextView extends TextView {

    public RobotoBlackTextView(Context context) {
        super(context);
        init(context);
    }

    public RobotoBlackTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RobotoBlackTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (!isInEditMode())
            setTypeface(TypefaceLoader.get(context, "Roboto-Medium.ttf"));
    }
}