package com.lumstic.ashoka.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.lumstic.ashoka.utils.TypefaceLoader;


public class RobotoBlackRegularButton extends Button {
    public RobotoBlackRegularButton(Context context) {
        super(context);
        init(context);
    }

    public RobotoBlackRegularButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RobotoBlackRegularButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (!isInEditMode())
            setTypeface(TypefaceLoader.get(context, "Roboto-Regular.ttf"));

    }

}