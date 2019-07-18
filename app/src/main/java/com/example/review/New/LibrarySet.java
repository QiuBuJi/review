package com.example.review.New;

import java.io.*;

public class LibrarySet extends StoreStruct<LibraryStruct> {

    public void save(File path) {
        try {
            FileOutputStream out   = new FileOutputStream(path);
            byte[]           bytes = getBytes();
            out.write(bytes);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readOf(File path) {
        removeAll(this);

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

    @Override
    public void getBytes(DataOutputStream dos) throws IOException {
        dos.writeInt(size());
        for (LibraryStruct libraryStruct : this) {
            byte[] bytes = libraryStruct.getBytes();
            dos.writeInt(bytes.length);
            dos.write(bytes);
        }
    }

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
