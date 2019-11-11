package com.example.review.Activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.review.R;
import com.example.review.Setting;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView list;
    private ImageView    imageViewBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        list = findViewById(R.id.setting_recyclerView_list);
        imageViewBack = findViewById(R.id.setting_imageView_back_button);


        imageViewBack.setOnClickListener(this);

        final String[] stringArray = getResources().getStringArray(R.array.strings_settings);

        list.setAdapter(getAdapter(stringArray));
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setItemAnimator(new DefaultItemAnimator());
        list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }

    private RecyclerView.Adapter getAdapter(final String[] stringArray) {
        return new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View inflate = LayoutInflater.from(SettingActivity.this).inflate(R.layout.activity_setting_item, viewGroup, false);
                return new Holder(inflate);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                View     view     = ((Holder) viewHolder).view;
                TextView textView = view.findViewById(R.id.settingItem_itextView_text);
                Switch   switch_  = view.findViewById(R.id.settingItem_switch);

                final String text = stringArray[i];
                textView.setText(text);

                boolean state = Setting.getBoolean(text);


                switch_.setChecked(state);

                switch_.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        Setting.set(text, b);
                    }
                });

            }

            @Override
            public int getItemCount() {
                return stringArray.length;
            }
        };
    }

    @Override
    public void onClick(View view) {
        finish();
    }


    class Holder extends RecyclerView.ViewHolder {
        View view;

        Holder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }
    }
}
