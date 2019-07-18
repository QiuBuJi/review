package com.example.review.New;

import java.io.*;

public class ReviewSet extends StoreStruct<ReviewStruct> {

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

    public void connectOf(LibrarySet librarySet) {
        for (int i = 0; i < this.size(); i++) {
            ReviewStruct rs = get(i);

            int k = i * 2;
            rs.show = librarySet.get(k + 1);
            rs.match = librarySet.get(k);
        }
    }

    @Override
    public void getBytes(DataOutputStream dos) throws IOException {
        dos.writeInt(size());
        for (ReviewStruct libraryStruct : this) {
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
            ReviewStruct libraryStruct = new ReviewStruct(bytes);
            add(libraryStruct);
        }
    }
}
