package com.example.review.New;

import java.io.*;

public abstract class StoreData implements SaveDataInterface {

    /**
     * 把内部数据，转换为字节流
     */
    public byte[] toBytes() {
        ByteArrayOutputStream out   = new ByteArrayOutputStream();
        DataOutputStream      dos   = new DataOutputStream(out);
        byte[]                bytes = null;
        try {
            toBytes(dos);
            bytes = out.toByteArray();
            dos.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * 设置要转换为字节流的数据
     *
     * @param dos 通过dos把数据输出
     */
    public abstract void toBytes(DataOutputStream dos) throws IOException;

    /**
     * 用字节流，装载数据
     *
     * @param rawBytes 字节流
     */
    public void loadWith(byte[] rawBytes) throws IOException {
        ByteArrayInputStream in  = new ByteArrayInputStream(rawBytes);
        DataInputStream      dis = new DataInputStream(in);

        loadWith(dis);
        dis.close();
        in.close();
    }

    /**
     * 把从字节流读取出来的数据，安需求设置到内部数据中
     *
     * @param dis 保存数据的参数
     */
    public abstract void loadWith(DataInputStream dis) throws IOException;

    /**
     * 保存数据到该路径
     *
     * @param path 路径
     */
    public void save(File path) {
        try {
            FileOutputStream out   = new FileOutputStream(path);
            byte[]           bytes = toBytes();
            out.write(bytes);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从路径读取数据
     *
     * @param path 路径
     */
    public void read(File path) {
        try {
            FileInputStream in     = new FileInputStream(path);
            int             length = (int) path.length();
            byte[]          bytes  = new byte[length];
            in.read(bytes);
            loadWith(bytes);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
