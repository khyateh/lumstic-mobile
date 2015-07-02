package lumstic.example.com.lumstic.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;


import lumstic.example.com.lumstic.Utils.TypefaceLoader;

/**
 * Created by poorva on 9/10/14.
 */
public class RobotoLightTextView extends TextView{

    public RobotoLightTextView(Context context) {
        super(context);
        init(context);
    }

    public RobotoLightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RobotoLightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (!isInEditMode())
            setTypeface(TypefaceLoader.get(context, "Roboto-Light.ttf"));

    }

}
