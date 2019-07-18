package com.example.review.Animator;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;

import com.example.review.DataStructureFile.ElementCategory;

import java.util.ArrayList;

public class TextColorGroupAnimator {

    static ValueAnimator ofEc(final EditText view, final ArrayList<ElementCategory> ecs) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 255);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();

                int      begin = 0, end = 0;
                Editable et    = view.getEditableText();


                for (ElementCategory ec : ecs) {
                    end = begin + ec.txt.length();
                    int abc = 0xff;

                    switch (ec.category) {
                        case correct:
                            abc = Color.BLACK;
                            et.setSpan(new ForegroundColorSpan(abc), begin, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                            break;
                        case malposition:

                            abc <<= 8;
                            abc <<= 8;
                            abc <<= 8;

                            value <<= 8;
                            value <<= 8;
                            abc |= value;
                            et.setSpan(new ForegroundColorSpan(abc), begin, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                            break;
                        case unnecesary:
                            if (value > 0xc3) value = 0xc3;

                            for (int i = 0; i < 3; i++) {
                                abc <<= 8;
                                abc |= value;
                            }
                            et.setSpan(new ForegroundColorSpan(abc), begin, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                            break;
                    }
                    begin = end;
                }
            }
        });
        return valueAnimator;
    }

    @NonNull
    private static StringBuffer toHexText(int rgb) {
        final int    baseNum = 16;
        StringBuffer hex     = new StringBuffer();
        char[]       hexList = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        if (rgb != 0) {
            while (rgb != 0) {
                int mode = rgb % baseNum;
                rgb /= baseNum;
                char hex1 = hexList[mode];
                hex.insert(0, hex1);
            }
        } else {
            hex.append("0");
        }

        int len = 2 - hex.length();
        for (int i = 0; i < len; i++) hex.insert(0, "0");
        return hex;
    }

}
