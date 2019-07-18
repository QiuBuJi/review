package com.example.review.New;

import android.support.annotation.NonNull;

import com.example.review.DataStructureFile.DateTime;
import com.example.review.DataStructureFile.WordExplain;
import com.example.review.Keyboard.KeyboardType3;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReviewStruct extends SaveData {
    public LibraryStruct show;
    public LibraryStruct match;

    int previousID = 0;
    int classType  = 0;
    int level;

    public int      viewCount;
    public boolean  joined;
    public boolean  selected;
    public boolean  showed;
    public DateTime time = new DateTime(0, 0, 0, 0, 0, 0);

    public LinkedList<byte[]> logs = new LinkedList<>();

    public int posi;

    public ReviewStruct() {
    }

    public ReviewStruct(ReviewStruct rs) {
        this.show = rs.show;
        this.showed = rs.showed;
        this.time = rs.time;
        this.match = rs.match;
        this.joined = rs.joined;
        this.logs = rs.logs;
        this.posi = rs.posi;
        this.level = rs.level;
        this.classType = rs.classType;
        this.previousID = rs.previousID;
    }

    public ReviewStruct(int level) {
        this.level = level;
    }

    public ReviewStruct(byte[] rawBytes) {
        loadWith(rawBytes);
    }

    public String getShow() {
        return show.getText();
    }

    public void setShow(String text) {
        this.show.setText(text);
    }

    public String getMatch() {
        return match.getText();
    }

    //完成率，到这个阈值后判断为正确
    float corRate = 0.6f;

    public boolean matching(ArrayList<WordExplain> wes, CountList countList) {
        ArrayList<WordExplain> match     = getMatchWordExplains();
        int                    corrCount = 0, errCount = 0, total = 0;

        for (int i = 0; i < match.size(); i++) {
            WordExplain matchWE = match.get(i);
            WordExplain we      = null;

            for (WordExplain wde : wes) {
                String trim = wde.category.trim();
                if (matchWE.category.equals(trim)) {
                    we = wde;
                    break;
                }
            }

            total += matchWE.explains.size();

            assert we != null;
            ArrayList<String> list = new ArrayList<>(we.explains);

            for (int k = 0; k < list.size(); k++) {
                String  str      = list.get(k);
                boolean contains = matchWE.explains.contains(str);
                if (contains) {
                    list.remove(k--);
                    corrCount++;
                }
            }

            if (list.size() > 0) {
                errCount += list.size();
                for (String str : list) {
                    int    index = we.explains.indexOf(str);
                    String txt   = we.explains.get(index);
                    if ("×".equals(txt.charAt(0) + "")) continue;
                    we.explains.remove(index);
                    we.explains.add(index, "×" + str);
                }
            }

            for (WordExplain wordExplain : wes) {
                for (int k = 0; k < wordExplain.explains.size(); k++) {
                    String txt = wordExplain.explains.get(k);
                    char   c   = txt.charAt(0);
                    if ("×".equals(c + "")) continue;
                    for (int y = k + 1; y < wordExplain.explains.size(); y++) {
                        String s = wordExplain.explains.get(y);
                        if (txt.equals(s)) {
                            errCount++;
                            wordExplain.explains.remove(y);
                            wordExplain.explains.add(y, "×" + s);
                            corrCount--;
                        }
                    }
                }
            }
        }

        int needCorrectNum = (int) (total * corRate);

        countList.corrCount = corrCount;
        countList.errCount = errCount;
        countList.totalNum = total;
        countList.corrRate = corRate;
        countList.needCorrNum = needCorrectNum;

        return errCount == 0 && corrCount >= needCorrectNum;
    }

    public boolean matchingType3(ArrayList<KeyboardType3.TextCom> tc, ArrayList<String> candidate, CountList countList) {
        int corrCount = 0, errCount = 0, total = 0;


        ArrayList<KeyboardType3.TextCom> tcs = new ArrayList<>();
        for (KeyboardType3.TextCom textCom : tc) {
            if (textCom.isCandidate) tcs.add(textCom);
        }

        for (int i = 0; i < tcs.size(); i++) {
            String                txt1    = candidate.get(i);
            KeyboardType3.TextCom textCom = tcs.get(i);
            boolean               equals1 = txt1.equals(textCom.text);

            if (equals1) {
                corrCount++;
            } else {
                errCount++;
                textCom.isStrike = true;
            }
            total++;
        }

        int needCorrectNum = total;

        countList.corrCount = corrCount;
        countList.errCount = errCount;
        countList.totalNum = total;
        countList.corrRate = 1;
        countList.needCorrNum = needCorrectNum;

        return errCount == 0 && corrCount >= needCorrectNum;
    }


    public boolean matching(String text) {
        return getMatch().equals(text);
    }

    public ArrayList<WordExplain> getFrame() {
        ArrayList<WordExplain> wordExplains = getMatchWordExplains();
        for (WordExplain wordExplain : wordExplains) {
            wordExplain.explains.clear();
        }
        return wordExplains;
    }

    public ArrayList<WordExplain> getMatchWordExplains() {
        String match = getMatch();
        return getMatchWordExplains(match);
    }

    public static ArrayList<WordExplain> getMatchWordExplains(String text) {
        text = Pattern.compile("\\r\\n|\\r|\\n|\\s").matcher(text).replaceAll("");
        Pattern pattern = Pattern.compile("[a-zA-Z*]+\\.");
        Matcher matcher = pattern.matcher(text);

        String[]               strPrefixs = pattern.split(text);
        ArrayList<String>      prefixs    = new ArrayList<>();
        ArrayList<WordExplain> item       = new ArrayList<>();

        for (String prefix : strPrefixs) {
            if (prefix.equals("")) continue;
            prefixs.add(prefix);
        }

        int i = 0;
        while (matcher.find()) {
            if (i == prefixs.size()) break;

            int start = matcher.start();
            int end   = matcher.end();

            String   category = text.substring(start, end);
            String[] explains = Pattern.compile("[;；，,]").split(prefixs.get(i++));

            WordExplain wordExplain = new WordExplain();
            wordExplain.category = category;
            for (String explain : explains) {
                if (explain.equals("")) continue;
                wordExplain.explains.add(explain);
            }

            item.add(wordExplain);
        }

        //没有前缀的解释，这样处理
        if (i == 0) {
            WordExplain wordExplain = new WordExplain();
            String[]    explains    = Pattern.compile("[;；，,]").split(text);

            Collections.addAll(wordExplain.explains, explains);
            wordExplain.category = "*.";
            item.add(wordExplain);
        }

        return item;
    }

    public void setMatch(String text) {
        match.setText(text);
    }

    public final LibraryStruct getLibraryShow() {
        return show;
    }

    public LibraryStruct getLibraryMatch() {
        return match;
    }

    public void setShow(LibraryStruct show) {
        this.show = show;
    }

    public void setMatch(LibraryStruct match) {
        this.match = match;
    }

    public void addData(LibraryStruct show, LibraryStruct match) {
        this.show = show;
        this.match = match;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void resetLevel() {
        setLevel(0);
    }

    @Override
    public void getBytes(DataOutputStream dos) throws IOException {
        dos.writeInt(previousID);
        dos.writeByte(classType);
        dos.writeByte(level);
//        dos.writeByte(viewCount);

        dos.writeBoolean(joined);
        dos.writeBoolean(selected);
        dos.writeBoolean(showed);

        dos.write(time.getBytes());

        int size = logs.size();
        dos.writeInt(size);
        for (byte[] log : logs) {
            dos.write(log);
        }

        //压缩存储
//        byte[] oldBt = logs.get(0).getBytes();
//        int    posi  = -1;
//        dos.write(oldBt);
//
//        for (int i = 1; i < size; i++) {
//            DateTime log   = logs.get(i);
//            byte[]   bytes = log.getBytes();
//
//            for (int k = 0; k < bytes.length; k++) {
//
//                if (bytes[k] != oldBt[k]) {
//                    posi = k;
//                    break;
//                }
//            }
//
//            int len = bytes.length - posi;
//            dos.writeByte(len);
//            dos.write(bytes, posi, len);
//            oldBt = bytes;
//        }
    }


    @Override
    public void loadWith(DataInputStream dis) throws IOException {
        previousID = dis.readInt();
        classType = dis.readByte();
        level = dis.readByte();
//        viewCount = dis.readByte();

        joined = dis.readBoolean();
        selected = dis.readBoolean();
        showed = dis.readBoolean();


        byte[] bytes = new byte[7];
        dis.read(bytes);
        time = new DateTime(bytes);


        int size = dis.readInt();
        for (int i = 0; i < size; i++) {
            bytes = new byte[7];
            dis.read(bytes);
            logs.add(bytes);
        }

        //压缩存储
//        byte[] oldBt = null;
//        dis.read(bytes);
//        oldBt = bytes;
//        logs.add(new DateTime(bytes));
//
//        for (int i = 0; i < size; i++) {
//            byte len = dis.readByte();
//
//            byte[] clone = oldBt.clone();
//            dis.read(clone, 7 - len, len);
//            logs.add(new DateTime(clone));
//            oldBt = clone;
//        }

    }


    @NonNull
    @Override
    public String toString() {
        return time.toString();
//        return "单词：" + match.getText() + "  解释：" + show.getText();
    }
}
