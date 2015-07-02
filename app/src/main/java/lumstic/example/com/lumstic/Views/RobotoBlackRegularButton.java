package lumstic.example.com.lumstic.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import lumstic.example.com.lumstic.Utils.TypefaceLoader;

/**
 * Created by work on 29/5/15.
 */
public class RobotoBlackRegularButton extends Button {
    public RobotoBlackRegularButton (Context context) {
        super(context);
        init(context);
    }

    public RobotoBlackRegularButton (Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RobotoBlackRegularButton (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (!isInEditMode())
            setTypeface(TypefaceLoader.get(context, "Roboto-Regular.ttf"));

    }

}