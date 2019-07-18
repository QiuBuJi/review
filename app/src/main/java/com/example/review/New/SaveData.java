package com.example.review.New;

import java.io.*;

public abstract class SaveData implements SaveDataInterface {

    public byte[] getBytes() {
        ByteArrayOutputStream out   = new ByteArrayOutputStream();
        DataOutputStream      dos   = new DataOutputStream(out);
        byte[]                bytes = null;
        try {
            getBytes(dos);
            bytes = out.toByteArray();
            dos.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public abstract void getBytes(DataOutputStream dos) throws IOException;

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

    public abstract void loadWith(DataInputStream dis) throws IOException;


    public void readFile(File file) {
        try {
            FileInputStream fis   = new FileInputStream(file);
            int             size  = (int) file.length();
            byte[]          bytes = new byte[size];
            fis.read(bytes);
            loadWith(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeFile(File file) {
        try {
            FileOutputStream fos   = new FileOutputStream(file);
            byte[]           bytes = getBytes();
            fos.write(bytes);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
