package com.example.review.New;

import java.io.*;
import java.util.ArrayList;

public abstract class StoreStruct<E> extends ArrayList<E> implements SaveDataInterface {
    @Override
    public byte[] getBytes() {
        ByteArrayOutputStream out      = new ByteArrayOutputStream();
        DataOutputStream      dos      = new DataOutputStream(out);
        byte[]                rawBytes = null;

        try {
            getBytes(dos);
            rawBytes = out.toByteArray();
            dos.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rawBytes;
    }

    @Override
    public abstract void getBytes(DataOutputStream dos) throws IOException;

    @Override
    public void loadWith(byte[] rawBytes) {
        ByteArrayInputStream in  = new ByteArrayInputStream(rawBytes);
        DataInputStream      dis = new DataInputStream(in);
        try {
            loadWith(dis);
            dis.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    abstract public void loadWith(DataInputStream dis) throws IOException;
}
