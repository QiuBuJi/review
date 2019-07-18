package com.example.review.Animator;

import android.animation.ValueAnimator;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

public class TextPartColorAnimator {


    public static ValueAnimator ofArgb(final Editable editable, final int start, final int end, int... color) {
        ValueAnimator valueAnimator = ValueAnimator.ofArgb(color);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ForegroundColorSpan what;

                int textColor = (int) animation.getAnimatedValue();

                what = new ForegroundColorSpan(textColor);

                editable.setSpan(what, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        });
        return valueAnimator;
    }
}
