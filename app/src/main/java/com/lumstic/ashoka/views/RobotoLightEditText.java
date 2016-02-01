package com.lumstic.ashoka.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;


public class RobotoLightEditText extends EditText {
    public RobotoLightEditText(Context context) {
        super(context);
        init(context);
    }

    public RobotoLightEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RobotoLightEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (!isInEditMode())
            setTypeface(TypefaceLoader.getInstance(context, "Roboto-Light.ttf").getTypeFace());
    }
}


