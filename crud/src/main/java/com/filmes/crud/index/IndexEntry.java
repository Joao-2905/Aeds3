package com.filmes.crud.index;

import java.io.IOException;
import java.io.RandomAccessFile;

public class IndexEntry {

    private int id;
    private long endereco;

    public IndexEntry(int id, long endereco) {
        this.id = id;
        this.endereco = endereco;
    }

    public int getId() {
        return id;
    }

    public long getEndereco() {
        return endereco;
    }

    public void setEndereco(long endereco) {
        this.endereco = endereco;
    }

    public void write(RandomAccessFile file) throws IOException {
        file.writeInt(id);
        file.writeLong(endereco);
    }

    public void read(RandomAccessFile file) throws IOException {
        this.id = file.readInt();
        this.endereco = file.readLong();
    }
}