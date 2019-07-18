package com.example.review.New;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

public class SaveCategoryStruct extends SaveData {
    public LinkedList<String> names = new LinkedList<>();

    public SaveCategoryStruct() {
        names.add("默认分组");
    }

    @Override
    public void getBytes(DataOutputStream dos) throws IOException {
        dos.writeInt(names.size());
        for (String str : names) dos.writeUTF(str);
    }

    @Override
    public void loadWith(DataInputStream dis) throws IOException {
        int size = dis.readInt();
        for (int i = 0; i < size; i++) {
            String str = dis.readUTF();
            names.add(str);
        }
    }
}
