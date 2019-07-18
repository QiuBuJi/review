package com.example.review.New;

import android.support.annotation.NonNull;

import java.io.*;

public class LibraryStruct extends SaveData {
    private String text = "";
    private int    type;


    public LibraryStruct() {
    }

    public LibraryStruct(LibraryStruct ls) {
        this.text = ls.text;
        this.type = ls.type;
    }


    public void copyOf(LibraryStruct ls) {
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    @Override
    public void getBytes(DataOutputStream dos) throws IOException {
        dos.writeUTF(text);
        dos.writeInt(type);
    }


    @Override
    public void loadWith(DataInputStream dis) throws IOException {
        text = dis.readUTF();
        type = dis.readInt();
    }

    @NonNull
    @Override
    public String toString() {
        return "type = " + type + " text = " + text;
    }
}
