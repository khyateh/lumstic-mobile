package lumstic.example.com.lumstic.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;



import lumstic.example.com.lumstic.Utils.TypefaceLoader;

public class RobotoBlackTextView extends TextView {

    public RobotoBlackTextView(Context context) {
        super(context);
        init(context);
    }

    public RobotoBlackTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RobotoBlackTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (!isInEditMode())
            setTypeface(TypefaceLoader.get(context, "Roboto-Medium.ttf"));
    }
}