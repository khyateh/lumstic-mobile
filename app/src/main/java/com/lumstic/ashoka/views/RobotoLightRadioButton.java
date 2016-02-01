package com.lumstic.ashoka.views;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

public class RobotoLightRadioButton extends RadioButton {
    public RobotoLightRadioButton(Context context) {
        super(context);
        init(context);
    }

    public RobotoLightRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RobotoLightRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context) {
        if (!isInEditMode())
            setTypeface(TypefaceLoader.getInstance(context, "Roboto-Light.ttf").getTypeFace());

    }
}
