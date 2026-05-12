package com.filmes.crud;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileManeger {

    public static RandomAccessFile open(String path) throws IOException {
        File file = new File(path);

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        return new RandomAccessFile(file, "rw");
    }
}