package com.example.review.New;

import java.io.*;

public interface SaveDataInterface {

    byte[] toBytes();

    void toBytes(DataOutputStream dos) throws IOException;

    void loadWith(byte[] rawBytes);

    void loadWith(DataInputStream dis) throws IOException;

    void save(File path);

    void read(File path);
}
