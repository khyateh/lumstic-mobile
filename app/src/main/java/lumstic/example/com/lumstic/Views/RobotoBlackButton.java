package lumstic.example.com.lumstic.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;


import lumstic.example.com.lumstic.Utils.TypefaceLoader;

/**
 * Created by poorva on 27/9/14.
 */
public class RobotoBlackButton extends Button {
    public RobotoBlackButton(Context context) {
        super(context);
        init(context);
    }

    public RobotoBlackButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RobotoBlackButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (!isInEditMode())
            setTypeface(TypefaceLoader.get(context, "Roboto-Medium.ttf"));

}

}
