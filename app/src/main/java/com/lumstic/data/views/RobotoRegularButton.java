package com.lumstic.data.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;


public class RobotoRegularButton extends Button {
    public RobotoRegularButton(Context context) {
        super(context);
        init(context);
    }

    public RobotoRegularButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RobotoRegularButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (!isInEditMode())
            setTypeface(TypefaceLoader.getInstance(context, "Roboto-Regular.ttf").getTypeFace());

    }

}