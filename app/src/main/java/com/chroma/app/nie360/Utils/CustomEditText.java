package com.chroma.app.nie360.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class CustomEditText extends android.support.v7.widget.AppCompatEditText {

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super( context, attrs, defStyle );
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super( context, attrs );
        init();
    }

    public CustomEditText(Context context) {
        super( context );
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.DEFAULT;

            switch (getTypeface().getStyle()) {
                case Typeface.BOLD:
                    tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/raleway_bold.ttf");
                    break;

                case Typeface.NORMAL:
                    tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/raleway_light.ttf");
                    break;

                case Typeface.ITALIC:
                    tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/raleway_regular.ttf");
                    break;

            }
            setTypeface(tf);
        }
    }

}