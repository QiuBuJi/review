package com.example.review.New;

import java.io.*;

public interface SaveDataInterface {

    byte[] getBytes();

    void getBytes(DataOutputStream dos) throws IOException;

    void loadWith(byte[] rawBytes);

    void loadWith(DataInputStream dis) throws IOException;


}
