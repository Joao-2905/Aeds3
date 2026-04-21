package CRUD.util;

import java.io.*;

public class FileManeger {

    public static RandomAccessFile open(String path) throws IOException {

        File file = new File(path);

        // cria pasta se não existir
        File parent = file.getParentFile();
        if(parent != null && !parent.exists()){
            parent.mkdirs();
        }

        return new RandomAccessFile(file, "rw");
    }
}