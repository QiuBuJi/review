package com.example.review;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity {


    TextView  textViewTitle;
    ImageView imageViewBackButton;
    ImageView imageView;
    private int cout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);


        textViewTitle = findViewById(R.id.main_about_textView_title);
        imageViewBackButton = findViewById(R.id.about_imageView_back_button);
        imageView = findViewById(R.id.imageView);


        imageViewBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //单击动画
                ObjectAnimator scalex = ObjectAnimator.ofFloat(imageView, "scaleX", 1, 0.5f, 1, 0.5f, 1);
                ObjectAnimator scaley = ObjectAnimator.ofFloat(imageView, "scaleY", 1, 0.5f, 1, 0.5f, 1);
                Interpolator   value  = new LinearInterpolator();
                scalex.setInterpolator(value);
                scaley.setInterpolator(value);
                scalex.setDuration(600);
                scaley.setDuration(600);
                scalex.start();
                scaley.start();

                cout++;
                if (cout == 12) {
                    ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                    layoutParams.width = (int) (imageView.getWidth() * 1.6f);
                    layoutParams.height = (int) (imageView.getHeight() * 1.6f);
                    imageView.requestLayout();
                } else if (cout > 6) {
                    imageView.setImageResource(R.mipmap.master);
                }
            }
        });
    }
}
