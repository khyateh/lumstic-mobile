package lumstic.ashoka.com.lumstic.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import lumstic.ashoka.com.lumstic.Utils.TypefaceLoader;

public class RobotoRegularTextView extends TextView {

    public RobotoRegularTextView(Context context) {
        super(context);
        init(context);
    }

    public RobotoRegularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RobotoRegularTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (!isInEditMode())
            setTypeface(TypefaceLoader.get(context, "Roboto-Regular.ttf"));

    }

}
