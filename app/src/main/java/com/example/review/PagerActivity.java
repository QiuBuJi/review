package com.example.review;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.review.Fragment.MyFragment;

import java.util.ArrayList;

public class PagerActivity extends AppCompatActivity {

    private ViewPager         pager;
    private PagerTabStrip     pagerTitle;
    private ArrayList<String> titles;
    private TabLayout         tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        pager = findViewById(R.id.pager_viewPager);
        tabLayout = findViewById(R.id.pager_tabLayout);

        tabLayout.addTab(tabLayout.newTab().setText("you shit"));
        SpannableString ss = new SpannableString("you");

        ss.setSpan(0, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tabLayout.addTab(tabLayout.newTab().setText(ss));

        titles = new ArrayList<>();
        ArrayList<String> data = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            data.add(i + "个数据");
        }


        final ArrayList<Fragment> fragments = new ArrayList<>();

        for (int i = 0; i < 1; i++) {
            fragments.add(new MyFragment());
            titles.add("标题" + i);
        }

        pager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int i) {
                return fragments.get(i);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return titles.get(position);
            }
        });

    }


    class Adapter extends PagerAdapter {
        ArrayList<String> data     = new ArrayList<>();
        Context           context;
        String            titles[] = {new String("one"), new String("two"), new String("three")};

        public Adapter(Context context, ArrayList<String> data) {
            this.data = data;
            this.context = context;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View inflate = View.inflate(context, R.layout.activity_pager_item, null);

            TextView item = inflate.findViewById(R.id.item_pager_textView);
            String   str  = data.get(position);
            item.setText(str);
            container.addView(inflate);

            return inflate;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }


    }
}
