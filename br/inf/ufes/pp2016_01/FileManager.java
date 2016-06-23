package br.inf.ufes.pp2016_01;

import java.io.*;
import java.util.Arrays;

public class FileManager {

    public byte[] readFile(String name) throws IOException {
        File file = new File(name);
        InputStream input = new FileInputStream(file);
        long length = file.length();
        // //cria array
        byte[] data = new byte[(int)length];
        int start = 0;
        int lengthtoRead = 0;
        while ((start < data.length) &&
                (lengthtoRead = input.read(data, start, data.length - start)) >= 0) {
            start += lengthtoRead;
        }
        input.close();
        return data;
    }

    private static void saveFile(String name, byte[] data) throws IOException {
        FileOutputStream outputFile = new FileOutputStream(name);
        outputFile.write(data);
        outputFile.close();
    }
}
