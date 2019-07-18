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
    private int count;

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
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1, 0.5f, 1, 0.5f, 1);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1, 0.5f, 1, 0.5f, 1);
                Interpolator   value  = new LinearInterpolator();
                scaleX.setInterpolator(value);
                scaleY.setInterpolator(value);
                scaleX.setDuration(600);
                scaleY.setDuration(600);
                scaleX.start();
                scaleY.start();

                count++;
                if (count == 12) {
                    ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                    layoutParams.width = (int) (imageView.getWidth() * 1.6f);
                    layoutParams.height = (int) (imageView.getHeight() * 1.6f);
                    imageView.requestLayout();
                } else if (count > 6) {
                    imageView.setImageResource(R.mipmap.master);
                }
            }
        });
    }
}
