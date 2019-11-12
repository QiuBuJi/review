package com.example.review.New;

import java.io.*;

public class LibraryList extends ArrayStoreList<LibraryStruct> {

    //取字节流
    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        dos.writeInt(size());
        for (LibraryStruct ls : this) {
            byte[] bytes = ls.toBytes();
            dos.writeInt(bytes.length);
            dos.write(bytes);
        }
    }

    //写字节流
    @Override
    public void loadWith(DataInputStream dis) throws IOException {
        int size = dis.readInt();
        for (int i = 0; i < size; i++) {
            int    len   = dis.readInt();
            byte[] bytes = new byte[len];
            dis.read(bytes);
            LibraryStruct libraryStruct = new LibraryStruct(bytes);
            add(libraryStruct);
        }
    }
}
