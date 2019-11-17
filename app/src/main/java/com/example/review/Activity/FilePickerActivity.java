package com.example.review.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.review.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class FilePickerActivity extends AppCompatActivity {

    ArrayList<String>  suffixs = new ArrayList<>(Arrays.asList("jpg", "png"));
    LinkedList<String> data    = new LinkedList<>();
    LinkedList<File>   files   = new LinkedList<>();
    RecyclerView       filePicker;
    MyAdapter          adapter;
    FilenameFilter     filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);
        filePicker = findViewById(R.id.filePicker_recyclerView_picker);
        filter = new MyFilenameFilter();

        File     file = MainActivity.externalRoot;
        String[] list = file.list(filter);

        files.add(file);
        data.addAll(Arrays.asList(list));

        adapter = new MyAdapter();
        filePicker.setAdapter(adapter);
        filePicker.setLayoutManager(new GridLayoutManager(this, 6));
    }

    public void onBackPressed() {

        try {
            files.removeLast();

            String[] list = files.getLast().list(filter);
            data.clear();
            data.addAll(Arrays.asList(list));
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            super.onBackPressed();
        }

    }


    class MyAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view     = LayoutInflater.from(FilePickerActivity.this).inflate(R.layout.activity_file_picker_item, viewGroup, false);

            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int posi) {
            MyHolder holder = (MyHolder) viewHolder;

            final File   last = files.getLast();
            final String text = data.get(posi);
            final File   file = new File(last, text);

            if (file.isDirectory()) {
                holder.image.setImageResource(R.mipmap.folder_icon);
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeFile(file.getPath(), options);


                options.inJustDecodeBounds = false;
                options.inSampleSize = 46;
//                options.inSampleSize = options.outWidth / 200;
                options.inScaled = true;
//
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);

                holder.image.setImageBitmap(bitmap);

            }

            holder.item.setText(text);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!file.isDirectory()) {
                        Intent intent = new Intent();
                        intent.putExtra("directory", file.getPath());
                        setResult(1, intent);
                        finish();
                    } else {
                        files.add(file);

                        String[] list = file.list(filter);
                        data.clear();
                        data.addAll(Arrays.asList(list));
                        adapter.notifyDataSetChanged();
                    }

                }
            });

        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    class MyFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File file, String str) {
            File path = new File(file, str);
            if (path.isDirectory() && !path.isHidden()) return true;

            int index = str.lastIndexOf('.');

            if (index != -1) {
                String  suffix   = str.substring(++index);

                return suffixs.contains(suffix);
            }

            return false;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView  item;
        View      view;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            item = view.findViewById(R.id.filePicker_textView_item);
            image = view.findViewById(R.id.filePicker_imageView_image);

        }
    }

}
