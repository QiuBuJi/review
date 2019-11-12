package com.example.review.New;

import android.support.annotation.NonNull;

import com.example.review.DataStructureFile.ReviewData;

import java.io.*;

public class LibraryStruct extends StoreData {
    private String text  = "";
    private int    type  = 0;
    public  int    refer = 0;
    public  int    id    = 0;

    public LibraryStruct() {
    }

    public LibraryStruct(LibraryStruct ls) {
        this.text = ls.text;
        this.type = ls.type;
    }

    public void copyOf(LibraryStruct ls) {
        //是引用的话，则不能拷贝
        if (refer > 0) return;

        //拷贝数据
        this.text = ls.text;
        this.type = ls.type;
    }

    public LibraryStruct(String text, int type) {
        this.text = text;
        this.type = type;
    }

    public LibraryStruct(byte[] rawBytes) {
        loadWith(rawBytes);
    }

    public void setIdAuto() {
        if (ReviewData.mLibraries != null) {
            int maxId = 0;
            for (LibraryStruct ls : ReviewData.mLibraries) if (ls.id > maxId) maxId = ls.id;
            id = maxId + 1;
        }
    }

    public String getText() {
        if (refer > 0 && ReviewData.mLibraries != null) {
            for (LibraryStruct ls : ReviewData.mLibraries)
                if (ls.id == refer) return ls.getText();
            return "**引用丢失！**";
        }
        return text;
    }

    public void setText(String text) {
        if (refer > 0) this.text = "";
        else this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        dos.writeUTF(text);
        dos.writeInt(type);

        dos.writeInt(id);
        dos.writeInt(refer);
    }

    @Override
    public void loadWith(DataInputStream dis) throws IOException {
        text = dis.readUTF();
        type = dis.readInt();

        id    = dis.readInt();
        refer = dis.readInt();
    }

    @NonNull
    @Override
    public String toString() {
        String format;
        if (refer > 0) format = String.format("ID:%d", id);
        else format = String.format("type[%d]： %d", type, text);
        return format;
    }
}
