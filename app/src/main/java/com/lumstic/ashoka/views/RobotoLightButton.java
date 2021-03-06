package com.lumstic.ashoka.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;


public class RobotoLightButton extends Button {
    public RobotoLightButton(Context context) {
        super(context);
        init(context);
    }

    public RobotoLightButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RobotoLightButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (!isInEditMode())
            setTypeface(TypefaceLoader.getInstance(context, "Roboto-Light.ttf").getTypeFace());
    }

}
