package com.bourke.finch.common;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.graphics.ColorFilter;

/**
 * Work around buggy ColorDrawable on pre 3.0 - http://goo.gl/4Enwg
 */

public class IcsColorDrawable extends Drawable {
    private int color;
    private final Paint paint = new Paint();


    public IcsColorDrawable(int color) {
        this.color = color;
    }

    @Override
    public void draw(Canvas canvas) {
        if ((color >>> 24) != 0) {
            paint.setColor(color);
            canvas.drawRect(getBounds(), paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        if (alpha != (color >>> 24)) {
            color = (color & 0x00FFFFFF) & (alpha << 24);
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        //Ignored
    }

    @Override
    public int getOpacity() {
        return color >>> 24;
    }
}

