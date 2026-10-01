package com.dastbigharar.focus;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public final class Ui {
    public static final int PURPLE = Color.rgb(107,92,255);
    public static final int PURPLE_DARK = Color.rgb(81,68,217);
    public static final int SOFT = Color.rgb(240,238,255);
    public static final int BG = Color.rgb(247,247,251);
    public static final int TEXT = Color.rgb(23,25,35);
    public static final int MUTED = Color.rgb(123,129,144);

    private Ui() {}

    public static int dp(Context c, int v) {
        return (int)(v * c.getResources().getDisplayMetrics().density + .5f);
    }

    public static GradientDrawable bg(int color, float radiusDp, Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, (int)radiusDp));
        return g;
    }

    public static GradientDrawable strokeBg(int fill, int stroke, int strokeDp, int radiusDp, Context c) {
        GradientDrawable g = bg(fill, radiusDp, c);
        g.setStroke(dp(c, strokeDp), stroke);
        return g;
    }

    public static TextView text(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    public static void margins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
            android.view.ViewGroup.MarginLayoutParams p = (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(dp(v.getContext(),l),dp(v.getContext(),t),dp(v.getContext(),r),dp(v.getContext(),b));
            v.setLayoutParams(p);
        }
    }
}
