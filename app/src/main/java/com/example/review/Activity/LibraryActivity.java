package com.example.review.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.review.New.LibraryList;
import com.example.review.New.LibraryStruct;
import com.example.review.R;

public class LibraryActivity extends AppCompatActivity {

    private LibraryList          libraries;
    private MyAdapter            adapter;
    private ImageView            imageViewBack;
    private EditText             search;
    private RecyclerView         list;
    private FloatingActionButton floating;
    private int                  mUpDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        list = findViewById(R.id.library_recyclerView_list);
        search = findViewById(R.id.library_editText_search);
        imageViewBack = findViewById(R.id.library_imageView_back_button);
        floating = findViewById(R.id.library_floatingActionButton);

        libraries = MainActivity.data.getLibraries();

        adapter = new MyAdapter(libraries);
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        list.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int i, int i1) {
                mUpDown = i1;
                return false;
            }
        });

        floating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int                        scrollState = list.getScrollState();
                RecyclerView.LayoutManager lm          = list.getLayoutManager();
                assert lm != null;
                int itemCount = lm.getItemCount() - 1;

                if (scrollState > 0) {
                    if (mUpDown < 0) list.scrollToPosition(0);
                    else if (mUpDown > 0)
                        list.scrollToPosition(itemCount);
                }
            }
        });
        floating.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                RecyclerView.LayoutManager lm        = list.getLayoutManager();
                assert lm != null;
                int itemCount = lm.getItemCount() - 1;
                list.scrollToPosition(itemCount / 2);
                return false;
            }
        });
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    acc();
                }
                return true;
            }
        });
    }

    private void acc() {
        LibraryList libs = new LibraryList();
        String      text = search.getText().toString();

        for (LibraryStruct library : libraries) {
            String libText = library.getText();

            if (libText.contains(text)) {
                libs.add(library);
            }
        }

        int size = libs.size();
        if (size == 0) adapter = new MyAdapter(libraries);
        else adapter = new MyAdapter(libs);
        list.setAdapter(adapter);
        Toast.makeText(this, "一共找到" + size + "项", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class MyAdapter extends RecyclerView.Adapter {
        private LibraryList libraries;

        public MyAdapter(LibraryList libraries) {
            this.libraries = libraries;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View inflate = LayoutInflater.from(LibraryActivity.this).inflate(R.layout.activity_library_item, viewGroup, false);
            return new MyHolder(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int posi) {
            MyHolder      holder        = (MyHolder) viewHolder;
            LibraryStruct libraryStruct = libraries.get(posi);

            holder.index.setText(String.format("%d", posi));
            holder.textView.setText(libraryStruct.getText());
            holder.view.setOnClickListener(getclickListener(posi));

        }

        @Override
        public int getItemCount() {
            if (libraries == null) return 0;
            return libraries.size();
        }
    }

    private View.OnClickListener getclickListener(final int posi) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("indexOfItem", posi);
                setResult(2, intent);
                finish();
            }
        };
    }

    class MyHolder extends RecyclerView.ViewHolder {
        View view;
        private final TextView textView;
        private final TextView index;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            textView = view.findViewById(R.id.library_item_textView_text);
            index = view.findViewById(R.id.library_item_textView_index);
        }
    }

}
