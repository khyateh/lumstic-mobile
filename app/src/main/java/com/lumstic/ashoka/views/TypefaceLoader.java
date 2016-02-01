package com.lumstic.ashoka.views;

import android.content.Context;
import android.graphics.Typeface;

public class TypefaceLoader {

    private static TypefaceLoader instance;
    private static Typeface typeface;

    private TypefaceLoader() {

    }

    public static TypefaceLoader getInstance(Context context, String fontPath) {
        synchronized (TypefaceLoader.class) {
            if (instance == null) {
                instance = new TypefaceLoader();
                typeface = Typeface.createFromAsset(context.getAssets(), fontPath);
            }
            return instance;
        }
    }

    public Typeface getTypeFace() {
        return typeface;
    }
}
