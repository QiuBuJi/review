package com.example.review.New;

import java.io.*;

public class ReviewList extends ArrayStoreList<ReviewStruct> {

    //关联指定的库
    public void connectOf(LibraryList libraries) {
        for (int i = 0; i < this.size(); i++) {
            ReviewStruct rs = get(i);

            int index = i * 2;
            rs.show  = libraries.get(index + 1);
            rs.match = libraries.get(index);
        }
    }

    //取字节流
    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        dos.writeInt(size());
        for (ReviewStruct libraryStruct : this) {
            byte[] bytes = libraryStruct.toBytes();
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
            ReviewStruct libraryStruct = new ReviewStruct(bytes);
            add(libraryStruct);
        }
    }
}
