package com.example.review.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.review.Adapter.AdapterList;
import com.example.review.DataStructureFile.ReviewData;
import com.example.review.New.ReviewStruct;
import com.example.review.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;


public class ListActivity extends Activity implements View.OnClickListener, TextView.OnEditorActionListener {

    ImageView imageViewBackButton;
    ImageView imageViewAdd;
    ImageView imageViewImport;
    EditText  editTextSearch;
    public  Button       buttonDelete;
    private RecyclerView list;

    private       AdapterList  adapter;
    static public boolean      checked;
    static public boolean      switch_state = false;
    static public ReviewStruct currentClickedRs;

    private ReviewData           searchList;
    private TextView             title;
    private FloatingActionButton floating;
    private int                  mUpDown;
    public  ReviewData           data;
    int mDy = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        imageViewBackButton = findViewById(R.id.list_imageView_back_button);
        imageViewAdd        = findViewById(R.id.imageView_add);
        imageViewImport     = findViewById(R.id.imageView_import);
        editTextSearch      = findViewById(R.id.editText_search);
        buttonDelete        = findViewById(R.id.button_delete);
        list                = findViewById(R.id.list_recycler_list);
        title               = findViewById(R.id.list_textView_title);
        floating            = findViewById(R.id.list_floatingActionButton);

        data = MainActivity.data;

        //初始化RecyclerView
        adapter = new AdapterList(this, data);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        list.setVerticalScrollBarEnabled(true);
        list.setAdapter(adapter);


        //返回按钮
        imageViewBackButton.setOnClickListener(this);
        //添加按钮
        imageViewAdd.setOnClickListener(this);
        //导入按钮，导入电脑数据
        imageViewImport.setOnClickListener(this);
        //删除按钮被单击
        buttonDelete.setOnClickListener(this);
        //设置输入法，搜索图标
        editTextSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        //设置IME搜索图标被按下的事件
        editTextSearch.setOnEditorActionListener(this);    //*** onEditorAction ***
        //悬浮按钮，点击事件
        floating.setOnClickListener(this);
        //列表滑动事件
        list.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int i, int i1) {
                mUpDown = i1;
                return false;
            }
        });
        //悬浮按钮，长按事件
        floating.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                RecyclerView.LayoutManager lm = list.getLayoutManager();
                assert lm != null;
                int itemCount = lm.getItemCount() - 1;
                list.scrollToPosition(itemCount / 2);
                return false;
            }
        });


        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mDy = dy;
            }
        });
    }

    //***************************** onEditorAction ***
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String txt = editTextSearch.getText().toString();

        //输入:回车搜索（被按下）、IME_ACTION_SEARCH 任意一个后，搜索内容
        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
            event.getAction() == KeyEvent.ACTION_DOWN &&
            actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {

            searchList = new ReviewData();

            if (txt.equals("")) {
                int          size = data.size();
                String       match, match1;
                ReviewStruct rsFirst, rsLast;
                boolean      hasFound;

                for (int i = 0; i < size; i++) {
                    rsFirst  = data.get(i);
                    match    = rsFirst.getMatch();
                    hasFound = false;
//                    if (rsFirst.match.getType() != 1) continue;

                    for (int k = i + 1; k < size; k++) {
                        rsLast = data.get(k);
                        match1 = rsLast.getMatch();

//                        if (rsLast.match.getType() != 1) continue;

                        if (match.equals(match1)) {
                            hasFound = true;
                            searchList.add(rsLast);
                        }
                    }
                    if (hasFound) searchList.add(rsFirst);
                }
            } else {
                //遍历数据，查找包含txt内容的数据

                Pattern compile = Pattern.compile("\\d*:.+");
                int     index   = 0;

                //匹配格式
                if (compile.matcher(txt).matches()) {
                    String[] split = Pattern.compile(":").split(txt);

                    //查找：类型未指定
                    if (split[0].equals("")) {
                        for (ReviewStruct rs : data) {
                            if (rs.match.getText().equals(split[1]) || rs.show.getText().equals(split[1])) {
                                rs.posi = index;
                                searchList.add(rs);
                            }
                            index++;
                        }
                    } else {
                        //查找：指定了类型
                        int num = Integer.parseInt(split[0]);

                        for (ReviewStruct rs : data) {
                            if ((rs.match.getText().equals(split[1]) && rs.match.getType() == num) ||
                                (rs.show.getText().equals(split[1]) && rs.show.getType() == num)) {
                                rs.posi = index;
                                searchList.add(rs);
                            }
                            index++;
                        }
                    }


                } else {
                    //模糊查找
                    for (ReviewStruct rs : data) {
                        if (rs.getMatch().contains(txt) || rs.getShow().contains(txt)) {
                            rs.posi = index;
                            searchList.add(rs);
                        }
                        index++;
                    }
                }
            }


            if (searchList.size() > 0) {
                adapter = new AdapterList(ListActivity.this, searchList);
                list.setAdapter(adapter);
                Toast.makeText(ListActivity.this, "一共搜索到" + searchList.size() + "个", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(ListActivity.this, "没有找到数据！", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void importData() {

        //弹出对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
        builder.setPositiveButton("确定", getOnClickListener());
        builder.setNegativeButton("取消", null);
        builder.setIcon(R.mipmap.warnning_icon1);
        builder.setTitle("确定要导入数据&删除所有当前数据？");
        builder.show();
    }

    private DialogInterface.OnClickListener getOnClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                //进度条******************************************************************************
                final ProgressDialog pd = new ProgressDialog(ListActivity.this);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setTitle("进度：");

                //handler过程
                final Handler handler = new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message message) {
                        switch (message.what) {
                            case 1:
                                pd.setTitle((CharSequence) message.obj);
                                break;
                            case 0:
                                pd.dismiss();
                                Toast.makeText(ListActivity.this, "一共导入" + data.size() + "个", Toast.LENGTH_SHORT).show();
                                adapter.notifyDataSetChanged();
                                break;
                        }
                        return false;
                    }
                });

                //进度回调函数*************************************************************************
                final long temp = System.currentTimeMillis();
                data.setProgress(new ReviewData.ProgressListener() {
                    @Override
                    public void onProgress(int total, int posi) {
                        pd.setMax(total);
                        pd.incrementProgressBy(1);

                        long millis = System.currentTimeMillis() - temp;
                        millis /= 1000;

                        Message msg = new Message();
                        msg.obj  = String.valueOf(millis).concat("秒");
                        msg.what = 1;
                        handler.sendMessage(msg);

                    }
                });

                pd.show();
                //开启线程读取数据*********************************************************************
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        data.loadDataOf(MainActivity.pathInit);
                        handler.sendEmptyMessage(0);
                    }
                }).start();


            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        switch_state = false;
        adapter.notifyDataSetChanged();
        checked = false;
    }

    @Override
    public void onBackPressed() {
        if (checked) {
            switch_state = false;
            buttonDelete.setVisibility(View.GONE);//隐藏删除按钮

            //把dt数据中的checked复位false，这样recyclerView就不会显示灰色背景了
            for (ReviewStruct dt : data)
                dt.selected = false;

            adapter.notifyDataSetChanged();
            checked = false;
        } else {
            if (searchList != null) {
                adapter = new AdapterList(ListActivity.this, data);
                list.setAdapter(adapter);
                editTextSearch.setText("");
                searchList = null;
                adapter.notifyDataSetChanged();
            } else
                finish();
        }
    }

    @Override
    public void onClick(View v) {
        int count = 0;

        switch (v.getId()) {
            case R.id.list_imageView_back_button:
                //把dt数据中的checked复位false，这样recyclerView就不会显示灰色背景了
                for (ReviewStruct rs : data) rs.selected = false;
                finish();
                break;
            case R.id.imageView_add:
                currentClickedRs = null;
                startActivity(new Intent(ListActivity.this, EditActivity.class));
                break;
            case R.id.imageView_import:
                importData();
                break;
            case R.id.button_delete:
                buttonDelete(count);
                break;
            case R.id.list_floatingActionButton:
                int scrollState = list.getScrollState();
                RecyclerView.LayoutManager lm = list.getLayoutManager();
                assert lm != null;
                int itemCount = lm.getItemCount() - 1;

                //在滑动的情况下
                if (scrollState > 0) {
                    if (mUpDown < 0) //上滑
                        list.scrollToPosition(0);
                    else if (mUpDown > 0) //下滑
                        list.scrollToPosition(itemCount);
                } else {
                    //下滑
                    if (mDy >= 0) {

                        //向下寻找未加入复习的
                        for (int i = adapter.posi; i < data.size(); i++) {
                            ReviewStruct ele = data.get(i);

                            if (!ele.joined) {
                                list.scrollToPosition(i);
                                Toast.makeText(this, "位置:" + (i + 1), Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    } else {
                        //上滑

                        //向上寻找未加入复习的
                        for (int i = adapter.posi; i >= 0; i--) {
                            ReviewStruct ele = data.get(i);

                            if (!ele.joined) {
                                list.scrollToPosition(i);
                                Toast.makeText(this, "位置:" + (i + 1), Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void buttonDelete(int count) {
        String txt = buttonDelete.getText().toString();

        if (txt.equals("退出")) {
            onBackPressed();
            data.save();
            return;
        }

        //搜索状态下，删除条目
        if (searchList != null) {
            for (int index = 0; index < searchList.size(); index++) {
                ReviewStruct rs = searchList.get(index);

                //checked为true，则删除该条项目
                if (rs.selected) {
                    data.removeFromInavalable_Avalable(rs);
                    data.remove(rs.posi);
                    data.removeLibraryItem(rs.posi * 2);

                    for (int i = index; i < searchList.size(); i++) {
                        ReviewStruct reviewStruct = searchList.get(i);
                        reviewStruct.posi--;
                    }

                    //通知adapter它的posi位置上的数据被删除了
                    searchList.remove(index);
                    adapter.notifyItemRemoved(index);

                    index--;//删除了1像数据，它的位置不变。这里自减1，下次加一就和原来一样了
                    count++;
                }
            }
        } else {
            //正常状态，删除条目
            for (int index = 0; index < data.size(); index++) {
                ReviewStruct rs = data.get(index);

                //checked为true，则删除该条项目
                if (rs.selected) {
                    data.removeFromInavalable_Avalable(rs);
                    data.remove(index);
                    data.removeLibraryItem(index * 2);

                    //通知adapter它的posi位置上的数据被删除了
                    adapter.notifyItemRemoved(index);

                    index--;//删除了1像数据，它的位置不变。这里自减1，下次加一就和原来一样了
                    count++;
                }
            }
        }

        int sizeData    = data.size() * 2;
        int sizeLibrary = data.getLibraries().size();

        if (sizeData != sizeLibrary) {
            throw new IllegalStateException("数据不一致");
        }

        //显示删除了多少条目
        Toast.makeText(ListActivity.this, "一共删除" + count + "项", Toast.LENGTH_SHORT).show();

        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                onBackPressed();
                return false;
            }
        });

        //延时启动handler
        int ms = 500;
        if (count == 0) ms = 0;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, ms);

        //如果数据有更改，保存数据
        data.save();
        data.saveLibrary();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        data.save();//保存数据
    }
}
