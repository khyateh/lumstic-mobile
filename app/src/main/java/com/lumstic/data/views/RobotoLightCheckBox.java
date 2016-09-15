package com.lumstic.data.views;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

public class RobotoLightCheckBox extends CheckBox {
    public RobotoLightCheckBox(Context context) {
        super(context);
        init(context);
    }

    public RobotoLightCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RobotoLightCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context) {
        if (!isInEditMode())
            setTypeface(TypefaceLoader.getInstance(context, "Roboto-Light.ttf").getTypeFace());

    }

}
