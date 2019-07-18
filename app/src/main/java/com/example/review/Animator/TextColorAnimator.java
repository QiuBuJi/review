package com.example.review.Animator;

import android.animation.ValueAnimator;
import android.widget.TextView;

public class TextColorAnimator {
    public static ValueAnimator ofArgb(final TextView view, int... values) {
        ValueAnimator valueAnimator = ValueAnimator.ofArgb(values);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setTextColor((int) animation.getAnimatedValue());
            }
        });
        return valueAnimator;
    }
}


