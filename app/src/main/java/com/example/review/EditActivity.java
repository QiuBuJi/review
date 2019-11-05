package com.example.review;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.review.DataStructureFile.DateTime;
import com.example.review.DataStructureFile.ReviewData;
import com.example.review.New.LibrarySet;
import com.example.review.New.LibraryStruct;
import com.example.review.New.ReviewStruct;


public class EditActivity extends Activity
        implements View.OnClickListener,
                   CompoundButton.OnCheckedChangeListener,
                   NumberPicker.OnValueChangeListener {

    EditText     editTextExplain;
    TextView     textViewNumber;
    TextView     textViewSave;
    ImageView    imageViewBackButton;
    EditText     editTextWord;
    ReviewStruct rs;
    NumberPicker picker;
    ReviewData   data;
    Switch       switchJoin;
    int          level;
    boolean      checked;
    LibrarySet   libraries;
    TextView     textViewTypeWord;
    TextView     textViewTypeExplain;
    ScrollView   scrollList;
    TextView     timeLogs;
    private Button buttonUp;
    private Button buttonDown;
    private Switch swGenerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        textViewNumber = findViewById(R.id.textView_number);
        editTextExplain = findViewById(R.id.editText_explain);
        editTextWord = findViewById(R.id.editText_word);
        imageViewBackButton = findViewById(R.id.edit_imageView_back_button);
        textViewSave = findViewById(R.id.edit_button_save);
        picker = findViewById(R.id.edit_numberPiker_piker);
        switchJoin = findViewById(R.id.edit_switch_join);
        textViewTypeWord = findViewById(R.id.edit_textView_type_word);
        textViewTypeExplain = findViewById(R.id.edit_textView_type_explain);

        scrollList = findViewById(R.id.edit_scrollView_list);
        timeLogs = findViewById(R.id.edit_scrollView_textView_detail);

        buttonUp = findViewById(R.id.edit_button_up);
        buttonDown = findViewById(R.id.edit_button_down);
        swGenerate = findViewById(R.id.edit_switch_generate_reverse);


        //设置监听器
        switchJoin.setOnCheckedChangeListener(this);
        picker.setOnValueChangedListener(this);
        imageViewBackButton.setOnClickListener(this);
        textViewSave.setOnClickListener(this);
        textViewTypeWord.setOnClickListener(this);
        textViewTypeExplain.setOnClickListener(this);
        textViewNumber.setOnClickListener(this);
//        editTextWord.setOnClickListener(this);
//        editTextExplain.setOnClickListener(this);
        buttonUp.setOnClickListener(this);
        buttonDown.setOnClickListener(this);


        data = MainActivity.data;
        picker.setMaxValue(12);
        picker.setMinValue(0);
        switchJoin.setChecked(false);

        libraries = MainActivity.data.getLibraries();


        //添加编辑内容&显示详细内容，的分支************************************************************
        if (ListActivity.currentClickedRs != null) {
            //显示************************************************************
            rs = ListActivity.currentClickedRs;

            //设置要显示的数据
            editTextWord.setText(rs.getMatch());
            editTextExplain.setText(rs.getShow());
            textViewNumber.setText(String.valueOf(rs.getLevel()));

            textViewTypeWord.setText(String.valueOf(rs.match.getType()));
            textViewTypeExplain.setText(String.valueOf(rs.show.getType()));

            swGenerate.setEnabled(false);

            level = rs.getLevel();
            picker.setValue(level);
            int count = 0;


            DateTime oldLog = null;
            for (byte[] mlog : rs.logs) {
                DateTime log = new DateTime(mlog);

                String        strIndex = DateTime.fillChar(++count, 3, ' ');
                StringBuilder sb       = new StringBuilder(log.toString());

                if (oldLog == null) {
                    oldLog = new DateTime(log);
                    timeLogs.append(strIndex + ". " + log.toString() + '\n');
                } else {
                    if (log.getYear() == oldLog.getYear()) {

                        if (log.getMonth() == oldLog.getMonth()) {

                            if (log.getDay() == oldLog.getDay()) {

                                if (log.getHour() == oldLog.getHour()) {

                                    if (log.getMinute() == oldLog.getMinute()) {

                                        if (log.getSecond() == oldLog.getSecond()) {
                                            sb.replace(0, 21, "    ┊  ┊  ┊   ┊  ┊  ┊");
                                        } else {
                                            oldLog.setSecond(log.getSecond());
                                            sb.replace(0, 18, "    ┊  ┊  ┊   ┊  ┊");
                                        }
                                    } else {
                                        oldLog.setMinute(log.getMinute());
                                        sb.replace(0, 15, "    ┊  ┊  ┊   ┊");
                                    }
                                } else {
                                    oldLog.setHour(log.getHour());
                                    sb.replace(0, 11, "    ┊  ┊  ┊");
                                }
                            } else {
                                oldLog.setDay(log.getDay());
                                sb.replace(0, 8, "    ┊  ┊");
                            }
                        } else {
                            oldLog.setMonth(log.getMonth());
                            sb.replace(0, 5, "    ┊");
                        }
                    } else {
                        oldLog = new DateTime(log);
                    }

                    String text = strIndex + ". " + sb.toString() + '\n';
                    timeLogs.append(text);
                }
            }

            switchJoin.setChecked(rs.joined);
        }
    }


    String[] items = {"1 纯单词", "2 单词解释", "3 填空式", "4 图片", "5 声音"};

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edit_imageView_back_button:
                finish();
                break;
            case R.id.edit_button_save:
                if (ListActivity.currentClickedRs == null) addData();
                else editData();
                break;
            case R.id.edit_textView_type_word:
                new AlertDialog.Builder(this)
                        .setItems(items, getListener(textViewTypeWord))
                        .setTitle("选择类型")
                        .show();
                break;
            case R.id.edit_textView_type_explain:
                new AlertDialog.Builder(this)
                        .setItems(items, getListener(textViewTypeExplain))
                        .setTitle("选择类型")
                        .show();
                break;
            case R.id.editText_word:
                //启动图片选择对话框

                break;
            case R.id.editText_explain:
                //启动图片选择对话框

                break;

            case R.id.edit_button_up:
                requestCode = REQUEST_CODE_WORD;
                dialogShow();
                break;
            case R.id.edit_button_down:
                requestCode = REQUEST_CODE_EXPLAIN;
                dialogShow();
                break;
        }
    }

    private void dialogShow() {
        new AlertDialog.Builder(this)
                .setItems(itemsChoose, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                startActivityForResult(new Intent(EditActivity.this, LibraryActivity.class), requestCode);
                                break;
                            case 1:
                                startActivityForResult(new Intent(EditActivity.this, FilePicker.class), requestCode);
                                break;
                        }
                    }
                }).show();
    }

    private DialogInterface.OnClickListener getListener(final TextView textViewType) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i >= 0) {
                    String item   = items[i];
                    char   chtype = item.charAt(0);
                    textViewType.setText(String.valueOf(chtype));
                }
            }
        };
    }

    //返回界面数据
    ReviewStruct getSavingData() {
        ReviewStruct rsSave = new ReviewStruct();


        String word    = editTextWord.getText().toString();
        String explain = editTextExplain.getText().toString();

        LibraryStruct lsWord    = new LibraryStruct(word, 1);
        LibraryStruct lsExplain = new LibraryStruct(explain, 2);


        CharSequence textWord = textViewTypeWord.getText();
        int          typeWord = Integer.valueOf((String) textWord);
        lsWord.setType(typeWord);


        CharSequence textExplain = textViewTypeExplain.getText();
        int          typeExplain = Integer.valueOf((String) textExplain);
        lsExplain.setType(typeExplain);


        rsSave.addData(lsExplain, lsWord);
        rsSave.joined = checked;
        rsSave.setLevel(level);

        return rsSave;
    }

    //添加数据
    private void addData() {
        ReviewStruct rsTemp = getSavingData();

        data.addLibrary(0, rsTemp.match, rsTemp.show);
        data.add(0, rsTemp);//数据添加到顶部

        if (swGenerate.isChecked()) {
            ReviewStruct  rs   = getSavingData();
            LibraryStruct show = rs.show;
            rs.show = rs.match;
            rs.match = show;
            rs.show.setType(1);
            rs.match.setType(2);
            data.addLibrary(0, rs.match, rs.show);
            data.add(0, rs);//数据添加到顶部
        }

        data.saveLibrary();
        data.save();//保存数据
        Toast.makeText(EditActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();

        //加入复习
        if (rsTemp.joined) {
            data.sortAddToInactivate(rsTemp);
        }


        finish();
    }

    //编辑数据
    private void editData() {
        try {
            boolean      isChange     = false;
            ReviewStruct reviewStruct = getSavingData();
            int          level        = reviewStruct.getLevel();

            if (level != rs.getLevel()) isChange = true;

            rs.setLevel(level);
            rs.joined = reviewStruct.joined;
            rs.show.copyOf(reviewStruct.show);
            rs.match.copyOf(reviewStruct.match);

            //加入复习
            if (rs.joined) {
                if (isChange) {
                    rs.setLevel(rs.getLevel() - 1);
                    data.updateInavalable_AddLevel(rs);
                } else {
                    data.sortAddToInactivate(rs);
                }
            } else {
                data.removeFromInavalable_Avalable(rs);
            }

            data.saveLibrary();
            data.save();//保存数据

            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(EditActivity.this, "不能保存大于12的值", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ListActivity.currentClickedRs = null;
    }


    String[] itemsChoose = {"在库内选择", "选择图片"};

    int requestCode;
    private static final int REQUEST_CODE_WORD    = 1;
    private static final int REQUEST_CODE_EXPLAIN = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //没有返回数据
        if (resultCode == 0) return;

        //接收图片路径
        String        directory = data.getStringExtra("directory");
        LibraryStruct ls        = null;
        if (directory == null) {
            int index = data.getIntExtra("indexOfItem", -1);
            ls = libraries.get(index);
        }

        switch (requestCode) {
            case REQUEST_CODE_WORD:
                if (directory != null) {
                    editTextWord.setText(directory);
                    textViewTypeWord.setText(String.valueOf(4));
                } else {
                    editTextWord.setText(ls.getText());
                    textViewTypeWord.setText(String.valueOf(ls.getType()));
                }
                break;
            case REQUEST_CODE_EXPLAIN:
                if (directory != null) {
                    editTextExplain.setText(directory);
                    textViewTypeExplain.setText(String.valueOf(4));
                } else {
                    editTextExplain.setText(ls.getText());
                    textViewTypeExplain.setText(String.valueOf(ls.getType()));
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        checked = isChecked;
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int value) {
        level = value;
        textViewNumber.setText(String.valueOf(level));
    }

}
