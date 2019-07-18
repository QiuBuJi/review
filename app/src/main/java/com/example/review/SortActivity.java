package com.example.review;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.review.Fragment.OutlineFragment;
import com.example.review.Fragment.SortFragment;

import java.util.ArrayList;

public class SortActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView               imageViewBackButton;
    private Spinner                 spinnerSort;
    private ViewPager               pager;
    private ArrayList<SortFragment> fragments = new ArrayList<>();
    ;
    private       TextView     indicate;
    public static SortFragment fragment;
    public static int          xPosi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort);

        spinnerSort = findViewById(R.id.sort_spinner_sort);
        imageViewBackButton = findViewById(R.id.sort_edit_imageView_back_button);
        pager = findViewById(R.id.sort_viewPager_pager);
        indicate = findViewById(R.id.sort_textView_indicate);

        imageViewBackButton.setOnClickListener(this);

        Intent intent = getIntent();
        int    posi   = intent.getIntExtra("posi", 0);

        fragments.add(new SortFragment(true));
        fragments.add(new SortFragment());
        fragments.add(new OutlineFragment());

        pager.setAdapter(getAdapter());
        pager.addOnPageChangeListener(getListener());
        if (posi > 0) {
            pager.arrowScroll(posi);
        }

        pager.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                xPosi = i;
            }
        });
    }

    private FragmentStatePagerAdapter getAdapter() {
        return new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return fragments.get(i);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        };
    }

    private ViewPager.OnPageChangeListener getListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                fragment = fragments.get(i);
                switch (i) {
                    case 0:
                        indicate.setText("复习中");
                        break;
                    case 1:
                        indicate.setText("待复习");
                        break;
                    case 2:
                        indicate.setText("大纲");
                        break;
                }
//                fragment.displayField = Setting.getInt("displayField");
//                fragment.selectPartToShow(fragment.displayField);
//                fragment.adapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sort_edit_imageView_back_button:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fragment != null) {
            Setting.set("displayField", fragment.displayField);
            fragment = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (fragment == null) return;

        fragment.displayField = Setting.getInt("displayField");

        spinnerSort.setSelection(fragment.displayField, true);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fragment.selectPartToShow(i);
                fragment.adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
}
